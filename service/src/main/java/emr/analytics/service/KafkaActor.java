package emr.analytics.service;

import akka.actor.AbstractActor;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import emr.analytics.models.messages.StreamingInfo;
import emr.analytics.models.messages.StreamingSourceRequest;
import emr.analytics.service.kafka.JsonSerializer;
import emr.analytics.service.sources.SourceFactory;
import emr.analytics.service.sources.SourceValues;
import emr.analytics.service.sources.StreamingSource;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Logger;

import java.util.Properties;

public class KafkaActor extends AbstractActor {

    // initialize logger
    private static final Logger logger = Logger.getLogger(KafkaActor.class);

    // flag that indicates whether the producer is running
    private boolean running;
    private KafkaProducer<String, SourceValues<Double>> producer;
    private final StreamingSourceRequest request;
    private final StreamingInfo info;

    public static Props props(StreamingSourceRequest request) { return Props.create(KafkaActor.class, request); }

    public KafkaActor(StreamingSourceRequest request){

        // initialize the running flag
        this.running = false;

        // capture the streaming source request
        this.request = request;

        // load kafka properties
        Properties properties = JobServiceHelper.loadProperties("kafka");

        info = new StreamingInfo(request.getTopic(),
                request.getStreamingSource().getPollingSourceType(),
                request.getStreamingSource().getFrequency());

        try {
            // instantiate a kafka producer
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("zookeeper.quorum"));
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
            this.producer = new KafkaProducer<>(props);
        }
        catch(Exception ex){
            logger.error(String.format("Exception occurred while instantiating kafka producer. Details: %s.", ex.toString()));
        }

        receive(ReceiveBuilder

            /**
             *
             */
            .match(String.class, s -> s.equals("info"), s -> {

                sender().tell(info, self());
            })

            /**
             * start the streaming source job
             */
            .match(String.class, s -> s.equals("start"), s -> {
                run();


            })

            /**
             * set the stop flag and send a poison pill
             */
            .match(String.class, s -> s.equals("stop"), s -> {
                // set running flag to false and close producer
                this.running = false;
                this.producer.close();
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

        // get an instance of the streaming source
        StreamingSource source = SourceFactory.get(this.request);

        // reference the topic
        String topic = this.request.getTopic();

        // using the configured frequency - calculate the number of milliseconds to wait between reads
        int interval = 1000 / this.request.getStreamingSource().getFrequency();

        // spawn a thread to produce kafka record at defined interval
        new Thread(() -> {

            while(running){

                try{
                    SourceValues<Double> values = source.read();
                    producer.send(new ProducerRecord<>(topic, values));

                    Thread.sleep(interval);
                }
                catch(Exception ex){
                    logger.error(String.format("Exception occurred while writing to Kafka. Details: %s.", ex.toString()));
                }
            }
        }).start();
    }
}
