package fr.uga.pddl4j.examples.mcts;

import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.plan.SequentialPlan;
import fr.uga.pddl4j.planners.AbstractPlanner;
import fr.uga.pddl4j.planners.ProblemNotSupportedException;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.ConditionalEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

/**
 * The class is an example of implementing a Monte Carlo Search planner.
 * It uses pure random walks to explore the state space.
 *
 * @author
 * @version
 */
@CommandLine.Command(
    name = "MCTS", 
    version = "MCTS 1.0", 
    description = "Solves a specified planning problem using MCTS with pure random walks.", 
    sortOptions = false, mixinStandardHelpOptions = true, headerHeading = "Usage:%n", 
    synopsisHeading = "%n", descriptionHeading = "%nDescription:%n%n", 
    parameterListHeading = "%nParameters:%n", 
    optionListHeading = "%nOptions:%n")
public class MonteCarloPlanner extends AbstractPlanner {

    private static final Logger LOGGER = LogManager.getLogger(MonteCarloPlanner.class.getName());

    // Parameters for Pure RW MCTS
    // private static final double ALPHA = 0.9;

    @CommandLine.Option(names = {"-n", "--num-walks"}, description = "Number of random walks to perform")
    private int NUM_WALK = 2000;

    @CommandLine.Option(names = {"-s", "--steps"}, description = "Maximum number of steps in a walk")
    private int LENGTH_WALK = 300;

    @CommandLine.Option(names = {"-e", "--extending-period"}, description = "Extending period for the length of walks")
    private int EXTENDING_PERIOD = 300;
    
    @CommandLine.Option(names = {"-r", "--extending-rate"}, description = "Extending rate for the length of walks")
    private double EXTENDING_RATE = 1.5;

    //private int MAX_STEPS = 7;

    public Plan mrw(Problem problem) throws ProblemNotSupportedException {
        final State init = new State(problem.getInitialState());
        final MonteCarloNode rootNode = new MonteCarloNode(init);
        Plan plan = null;
        int currentLengthWalk = LENGTH_WALK;

        for (int i = 0; i < NUM_WALK; i++) {
            // Selection step
            MonteCarloNode nodeToExplore = selectPromisingNode(rootNode);
            
            if (!nodeToExplore.isTerminal() && !nodeToExplore.getState().satisfy(problem.getGoal())) {
                // Expand the node
                expandNode(nodeToExplore, problem);
            } else {
                System.out.println("Solution found after " + i + " iterations.");
                plan = extractPlan(nodeToExplore, problem);
                break;
            }

            // Simulate a random playout from the node
            int simulationResult = simulateRandomPlayout(nodeToExplore, problem, currentLengthWalk);
            
            // Backpropagate the result
            backpropagate(nodeToExplore, simulationResult);
            
            // Extend the length of walks periodically
            if (i % EXTENDING_PERIOD == 0 && i != 0) {
                currentLengthWalk = (int) (currentLengthWalk * EXTENDING_RATE);
            }
        }
        return plan;
    }


    // Selection step: Traverse the tree using UCT to find a promising node.
    private MonteCarloNode selectPromisingNode(MonteCarloNode rootNode) {
        MonteCarloNode node = rootNode;
        while (!node.isLeaf()) {
            node = node.selectPromisingNode(); // Use UCT to select the most promising node
        }
        return node;
    }

    // Expansion step: Generate all possible children from the given node.
    private void expandNode(MonteCarloNode node, Problem problem) {
        List<Action> actions = problem.getActions();
        State currentState = node.getState();
        int actionIndex = 0;

        for (Action action : actions) {
            if (action.isApplicable(currentState)) {
                State newState = applyActionAndGetNewState(currentState, action);
                MonteCarloNode newNode = new MonteCarloNode(newState, node, actionIndex);
                node.addChild(newNode);

                // Set terminal flag if the new node satisfies the goal
                if (newState.satisfy(problem.getGoal())) {
                    newNode.setTerminal(true);
                }
            }
            actionIndex++;
        }
    }

    // Simulation step: Perform a random playout from the given node.
    private int simulateRandomPlayout(MonteCarloNode node, Problem problem, int maxDepth) {
        State currentState = node.getState();
        int depth = 0;
        Random random = new Random();
        List<Action> applicableActions = new ArrayList<>();

        while (!currentState.satisfy(problem.getGoal()) && depth < maxDepth) {
            applicableActions.clear();

            for (Action action : problem.getActions()) {
                if (action.isApplicable(currentState)) {
                    applicableActions.add(action);
                }
            }
            if (applicableActions.isEmpty()) break;

            Action action = applicableActions.get(random.nextInt(applicableActions.size()));
            System.out.println("Action: " + action.getName());
            currentState = applyActionAndGetNewState(currentState, action);
            System.out.println("State: " + currentState);
            depth++;
        }

        // Return result based on whether the goal was achieved
        return currentState.satisfy(problem.getGoal()) ? 1 : 0;
    }

    // Apply an action to a state and return the resulting new state.
    private State applyActionAndGetNewState(State state, Action action) {
        State newState = new State(state);
        for (ConditionalEffect ce : action.getConditionalEffects()) {
            if (newState.satisfy(ce.getCondition())) {
                newState.apply(ce.getEffect());
            }
        }
        return newState;
    }

    // Backpropagation step: Update the visit and win counts in the node's ancestry.
    private void backpropagate(MonteCarloNode node, int result) {
        MonteCarloNode currentNode = node;
        while (currentNode != null) {
            currentNode.addVisits();
            currentNode.addWins(result);
            currentNode = currentNode.getParent();
        }
    }

    // Extract the plan that leads to the specified node.
    private Plan extractPlan(MonteCarloNode node, Problem problem) {
        Plan plan = new SequentialPlan();
        MonteCarloNode current = node;
        while (current != null && current.getParent() != null) {
            if (current.getAction() != -1) {
                Action action = problem.getActions().get(current.getAction());
                plan.add(0, action);
            }
            current = current.getParent();
        }
        return plan;
    }

    /**
     * Instantiates the planning problem from a parsed problem.
     *
     * @param problem the problem to instantiate.
     * @return the instantiated planning problem or null if the problem cannot be
     *         instantiated.
     */
    @Override
    public Problem instantiate(DefaultParsedProblem problem) {
        final Problem pb = new DefaultProblem(problem);
        pb.instantiate();
        return pb;
    }

    /**
     * Search a solution plan to a specified domain and problem using MRW.
     *
     * @param problem the problem to solve.
     * @return the plan found or null if no plan was found.
     * @throws ProblemNotSupportedException
     */
    @Override
    public Plan solve(final Problem problem) throws ProblemNotSupportedException {
        LOGGER.info("* Starting MonteCarlo search \n");
        // Search a solution
        final long begin = System.currentTimeMillis();
        final Plan plan = this.mrw(problem);
        final long end = System.currentTimeMillis();
        // If a plan is found update the statistics of the planner
        // and log search information
        if (plan != null) {
            LOGGER.info("* MonteCarloPlanner* search succeeded\n");
            this.getStatistics().setTimeToSearch(end - begin);
        } else {
            LOGGER.info("* MonteCarloPlanner search failed\n");
        }
        // Return the plan found or null if the search fails.
        return plan;
    }

    @Override
    public boolean isSupported(Problem arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isSupported'");
    }

    /**
     * The main method of the <code>MCTS</code> planner.
     *
     * @param args the arguments of the command line.
     */
    public static void main(String[] args) {
        try {
            final MonteCarloPlanner planner = new MonteCarloPlanner();
            CommandLine cmd = new CommandLine(planner);
            cmd.execute(args);
        } catch (IllegalArgumentException e) {
            LOGGER.fatal(e.getMessage());
        }
    }

}
