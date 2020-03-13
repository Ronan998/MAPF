package Algorithm.Bmaa;

import Algorithm.Time;
import Benchmark.Result;
import DataStructures.graph.Graph;
import DataStructures.graph.Node;
import Error.NoAgentAtGoalException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Bmaa{

    public static final int DEFAULT_EXPANSIONS = 32;
    public static final double DEFAULT_VISION = Math.sqrt(2);
    public static final int DEFAULT_MOVES = 32;


    private final int EXPANSIONS;
    private final double VISION;
    private final int MOVES;
    private final boolean PUSH;
    private final boolean FLOW;

    private Graph graph;
    private List<BmaaAgent> agents;

    private Time time = new Time();

    /**
     * The time limit imposed on runtime of the algorithm, in milliSeconds
     */
    private long timeLimit;

    public Bmaa(Graph graph, List<Node> s, List<Node> t,
                int expansions, double vision, int moves, boolean push, boolean flow) {
        this.EXPANSIONS = expansions;
        this.VISION = vision;
        this.MOVES = moves;
        this.PUSH = push;
        this.FLOW = flow;

        this.graph = graph;
        createAgents(graph, s, t, expansions, vision, moves, push, flow);
    }

    public Result runWithTimeLimit(Duration timeLimit) {
        this.timeLimit = timeLimit.toMillis();

        while (!allAgentsAtGoals() && underTimeLimit()) {
            time.startStopWatch();
            npcController();
            time.stopStopWatch();
        }
        return collectResults();
    }

    /**
     * Run the BMAA algorithm, but at given stop times pause the execution and gather
     * current statistics at that time.
     * @param stopTimes integer values which indicate the time in milli seconds at which to pause execution
     *                  gather statistics about the progress of the run.
     * @return a mapping of the stopping time to the results of the algorithm at that time.
     */
    public List<Result> runWithMultipleTimeLimits(List<Integer> stopTimes) {
        List<Result> results = new ArrayList<>();

        for (int stopTime : stopTimes) {
            this.timeLimit = stopTime;
            time.startStopWatch();
            while (!allAgentsAtGoals() && underTimeLimit()) {
                npcController();
            }
            time.stopStopWatch();
            try {
                results.add(collectResults());
            } catch (NoAgentAtGoalException e) {
                e.printStackTrace();
                System.out.println("Skipping " + stopTime + "ms timelimit");
            }
        }
        return results;
    }

    private Result collectResults() {

        // Completion Rate
        int agentsAtGoal = 0;
        for (BmaaAgent agent : agents) {
            if (agent.atGoalBeforeTimeLimit(timeLimit)) {
                agentsAtGoal += 1;
            }
        }

        double completionRate = (double) agentsAtGoal / (double) agents.size();

        // Completion Time (Seconds)
        double averageCompletionTimeSeconds =
                agents.stream()
                        .mapToDouble(agent -> {
                            Long completionTimeSeconds = agent.getCompletionTimeSeconds(timeLimit);
                            if (completionTimeSeconds == null) {
                                return timeLimit;
                            }
                            else {
                                return completionTimeSeconds;
                            }
                        })
                        .average()
                        .orElseThrow(RuntimeException::new);

        // Completion Time (Time steps)
        int max =
                agents.stream()
                        .filter(agent -> agent.getCompletionTimeSteps(timeLimit) != null)
                        .mapToInt(agent -> agent.getCompletionTimeSteps(timeLimit))
                        .max()
                        .orElseThrow(() -> {
                            System.out.println("Error: Completion rate: " + completionRate);
                            return new NoAgentAtGoalException("For " + this.agents.size() + "agents with time limit of " + timeLimit);
                        });

        double averageCompletionTimeSteps =
                agents.stream()
                        .mapToInt(agent -> {
                            if (agent.getCompletionTimeSteps(timeLimit) == null) {
                                return max;
                            }
                            else {
                                return agent.getCompletionTimeSteps(timeLimit);
                            }
                        })
                        .average()
                        .orElseThrow(RuntimeException::new);

        // Travel distances
        double averageTravelDistance =
                agents.stream()
                        .mapToDouble(agent -> agent.getTravelDistance(timeLimit))
                        .average()
                        .orElseThrow(RuntimeException::new);

        return new Result(
                (int) timeLimit,
                agents.size(),
                completionRate,
                averageCompletionTimeSeconds,
                averageCompletionTimeSteps,
                averageTravelDistance);
    }

    // ------------------------------------------------------------------------------------------

    private void npcController() {
        for (BmaaAgent agent : agents) {
            agent.searchPhase();
        }

        for (BmaaAgent agent : agents) {
            if (agent.nextNodeIsDefined()) {
                Node n = agent.getNextNode();

                if (PUSH && n.isOccupied() && n.getAgent().atGoal()) {
                    n.getAgent().push();
                }

                if (!n.isOccupied()) {
                    agent.moveToNextOnPath();
                }
            }
        }

        time.incrementTimeStep();
    }

    // ------------------------------------------------------------------------------------------

    private void createAgents(Graph graph, List<Node> s, List<Node> t,
                              int expansions, double vision, int moves, boolean push, boolean flow) {
        agents = new ArrayList<>();
        for (int i=0; i<s.size(); i++) {
            agents.add(new BmaaAgent(graph, s.get(i), t.get(i), expansions, vision, moves, time));
        }
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Returns true if all agents are at their goals, false otherwise.
     * @return a boolean indicating if all agents are at their goals
     */
    private boolean allAgentsAtGoals() {
        return agents.stream()
                .allMatch(agent -> agent.atGoalBeforeTimeLimit(timeLimit));
    }

    /**
     * Return true if the current execution time indicated by the stopwatch is under the time limit.
     * @return a boolean indicating if execution time is under the time limit
     */
    private boolean underTimeLimit() {
        return time.milliSecondsElapsed() < timeLimit;
    }
}