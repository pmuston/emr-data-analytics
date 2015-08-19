package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.models.messages.JobVariable;
import emr.analytics.service.consumers.DataConsumer;
import emr.analytics.service.messages.ConsumeJob;
import emr.analytics.service.consumers.ConsumerData;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.apache.log4j.Logger;

import java.util.*;

public class KafkaConsumer extends AbstractActor {

    // initialize logger
    private static final Logger logger = Logger.getLogger(KafkaConsumer.class);

    private ConsumerConnector consumer;
    private ActorRef client;
    private final String topic = "ONLINE";
    private boolean running;

    private Map<UUID, JobVariable> consumerJobs;

    public static Props props() { return Props.create(KafkaConsumer.class); }

    public KafkaConsumer(){

        // initialize the running flag
        this.running = false;

        // initialize map of consumer jobs
        consumerJobs = new HashMap<>();

        // load kafka properties
        Properties properties = JobServiceHelper.loadProperties("kafka");

        try {
            // instantiate a kafka producer
            this.consumer = Consumer.createJavaConsumerConnector(this.getConfig());
        }
        catch(Exception ex){
            logger.error(String.format("Exception occurred while instantiating kafka consumer. Details: %s.", ex.toString()));
        }

        receive(ReceiveBuilder

                        /**
                         * When a reference to the client actor is received start the streaming source job
                         */
                        .match(ActorRef.class, actor -> {

                            this.client = actor;
                            run();
                        })

                        /**
                         * Manage the map of consumer jobs
                         */
                        .match(ConsumeJob.class, job -> {

                            switch (job.getState()) {
                                case START:
                                    if (!consumerJobs.containsKey(job.getDiagramId()))
                                        consumerJobs.put(job.getDiagramId(),
                                                new JobVariable(job.getDiagramId().toString()));
                                    break;
                                case END:
                                    if (consumerJobs.containsKey(job.getDiagramId()))
                                        consumerJobs.remove(job.getDiagramId());
                                    break;
                            }
                        })

                        /**
                         * set the stop flag and send a poison pill
                         */
                        .match(String.class, s -> s.equals("stop"), s -> {

                            // set running flag to false and close the consumer
                            this.running = false;
                            this.consumer.shutdown();
                            // kill this actor
                            self().tell(PoisonPill.getInstance(), self());
                        })

                        .build()
        );
    }

    /**
     * Asynchronously produce kafka records
     */
    private void run(){
        this.running = true;

        // Define single thread for topic
        Map<String, Integer> topicMap = new HashMap<String, Integer>();
        topicMap.put(this.topic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreamsMap = this.consumer.createMessageStreams(topicMap);

        List<KafkaStream<byte[], byte[]>> streamList = consumerStreamsMap.get(this.topic);

        // spawn a thread to consume records
        new Thread(() -> {

            while(running) {

                try {

                    for (final KafkaStream<byte[], byte[]> stream : streamList) {

                        ConsumerIterator<byte[], byte[]> iterator = stream.iterator();

                        while (iterator.hasNext()) {

                            MessageAndMetadata<byte[], byte[]> iteration = iterator.next();

                            UUID diagramId = UUID.fromString(new String(iteration.key()));
                            String value = new String(iteration.message());

                            if (this.client != null && this.consumerJobs.containsKey(diagramId)) {

                                JobVariable variable = this.consumerJobs.get(diagramId);
                                variable.add(value);

                                this.client.tell(variable, self());
                            }

                            ConsumerData consumerData = new ConsumerData();
                            consumerData.ip = "localhost";
                            consumerData.value = value;

                            DataConsumer.send("Simulated", consumerData);
                        }
                    }
                }
                catch(Exception ex) {

                    logger.error(String.format("Exception occurred while writing to Kafka. Details: %s.", ex.toString()));
                }
            }
        }).start();
    }

    private ConsumerConfig getConfig(){
        Properties props = new Properties();
        props.put("zookeeper.connect", "localhost:2181");
        props.put("group.id", "service");
        props.put("zookeeper.session.timeout.ms", "500");
        props.put("zookeeper.sync.time.ms", "250");
        props.put("auto.commit.interval.ms", "1000");

        return new ConsumerConfig(props);
    }
}
