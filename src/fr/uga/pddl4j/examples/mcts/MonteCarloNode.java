package fr.uga.pddl4j.examples.mcts;

import fr.uga.pddl4j.problem.State;
import java.util.ArrayList;
import java.util.List;

public class MonteCarloNode extends State {
    private MonteCarloNode parent;
    private List<MonteCarloNode> children;
    private int visits;
    private double wins;
    private int action;
    
    public MonteCarloNode(State state) {
        super(state);
        this.children = new ArrayList<>();
        this.visits = 0;
        this.wins = 0.0;
        this.action = -1;
    }

    public MonteCarloNode(State state, MonteCarloNode parent, int action) {
        super(state);
        this.parent = parent;
        this.children = new ArrayList<>();
        this.visits = 0;
        this.wins = 0.0;
        this.action = action;
    }

    public void addChild(MonteCarloNode child) {
        children.add(child);
    }

    public List<MonteCarloNode> getChildren() {
        return children;
    }

    public int getVisits() {
        return visits;
    }

    public double getWins() {
        return wins;
    }

    public MonteCarloNode getParent() {
        return parent;
    }

    public void updateStats(double score) {
        this.visits++;
        this.wins += score;
    }

    public MonteCarloNode selectPromisingNode() {
        MonteCarloNode selected = null;
        double bestValue = Double.MIN_VALUE;
        for (MonteCarloNode child : children) {
            double ucbValue = calculateUCB1(child);
            if (ucbValue > bestValue) {
                bestValue = ucbValue;
                selected = child;
            }
        }
        return selected;
    }

    private double calculateUCB1(MonteCarloNode child) {
        double explorationParameter = 1.41;
        if (child.visits == 0) {
            return Double.MAX_VALUE;
        }
        double exploitation = child.wins / child.visits;
        double exploration = Math.sqrt(Math.log(this.visits) / child.visits);
        return exploitation + explorationParameter * exploration;
    }

    public void addVisits() {
        this.visits++;
    }

    public void addWins(int result) {
        this.wins += result;
    }

    public void setAction(int indexOf) {
        this.action = indexOf;
    }

    public int getAction() {
        return action;
    }
}
