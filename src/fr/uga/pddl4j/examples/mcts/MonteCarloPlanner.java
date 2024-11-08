package fr.uga.pddl4j.examples.mcts;

import fr.uga.pddl4j.heuristics.state.StateHeuristic;
import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.parser.RequireKey;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.plan.SequentialPlan;
import fr.uga.pddl4j.planners.AbstractPlanner;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.planners.PlannerConfiguration;
import fr.uga.pddl4j.planners.ProblemNotSupportedException;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.Condition;

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
    public static final String NUM_WALK_SETTING = "NUM_WALK";
    public static final int DEFAULT_NUM_WALK = 2000;

    public static final String LENGTH_WALK_SETTING = "LENGTH_WALK";
    public static final int DEFAULT_LENGTH_WALK = 10;

    // public static final int DEFAULT_EXTENDING_PERIOD = 300;

    public static final String MAX_STEPS_SETTING = "MAX_STEPS";
    public static final int DEFAULT_MAX_STEPS = 7;

    // public static final String EXTENDING_RATE_SETTING = "EXTENDING_RATE";
    // public static final double DEFAULT_EXTENDING_RATE = 1.5;

    // public static final double ALPHA = 0.9;
    // public static final double acceptableProgressThreshold = 0.5;

    public static final String MAX_SEARCH_TIME_SETTING = "MAX_SEARCH_TIME";
    public static final long DEFAULT_MAX_SEARCH_TIME = 600L;
    /**
     * The HEURISTIC property used for planner configuration.
     */
    public static final String HEURISTIC_SETTING = "HEURISTIC";
    /**
     * The default value of the HEURISTIC property used for planner configuration.
     */
    public static final StateHeuristic.Name DEFAULT_HEURISTIC = StateHeuristic.Name.FAST_FORWARD;

    private int NUM_WALK;
    @CommandLine.Option(names = {"-n", "--num-walks"}, description = "Number of random walks to perform")
    public void setNumWalks(int numWalks) {
        if (numWalks <= 0) {
            throw new IllegalArgumentException("The number of walks must be greater than 0.");

        }
        this.NUM_WALK = numWalks;
    }
    public int getNumWalks() {
        return this.NUM_WALK;
    }

    private int LENGTH_WALK;
    @CommandLine.Option(names = {"-s", "--steps"}, description = "Maximum number of steps in a walk")
    public void setLengthWalk(int lengthWalk) {
        if (lengthWalk <= 0) {
            throw new IllegalArgumentException("The length of the walk must be greater than 0.");
        }
        this.LENGTH_WALK = lengthWalk;
    }
    public int getLengthWalk() {
        return this.LENGTH_WALK;
    }

    private int MAX_STEPS;
    @CommandLine.Option(names = {"-m", "--max-steps"}, description = "Maximum number of steps before starting to explore another branch of the MCTS")
    public void setMaxSteps(int maxSteps) {
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("The maximum number of steps must be greater than 0.");
        }
        this.MAX_STEPS = maxSteps;
    }
    public int getMaxSteps() {
        return this.MAX_STEPS;
    }

    private long MAX_SEARCH_TIME;
    @CommandLine.Option(names = {"-mT", "--max-search-time"}, description = "Maximum search time in seconds")
    public  void setMaxSearchTime(long maxSearchTime) {
        if (maxSearchTime <= 0) {
            throw new IllegalArgumentException("The maximum search time must be greater than 0.");
        }
        this.MAX_SEARCH_TIME = maxSearchTime;
    }
    public long getMaxSearchTime() {
        return this.MAX_SEARCH_TIME;
    }

    // private int EXTENDING_PERIOD;
    // @CommandLine.Option(names = {"-e", "--extending-period"}, description = "Extending period for the length of walks")
    // public void setExtendingPeriod(int extendingPeriod) {
    //     if (extendingPeriod <= 0) {
    //         throw new IllegalArgumentException("The extending period must be greater than 0.");
    //     }
    //     this.EXTENDING_PERIOD = extendingPeriod;
    // }
    // public int getExtendingPeriod() {
    //     return this.EXTENDING_PERIOD;
    // }
    
    // private double EXTENDING_RATE;
    // @CommandLine.Option(names = {"-r", "--extending-rate"}, description = "Extending rate for the length of walks")
    // public void setExtendingRate(double extendingRate) {
    //     if (extendingRate <= 0) {
    //         throw new IllegalArgumentException("The extending rate must be greater than 0.");
    //     }
    //     this.EXTENDING_RATE = extendingRate;
    // }
    // public double getExtendingRate() {
    //     return this.EXTENDING_RATE;
    // }

    private StateHeuristic.Name HEURISTIC;
    @CommandLine.Option(names = {"-h", "--heuristic"}, defaultValue = "FAST_FORWARD",
        description = "Set the heuristic : AJUSTED_SUM, AJUSTED_SUM2, AJUSTED_SUM2M, COMBO, "
            + "MAX, FAST_FORWARD SET_LEVEL, SUM, SUM_MUTEX (preset: FAST_FORWARD)")
    public void setHeuristic(StateHeuristic.Name heuristic) {
        this.HEURISTIC = heuristic;
    }
    public StateHeuristic.Name getH() {
        return this.HEURISTIC;
    }

    /**
     * Creates a new MCT search planner with the default configuration.
     */
    public MonteCarloPlanner() {
        this(MonteCarloPlanner.getDefaultConfiguration());
    }

    /**
     * Creates a new MCT search planner with a specified configuration.
     *
     * @param configuration the configuration of the planner.
     */
    public MonteCarloPlanner(final PlannerConfiguration configuration) {
        super();
        this.setConfiguration(configuration);
    }

    public Plan mrw(Problem problem) throws ProblemNotSupportedException {
        // Check if the problem is supported by the planner
        if (!this.isSupported(problem)) {
            throw new ProblemNotSupportedException("Problem not supported");
        }

        StateHeuristic h = StateHeuristic.getInstance(this.getH(), problem);
        
        // Initialize the root node
        final State init = new State(problem.getInitialState());
        final MonteCarloNode rootNode = new MonteCarloNode(init);

        // Get the goal of the problem.
        final Condition goal = problem.getGoal();
        // Set the heuristic value of the root node.
        rootNode.setHValue(h.estimate(init, goal));

        // Get the actions of the problem.
        final List<Action> actions = problem.getActions();
        MonteCarloNode currentState = rootNode; 
        double hmin = rootNode.getHValue();
        int numSteps = 0;

        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        long endTime = startTime + MAX_SEARCH_TIME * 1000L;

        while (!currentState.satisfy(goal) && (currentTime < endTime)) {

            if (numSteps >= getMaxSteps()) {
                currentState = rootNode;
                numSteps = 0;
            }
            
            currentState = simulatePureRandomWalk(currentState, actions, goal, h);

            if (currentState.getHValue() < hmin) {
                hmin = currentState.getHValue();
                numSteps = 0;
            } else {
                numSteps++;
            }

            currentTime = System.currentTimeMillis();
        }

        if(currentTime >= endTime) {
            LOGGER.info("Search time exceeded the maximum search time of " + getMaxSearchTime() + " seconds.\n");
            return null;
        }

        return extractPlan(currentState, problem);
    }
        

    // Simulation step: Perform a random walk from the given node.
    public MonteCarloNode simulatePureRandomWalk(MonteCarloNode node, List<Action> actions, Condition goal, StateHeuristic h) {

        MonteCarloNode currentNode = null;
        MonteCarloNode best = null;
        int numSteps = 0;
        double hmin = Double.POSITIVE_INFINITY;
        Random rand = new Random();

        while (numSteps < getLengthWalk()) {
            currentNode = node;
            int steps = 0;
            while (!currentNode.getState().satisfy(goal) && steps < getLengthWalk()) {
                List<Action> applicableActions = getApplicableActions(currentNode, actions);
                if (applicableActions.isEmpty()) {
                    break;
                }

                // Choose a random action
                Action action = applicableActions.get(rand.nextInt(applicableActions.size()));

                currentNode = new MonteCarloNode(currentNode.getState(), currentNode, actions.indexOf(action), currentNode.getHValue());
                currentNode.getState().apply(action.getConditionalEffects());
                currentNode.setHValue(h.estimate(currentNode.getState(), goal));

                if(currentNode.getState().satisfy(goal)) {
                    return currentNode;
                }

                steps++;
            }

            if (currentNode.getHValue() < hmin) {
                hmin = currentNode.getHValue();
                best = currentNode;
            }
            numSteps++;

        }

        return (best != null) ? best : currentNode;
    }

    private List<Action> getApplicableActions(State state, List<Action> actions) {
        List<Action> applicableActions = new ArrayList<>();
        for (Action action : actions) {
            if (action.isApplicable(state)) {
                applicableActions.add(action);
            }
        }
        return applicableActions;
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
    public boolean isSupported(Problem problem) {
        return (problem.getRequirements().contains(RequireKey.ACTION_COSTS)
            || problem.getRequirements().contains(RequireKey.CONSTRAINTS)
            || problem.getRequirements().contains(RequireKey.CONTINOUS_EFFECTS)
            || problem.getRequirements().contains(RequireKey.DERIVED_PREDICATES)
            || problem.getRequirements().contains(RequireKey.DURATIVE_ACTIONS)
            || problem.getRequirements().contains(RequireKey.DURATION_INEQUALITIES)
            || problem.getRequirements().contains(RequireKey.FLUENTS)
            || problem.getRequirements().contains(RequireKey.GOAL_UTILITIES)
            || problem.getRequirements().contains(RequireKey.METHOD_CONSTRAINTS)
            || problem.getRequirements().contains(RequireKey.NUMERIC_FLUENTS)
            || problem.getRequirements().contains(RequireKey.OBJECT_FLUENTS)
            || problem.getRequirements().contains(RequireKey.PREFERENCES)
            || problem.getRequirements().contains(RequireKey.TIMED_INITIAL_LITERALS)
            || problem.getRequirements().contains(RequireKey.HIERARCHY))
            ? false : true;
    }

    private static PlannerConfiguration getDefaultConfiguration() {
        PlannerConfiguration config = Planner.getDefaultConfiguration();
        config.setProperty(MonteCarloPlanner.HEURISTIC_SETTING, DEFAULT_HEURISTIC.toString());
        config.setProperty(MonteCarloPlanner.MAX_SEARCH_TIME_SETTING, DEFAULT_MAX_SEARCH_TIME);
        config.setProperty(MonteCarloPlanner.MAX_STEPS_SETTING, DEFAULT_MAX_STEPS);
        config.setProperty(MonteCarloPlanner.NUM_WALK_SETTING, DEFAULT_NUM_WALK);
        config.setProperty(MonteCarloPlanner.LENGTH_WALK_SETTING, DEFAULT_LENGTH_WALK);
        // config.setProperty(MonteCarloPlanner.EXTENDING_PERIOD, DEFAULT_EXTENDING_PERIOD);
        // config.setProperty(MonteCarloPlanner.EXTENDING_RATE, DEFAULT_EXTENDING_RATE);

        return config;
    }

    /**
    * Returns the configuration of the planner.
    *
    * @return the configuration of the planner.
    */
   @Override
   public PlannerConfiguration getConfiguration() {
       final PlannerConfiguration config = super.getConfiguration();
       config.setProperty(MonteCarloPlanner.HEURISTIC_SETTING, this.getH().toString());
       config.setProperty(MonteCarloPlanner.MAX_SEARCH_TIME_SETTING, this.getMaxSearchTime());
       config.setProperty(MonteCarloPlanner.MAX_STEPS_SETTING, this.getMaxSteps());
       config.setProperty(MonteCarloPlanner.NUM_WALK_SETTING, this.getNumWalks());
       config.setProperty(MonteCarloPlanner.LENGTH_WALK_SETTING, this.getLengthWalk());
       // config.setProperty(MonteCarloPlanner.EXTENDING_PERIOD, this.getExtendingPeriod());
       // config.setProperty(MonteCarloPlanner.EXTENDING_RATE, this.getExtendingRate());
       
       return config;
   }

   /**
    * Sets the configuration of the planner. If a planner setting is not defined in
    * the specified configuration, the setting is initialized with its default value.
    *
    * @param configuration the configuration to set.
    */
   @Override
   public void setConfiguration(final PlannerConfiguration configuration) {
       super.setConfiguration(configuration);
       
       if (configuration.getProperty(MonteCarloPlanner.HEURISTIC_SETTING) == null) {
           this.setHeuristic(MonteCarloPlanner.DEFAULT_HEURISTIC);
       } else {
           this.setHeuristic(StateHeuristic.Name.valueOf(configuration.getProperty(
            MonteCarloPlanner.HEURISTIC_SETTING)));
       }

       if(configuration.getProperty(MonteCarloPlanner.MAX_SEARCH_TIME_SETTING) == null) {
           this.setMaxSearchTime(MonteCarloPlanner.DEFAULT_MAX_SEARCH_TIME);
       } else {
           this.setMaxSearchTime(Long.parseLong(configuration.getProperty(MonteCarloPlanner.MAX_SEARCH_TIME_SETTING)));
       }

         if(configuration.getProperty(MonteCarloPlanner.MAX_STEPS_SETTING) == null) {
              this.setMaxSteps(MonteCarloPlanner.DEFAULT_MAX_STEPS);
         } else {
              this.setMaxSteps(Integer.parseInt(configuration.getProperty(MonteCarloPlanner.MAX_STEPS_SETTING)));
         }

        if(configuration.getProperty(MonteCarloPlanner.NUM_WALK_SETTING) == null) {
            this.setNumWalks(MonteCarloPlanner.DEFAULT_NUM_WALK);
        } else {
            this.setNumWalks(Integer.parseInt(configuration.getProperty(MonteCarloPlanner.NUM_WALK_SETTING)));
        }

        if(configuration.getProperty(MonteCarloPlanner.LENGTH_WALK_SETTING) == null) {
            this.setLengthWalk(MonteCarloPlanner.DEFAULT_LENGTH_WALK);
        } else {
            this.setLengthWalk(Integer.parseInt(configuration.getProperty(MonteCarloPlanner.LENGTH_WALK_SETTING)));
        }
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
