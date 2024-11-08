package fr.uga.pddl4j.examples.mcts;

import fr.uga.pddl4j.problem.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class MonteCarloNode extends State {
    
    private MonteCarloNode parent;
    private List<MonteCarloNode> children;
    private double hValue;
    private int actionIndex;
    private boolean isTerminal;

    // Constructor for the root node
    public MonteCarloNode(State state) {
        super(state);
        this.parent = null;
        this.children = new ArrayList<>();
        this.hValue = Double.POSITIVE_INFINITY;
        this.isTerminal = false;
    }

    // Constructor for the child nodes
    public MonteCarloNode(State state, MonteCarloNode parent, int actionIndex, double hValue) {
        super(state);
        this.parent = parent;
        this.actionIndex = actionIndex;
        this.hValue = hValue;
    }

    // Get the heuristic value
    public double getHValue() {
        return hValue;
    }

    public boolean isTerminal() {
       return isTerminal;
    }

    // check if the node is a leaf
    public boolean isLeaf() {
        return children.isEmpty();
    }

    // Add a child node
    public void addChild(MonteCarloNode child) {
        children.add(child);
    }

    public State getState() {
        return this;
    }

    public MonteCarloNode getParent() {
        return parent;
    }

    public List<MonteCarloNode> getChildren() {
        return children;
    }

    public int getAction() {
        return actionIndex;
    }

    public void setTerminal(boolean b) {
        isTerminal = b;
    }

    public void setHValue(double hMin) {
        hValue = hMin;
    }

    // Select the most promising node
    // public MonteCarloNode selectPromisingNode() {
    //     return children.stream()
    //             .max(Comparator.comparingDouble(this::uctValue))
    //             .orElse(null);
    // }

    // Calculate the UCT value
    // private double uctValue(MonteCarloNode node) {
    //     double explorationParameter = Math.sqrt(2);
    //     double exploitation = (double) node.wins / (node.visits + 1e-6);
    //     double exploration = Math.sqrt(Math.log(this.visits + 1) / (node.visits + 1e-6));
    //     return exploitation + explorationParameter * exploration;
    // }
}
