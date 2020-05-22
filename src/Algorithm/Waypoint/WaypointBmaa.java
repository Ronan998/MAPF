package Algorithm.Waypoint;

import Algorithm.Time;
import Algorithm.Util;
import Benchmark.Benchmark;
import Benchmark.ProblemMap;
import Benchmark.ProblemSet;
import Benchmark.Result;
import Error.NoAgentAtGoalException;
import DataStructures.graph.Graph;
import DataStructures.graph.Node;
import com.google.common.base.Stopwatch;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WaypointBmaa {

    public static final int DEFAULT_EXPANSIONS = 32;
    public static final double DEFAULT_VISION = Math.sqrt(2);
    public static final int DEFAULT_MOVES = 32;


    private final int EXPANSIONS;
    private final double VISION;
    private final int MOVES;
    private final boolean PUSH;
    private final boolean FLOW;

    private Graph graph;
    private List<WaypointAgent> agents;

    private Time time = new Time();

    /**
     * The time limit imposed on runtime of the algorithm, in milliSeconds
     */
    private long timeLimit;

    public WaypointBmaa(Graph graph, List<Node> s, List<Node> t,
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

        for (WaypointAgent a : agents) a.init();
        System.out.println("Finished computing full paths and decomposing");
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

        for (WaypointAgent agent : agents) agent.init();

        for (int stopTime : stopTimes) {
            this.timeLimit = stopTime;
            while (!allAgentsAtGoals() && underTimeLimit()) {
                time.startStopWatch();
                npcController();
                time.stopStopWatch();
            }
            try {
                results.add(collectResults());
            } catch (NoAgentAtGoalException e) {
                e.printStackTrace();
                System.out.println("Skipping " + stopTime + "ms timelimit");
            }
        }
        return results;
    }

    public List<Result> runWithMultipleTimeLimitsWithMovementCost(List<Integer> stopTimes) {
        List<Result> results = new ArrayList<>();

        for (WaypointAgent agent : agents) agent.init();

        for (int stopTime : stopTimes) {
            this.timeLimit = stopTime;
            while (!allAgentsAtGoals() && underTimeLimit()) {
                time.startStopWatch();
                npcControllerWithMovementCost();
                time.stopStopWatch();
            }
            try {
                results.add(collectResults());
            } catch (NoAgentAtGoalException e) {
                e.printStackTrace();
                System.out.println("Skipping " + stopTime + "ms timelimit");
            }
        }
        return results;
    }

    /**
     * Run the algorithm in the same way as  {@link #runWithMultipleTimeLimits(List)} but include
     * the time it takes to build full paths in the timing of the overall algorithm.
     */
    public List<Result> runWithMultipleTimeLimitsWithFulPathConstruction(List<Integer> stopTimes) {
        List<Result> results = new ArrayList<>();

        time.startStopWatch();
        for (WaypointAgent agent : agents) agent.init();
        time.stopStopWatch();

        for (int stopTime : stopTimes) {
            this.timeLimit = stopTime;
            while (!allAgentsAtGoals() && underTimeLimit()) {
                time.startStopWatch();
                npcController();
                time.stopStopWatch();
            }
            try {
                results.add(collectResults());
            } catch (NoAgentAtGoalException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public List<Result> runWithMultipleTimeLimitsWithFulPathConstructionWithMovementCost(List<Integer> stopTimes) {
        List<Result> results = new ArrayList<>();

        time.startStopWatch();
        for (WaypointAgent agent : agents) agent.init();
        time.stopStopWatch();

        for (int stopTime : stopTimes) {
            this.timeLimit = stopTime;
            while (!allAgentsAtGoals() && underTimeLimit()) {
                time.startStopWatch();
                npcControllerWithMovementCost();
                time.stopStopWatch();
            }
            try {
                results.add(collectResults());
            } catch (NoAgentAtGoalException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    private Result collectResults() {

        // Completion Rate
        int agentsAtGoal = 0;
        for (WaypointAgent agent : agents) {
            if (agent.atGoalBeforeTimeLimit(timeLimit)) {
                agentsAtGoal += 1;
            }
        }

        double completionRate = (double) agentsAtGoal / (double) agents.size();

        if (completionRate == 0.00) {
            return new Result.Builder()
                    .timeLimit((int) timeLimit)
                    .numberOfAgents(agents.size())
                    .completionRate(completionRate)
                    .build();
        }

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
                .orElse(Double.NaN);

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
                .orElse(Double.NaN);

        // Travel distances
        double averageTravelDistance =
                agents.stream()
                .mapToDouble(agent -> agent.getTravelDistance(timeLimit))
                .average()
                .orElse(Double.NaN);

        return new Result.Builder()
                .timeLimit((int) timeLimit)
                .numberOfAgents(agents.size())
                .completionRate(Double.isNaN(completionRate) ? null : completionRate)
                .averageCompletiontimeSeconds(Double.isNaN(averageCompletionTimeSeconds) ? null : averageCompletionTimeSeconds)
                .averageCompletionTimeSteps(Double.isNaN(averageCompletionTimeSteps) ? null : averageCompletionTimeSteps)
                .averageTravelDistance(Double.isNaN(averageTravelDistance) ? null : averageTravelDistance).build();
    }

    // ------------------------------------------------------------------------------------------

    private void npcController() {
        for (WaypointAgent agent : agents) {
            agent.searchPhase();
        }

        for (WaypointAgent agent : agents) {
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

    private void npcControllerWithMovementCost() {
        for (WaypointAgent agent : agents) {
            agent.searchPhase();
        }

        for (WaypointAgent agent : agents) {
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

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        time.incrementTimeStep();
    }
    // ------------------------------------------------------------------------------------------

    private void createAgents(Graph graph, List<Node> s, List<Node> t,
                                     int expansions, double vision, int moves, boolean push, boolean flow) {
        agents = new ArrayList<>();
        for (int i=0; i<s.size(); i++) {
           agents.add(new WaypointAgent(graph, s.get(i), t.get(i), expansions, vision, moves, time, 10*Math.sqrt(2)));
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
