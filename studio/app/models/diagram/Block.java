package models.diagram;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB schema for Diagram Block.
 */
public class Block {
    /**
     * Block constructor.
     * @param name name of the block
     * @param definition the name of the definition that the block is based on
     */
    public Block(String name, String definition) {
        this.name = name;
        this.definition = definition;
        this.inputConnectors = new ArrayList<Connector>();
        this.outputConnectors = new ArrayList<Connector>();
        this.parameters = new ArrayList<Parameter>();
    }

    /**
     * Returns the name of this Block.
     * @return block name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Block.
     * @param name block name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the definition that this Block is based on.
     * @return block definition name
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * Sets the name of the definition that this Block is based on.
     * @param definition block definition name
     */
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    /**
     * Returns the state of this Block.
     * @return block state
     */
    public int getState() {
        return state;
    }

    /**
     * Sets the state of this Block.
     * @param state block state
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * Sets the x position of this Block in Diagram.
     * @return x position of block in diagram
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the x position of this Block in Diagram.
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y position of this block in Diagram.
     * @return y position of block in diagram
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the y position of this Block in Diagram.
     * @param y y position of block in diagram
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Returns the width of this Block.
     * @return block width
     */
    public int getW() {
        return w;
    }

    /**
     * Sets the width of this Block.
     * @param w block width
     */
    public void setW(int w) {
        this.w = w;
    }

    /**
     * Returns a list of input Connectors tha belong to this Block.
     * @return list of connectors
     */
    public List<Connector> getInputConnectors() {
        return inputConnectors;
    }

    /**
     * Sets the list of input Connectors that belong to this Block.
     * @param inputConnectors list of connectors
     */
    public void setInputConnectors(List<Connector> inputConnectors) {
        this.inputConnectors = inputConnectors;
    }

    /**
     * Returns a list of output Connectors that belong to this Block.
     * @return list of connectors
     */
    public List<Connector> getOutputConnectors() {
        return outputConnectors;
    }

    /**
     * Sets ths list of output Connectors that belong to this Block.
     * @param outputConnectors list of connectors
     */
    public void setOutputConnectors(List<Connector> outputConnectors) {
        this.outputConnectors = outputConnectors;
    }

    /**
     * Returns the list of Parameters that belong to this Block.
     * @return list of parameters
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Sets the list of Parameters that belong to this Block.
     * @param parameters list of parameters
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Hidden Jackson constructor.
     */
    private Block() { }

    /**
     * Private members.
     */
    private String name;
    private String definition;
    private int state;
    private int x;
    private int y;
    private int w;
    private List<Connector> inputConnectors;
    private List<Connector> outputConnectors;
    private List<Parameter> parameters;
}