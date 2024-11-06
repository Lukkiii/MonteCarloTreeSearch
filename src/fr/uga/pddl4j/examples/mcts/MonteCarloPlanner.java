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
@CommandLine.Command(name = "MCTS",
    version = "MCTS 1.0",
    description = "Solves a specified planning problem using MCTS with pure random walks.",
    sortOptions = false,
    mixinStandardHelpOptions = true,
    headerHeading = "Usage:%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n")

public class MonteCarloPlanner extends AbstractPlanner {

    private static final Logger LOGGER = LogManager.getLogger(MonteCarloPlanner.class.getName());

    public Plan mrw(Problem problem) throws ProblemNotSupportedException  {
        
        // We get the initial state from the planning problem
        final State init = new State(problem.getInitialState());
        MonteCarloNode rootNode = new MonteCarloNode(init);
        Plan plan = null;

        int maxIterations = 1000;
        int maxDepth = 100;

        for (int i = 0; i < maxIterations; i++) {
            // 1. Selection
            MonteCarloNode nodeToExplore = rootNode;
            
            while (!nodeToExplore.getChildren().isEmpty()) {
                nodeToExplore = nodeToExplore.selectPromisingNode();
            }

            // 2. Expansion
            if (!nodeToExplore.satisfy(problem.getGoal())) {
                expandNode(nodeToExplore, problem);
            } else {
                System.out.println("Goal reached");
                plan = extractPlan(nodeToExplore, problem);
                break;
            }

            // 3. Simulation
            int simulationResult = simulateRandomPlayout(nodeToExplore, problem, maxDepth);

            // 4. Backpropagation
            backpropagate(nodeToExplore, simulationResult);
        }

        return plan;
    }

    private void expandNode(MonteCarloNode node, Problem problem) {
        for (int i = 0; i < problem.getActions().size(); i++) {
            // We get the actions of the problem
            Action a = problem.getActions().get(i);
            // If the action is applicable in the current node
            if (a.isApplicable(node)) {
                MonteCarloNode next = new MonteCarloNode(node);
                // We apply the effect of the action
                final List<ConditionalEffect> effects = a.getConditionalEffects();
                for (ConditionalEffect ce : effects) {
                    if (node.satisfy(ce.getCondition())) {
                        next.apply(ce.getEffect());
                    }
                }
                // We set the new child node information
                next.setAction(i);
                
                // We add the new child node to the current node
                node.addChild(next);
            }
        };
    }

    private int simulateRandomPlayout(MonteCarloNode node, Problem problem, int maxDepth) {
        
        int depth = 0;

        while (!node.satisfy(problem.getGoal()) && depth < maxDepth) {
            List<Action> actions = problem.getActions();
            if (actions.isEmpty()) break;
            Action action = actions.get(new Random().nextInt(actions.size()));
            MonteCarloNode next = new MonteCarloNode(node);
            final List<ConditionalEffect> effects = action.getConditionalEffects();
                for (ConditionalEffect ce : effects) {
                    if (node.satisfy(ce.getCondition())) {
                        next.apply(ce.getEffect());
                    }
                }
            depth++;
        }
        return depth;
    }


    private void backpropagate(MonteCarloNode node, int result) {
        MonteCarloNode currentNode = node;
        while (currentNode != null) {
            currentNode.addVisits();
            currentNode.addWins(result);
            currentNode = currentNode.getParent();
        }
    }


    private Plan extractPlan(MonteCarloNode node, Problem problem) {
        if (node == null) {
            return new SequentialPlan();
        }

        MonteCarloNode current = node;
        Plan plan = new SequentialPlan();
        while (current != null && current.getAction() != -1) {
            Action action = problem.getActions().get(current.getAction());
            plan.add(0, action);
            current = current.getParent();
        }
        return plan;
    }


    /**
     * Instantiates the planning problem from a parsed problem.
     *
     * @param problem the problem to instantiate.
     * @return the instantiated planning problem or null if the problem cannot be instantiated.
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
