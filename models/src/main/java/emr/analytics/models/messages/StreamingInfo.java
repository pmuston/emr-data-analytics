package emr.analytics.models.messages;

import emr.analytics.models.sources.PollingSource;

import java.io.Serializable;
import java.util.Date;

public class StreamingInfo implements Serializable {

    private String topic;
    private PollingSource.PollingSourceType pollingSourceType;
    private int frequency;
    private Date started = null;
    private JobStates state;

    public StreamingInfo(String topic, PollingSource.PollingSourceType pollingSourceType, int frequency){
        this.topic = topic;
        this.pollingSourceType = pollingSourceType;
        this.frequency = frequency;
        this.started = new Date();
        this.state = JobStates.RUNNING;
    }

    public String getTopic(){ return this.topic; }

    public PollingSource.PollingSourceType getPollingSourceType(){ return this.pollingSourceType; }

    public int getFrequency(){ return this.frequency; }

    public Date getStarted(){ return this.started; }

    public JobStates getState(){ return this.state; }

    public void setState(JobStates state) { this.state = state; }
}
