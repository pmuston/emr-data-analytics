package emr.analytics.service.jobs;

import emr.analytics.models.definition.Definition;
import emr.analytics.models.definition.Mode;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.Wire;
import emr.analytics.service.SourceBlocks;
import emr.analytics.service.messages.JobRequest;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public abstract class AnalyticsJob implements Serializable {

    protected UUID _id;
    protected Mode _mode;
    protected String _diagramName;
    protected String _source;
    protected LogLevel _logLevel = LogLevel.Progress;

    public AnalyticsJob(JobRequest request, String template, HashMap<String, Definition> definitions){
        this._id = request.getJobId();
        this._mode = request.getMode();
        this._diagramName = request.getDiagram().getName();
        this._source = this.compile(request, template, definitions);
    }

    public String getDiagramName(){
        return _diagramName;
    }

    public String getName(){ return _diagramName + "_" + _mode.toString(); }

    public void setLogLevel(LogLevel level){
        _logLevel = level;
    }

    public LogLevel getLogLevel(){ return _logLevel; }

    public UUID getId(){ return _id; }

    public String getSource(){ return _source; }

    public Mode getMode() { return _mode; }

    protected String compile(JobRequest request, String template, HashMap<String, Definition> definitions){

        String source = "";

        Diagram diagram = request.getDiagram();

        // compile a list of blocks to execute
        SourceBlocks sourceBlocks = new SourceBlocks();

        HashSet<UUID> queued = new HashSet<UUID>();

        // Initialize queue of blocks to compile
        Queue<Block> queue = new LinkedList<Block>();
        for (Block block : diagram.getRoot()) {
            queued.add(block.getId());
            queue.add(block);
        }

        // Capture all configured blocks in order
        while (!queue.isEmpty()) {
            Block block = queue.remove();

            // Capture configured blocks and queue descending blocks
            if (block.isConfigured()) {

                sourceBlocks.add(block, diagram.getLeadingWires(block.getId()));

                for (Block next : diagram.getNext(block.getId())) {
                    if (!queued.contains(next.getId())) {

                        // confirm all leading blocks have been previously queued
                        boolean ready = true;
                        List<Wire> wires = diagram.getLeadingWires(next.getId());
                        for(Wire wire : wires){
                            if (!queued.contains(wire.getFrom_node()))
                                ready = false;
                        }

                        if (ready) {
                            queue.add(next);
                            queued.add(next.getId());
                        }
                    }
                }
            }
        }

        // compile configured blocks
        if (!sourceBlocks.isEmpty()) {
            try {
                source = sourceBlocks.compile(template);
            }
            catch (IOException ex) {
                System.err.println(String.format("IOException: %s.", ex.toString()));
            }

            // todo: temporarily print generated code
            System.out.println(source);
        }

        return source;
    }
}