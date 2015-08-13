package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class Query extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {

        Definition definition = new Definition("Query", "Query", Category.DATA_SOURCES.toString());
        definition.setDescription("Loads data based on a query");
        return definition;
    }

    @Override
    public ModeDefinition createOfflineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();
        modeDefinition.setOutputs(createOutputConnectors());
        modeDefinition.setParameters(createParameters());

        return modeDefinition;
    }

    @Override
    public ModeDefinition createOnlineMode(){

        ModeDefinition modeDefinition = new ModeDefinition();

        // create outputs
        List<ConnectorDefinition> outputs = new ArrayList<ConnectorDefinition>();
        outputs.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        modeDefinition.setOutputs(outputs);

        // create parameters
        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
        parameters.add(new ParameterDefinition("Topic",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));
        modeDefinition.setParameters(parameters);

        modeDefinition.setSignature(new Signature("StreamingSources",
                "kafkaStream",
                new String[]{
                        "ssc",
                        "parameter:Topic",
                        "broker"
                }));

        return modeDefinition;
    }

    public List<ConnectorDefinition> createOutputConnectors() {

        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        return outputConnectors;
    }

    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Project",
                DataType.LIST.toString(),
                "None",
                new ArrayList<String>(),
                new ParameterSource("Jar",
                        "plugins-1.0-SNAPSHOT.jar",
                        "Bricks",
                        new ArrayList<Argument>())));

        parameters.add(new ParameterDefinition("Query",
                DataType.QUERY.toString(),
                "None",
                new ArrayList<String>(),
                null));

        return parameters;
    }
}
