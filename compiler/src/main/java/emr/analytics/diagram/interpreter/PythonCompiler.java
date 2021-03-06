package emr.analytics.diagram.interpreter;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import emr.analytics.models.definition.DataType;
import emr.analytics.models.definition.Definition;
import emr.analytics.models.diagram.Block;
import emr.analytics.models.diagram.Diagram;
import emr.analytics.models.diagram.Parameter;
import emr.analytics.models.diagram.Wire;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class PythonCompiler implements TargetCompiler {

    private final String template = "python_driver.mustache";
    private HashMap<String, Definition> definitions;
    private Diagram diagram;

    public PythonCompiler(HashMap<String, Definition> definitions, Diagram diagram){

        this.definitions = definitions;
        this.diagram = diagram;
    }

    public CompiledDiagram compile(){

        // compile a list of blocks to execute
        PythonBlocks blocks = new PythonBlocks();

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

                blocks.add(block, diagram.getLeadingWires(block.getId()));

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
        String source = "";
        if (!blocks.isEmpty()) {
            try {
                source = blocks.compile(template);
            }
            catch (IOException ex) {
                System.err.println(String.format("IOException: %s.", ex.toString()));
            }
        }

        return new CompiledDiagram(source);
    }

    private class PythonBlocks {
        public List<DefinitionImport> definitionImports;
        public List<SourceBlock> blocks;

        private HashSet<String> definitionNames;

        public PythonBlocks() {
            definitionImports = new ArrayList<DefinitionImport>();
            definitionNames = new HashSet<String>();
            blocks = new ArrayList<SourceBlock>();
        }

        public void add(Block block, List<Wire> wires) {
            if(!definitionNames.contains(block.getDefinition())) {
                String definitionName = block.getDefinition();
                definitionImports.add(new DefinitionImport(String.format("from %s import %s", definitionName, definitionName)));
                definitionNames.add(definitionName);
            }

            Definition definition = definitions.get(block.getDefinition());

            blocks.add(new SourceBlock(definition, block, wires));
        }

        public boolean isEmpty() {
            return this.blocks.isEmpty();
        }

        public String compile(String template) throws IOException {
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(template);

            StringWriter writer = new StringWriter();
            mustache.execute(writer, this);
            return writer.toString();
        }

        public class DefinitionImport {
            public String definitionImport;

            public DefinitionImport(String definitionImport) {
                this.definitionImport = definitionImport;
            }
        }

        public class SourceBlock {
            public String name;
            public UUID id;
            public String variableName;
            public String definitionName;
            public String parameters;
            public String inputs;

            public SourceBlock(Definition definition, Block block, List<Wire> wires) {
                name = block.getName();
                id = block.getId();
                variableName = createVariableNameFromUniqueName(block.getId().toString());
                definitionName = block.getDefinition();

                // todo: string builders

                parameters = "";
                List<Parameter> params = block.getParameters();
                for(int i = 0; i < params.size(); i++) {
                    if (i > 0) {
                        parameters += ", ";
                    }

                    Parameter param = params.get(i);

                    String parameterType = param.getType();

                    if (parameterType != null) {

                        if (parameterType.equals(DataType.LIST.toString())
                                || parameterType.equals(DataType.STRING.toString())) {

                            parameters += String.format("'%s': '%s'", param.getName(), param.getValue());
                        }
                        else if (parameterType.equals(DataType.MULTI_SELECT_LIST.toString())) {

                            parameters += String.format("'%s': %s", param.getName(), adaptValueToStringArray(param.getValue()));

                        } else {

                            parameters += String.format("'%s': %s", param.getName(), param.getValue());
                        }
                    }
                }

                HashMap<String, List<String>> inputVariables = new HashMap<String, List<String>>();
                for(Wire wire : wires){

                    List<String> input;
                    if(!inputVariables.containsKey(wire.getTo_connector())) {
                        input = new ArrayList<String>();
                        inputVariables.put(wire.getTo_connector(), input);
                    }
                    else {
                        input = inputVariables.get(wire.getTo_connector());
                    }

                    input.add(String.format("%s/%s", wire.getFrom_node(), wire.getFrom_connector()));
                }

                // todo: string builders

                inputs = "";
                for(HashMap.Entry<String, List<String>> variables : inputVariables.entrySet()){
                    if (!inputs.isEmpty())
                        inputs += ", ";

                    inputs += String.format("'%s': [", variables.getKey());

                    List<String> leadingBlocks = variables.getValue();
                    for(int i = 0; i < leadingBlocks.size(); i++){
                        if (i > 0)
                            inputs += ", ";

                        inputs += String.format("'%s'", leadingBlocks.get(i));
                    }
                    inputs += "]";
                }
            }

            private String adaptValueToStringArray(Object value) {

                if (value.toString().equals("[]")) {
                    return value.toString();
                }

                String[] items = value.toString().replaceAll("\\[|\\]", "").split(", ");

                StringBuilder sb = new StringBuilder();
                sb.append("[");

                for (int idx = 0; idx < items.length; idx++) {

                    if (idx > 0) {
                        sb.append(", ");
                    }

                    sb.append(String.format("'%s'", items[idx]));
                }

                sb.append("]");

                return sb.toString();
            }
        }

        private String createVariableNameFromUniqueName(String uniqueName) {
            StringBuilder variableName = new StringBuilder();
            variableName.append('_');
            variableName.append(uniqueName.replaceAll("-", "_"));
            return variableName.toString();
        }
    }
}
