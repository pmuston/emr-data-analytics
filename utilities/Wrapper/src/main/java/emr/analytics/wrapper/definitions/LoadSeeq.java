package emr.analytics.wrapper.definitions;

import emr.analytics.models.definition.*;
import emr.analytics.wrapper.BlockDefinition;
import emr.analytics.wrapper.IExport;

import java.util.ArrayList;
import java.util.List;

public class LoadSeeq extends BlockDefinition implements IExport {

    @Override
    public Definition createDefinition() {
        return new Definition("LoadSeeq", "Load Seeq", Category.DATA_SOURCES.toString());
    }

    @Override
    public List<ConnectorDefinition> createInputConnectors() {
        return null;
    }

    @Override
    public List<ConnectorDefinition> createOutputConnectors() {
        List<ConnectorDefinition> outputConnectors = new ArrayList<ConnectorDefinition>();
        outputConnectors.add(new ConnectorDefinition("out", DataType.FRAME.toString()));
        return outputConnectors;
    }

    @Override
    public List<ParameterDefinition> createParameters() {

        List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();

        parameters.add(new ParameterDefinition("Seeq IP",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Project Name",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));

        parameters.add(new ParameterDefinition("Capsule Name",
                DataType.STRING.toString(),
                "",
                new ArrayList<String>(),
                null));

        return parameters;
    }

    @Override
    public Signature createSignature() {
        return null;
    }
}
