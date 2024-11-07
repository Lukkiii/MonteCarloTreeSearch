package fr.uga.pddl4j.examples.mcts;

import fr.uga.pddl4j.problem.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class MonteCarloNode extends State {
    private MonteCarloNode parent;
    private List<MonteCarloNode> children;
    private int visits;
    private int wins;
    private boolean terminal;
    private final int action; // Action that led to this node

    // Constructor for the root node
    public MonteCarloNode(State state) {
        super(state);
        this.children = new ArrayList<>();
        this.visits = 0;
        this.wins = 0;
        this.terminal = false;
        this.action = -1;
    }

    // Constructor for the child nodes
    public MonteCarloNode(State state, MonteCarloNode parent, int action) {
        super(state);
        this.parent = parent;
        this.children = new ArrayList<>();
        this.visits = 0;
        this.wins = 0;
        this.terminal = false;
        this.action = action;
    }

    // check if the node is terminal
    public boolean isTerminal() {
        return terminal;
    }

    // Setter for terminal
    public void setTerminal(boolean isTerminal) {
        this.terminal = isTerminal;
    }

    // check if the node is a leaf
    public boolean isLeaf() {
        return children.isEmpty();
    }

    // Select the most promising node
    public MonteCarloNode selectPromisingNode() {
        return children.stream()
                .max(Comparator.comparingDouble(this::uctValue))
                .orElse(null);
    }

    // Calculate the UCT value
    private double uctValue(MonteCarloNode node) {
        double explorationParameter = Math.sqrt(3);
        double exploitation = (double) node.wins / (node.visits + 1e-6);
        double exploration = Math.sqrt(Math.log(this.visits + 1) / (node.visits + 1e-6));
        return exploitation + explorationParameter * exploration;
    }

    // Add a child node
    public void addChild(MonteCarloNode child) {
        children.add(child);
    }

    // Add visits and wins
    public void addVisits() {
        this.visits++;
    }

    public void addWins(int result) {
        this.wins += result;
    }

    // Getters
    public int getVisits() {
        return visits;
    }

    public int getWins() {
        return wins;
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
        return action;
    }
}
