package emr.analytics.service.messages;

import emr.analytics.models.definition.Mode;

import java.util.UUID;

public class JobFailed extends JobStatus {

    public JobFailed(UUID id, Mode mode){ super(id, mode); }
}