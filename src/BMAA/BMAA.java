package BMAA;

import Benchmark.Result;
import Visualisation.Visualisation;
import dataStructures.graph.Graph;
import dataStructures.graph.Node;
import Error.NoAgentAtGoalException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BMAA{

    public static final int DEFAULT_EXPANSIONS = 32;
    public static final double DEFAULT_VISION = Math.sqrt(2);
    public static final int DEFAULT_MOVES = 32;


    private final int EXPANSIONS;
    private final double VISION;
    private final int MOVES;
    private final boolean PUSH;
    private final boolean FLOW;

    private Graph graph;
    private List<Agent> agents;

    public Time time = new Time();

    /**
     * The time limit imposed on runtime of the algorithm, in milliSeconds
     */
    private long timeLimit;

    public BMAA(Graph graph, List<Node> s, List<Node> t,
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

        for (Agent a : agents) a.init();
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

        for (Agent agent : agents) agent.init();

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
        for (Agent agent : agents) {
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

    /**
     * Implementation of BMAA* NPC Controller algorithm.
     * The controller first invokes every agents {@code agent.searchPhase}
     * algorithm, allowing it to compute their prefix paths if needed.
     * Then the controller iterates through each agent, first getting the node
     * {@code n} to which they should next move to. If the desired node is
     * blocked by an agent that has reached its goal node already and
     * {@code ALLOWED_PUSH} is set to true, the agent can push the other agent
     * off its goal node.
     * Finally if the node is clear of any obstruction, the algorithm moves the
     * agent to its desired node.
     *
     */
//    public void npcController() {
//        startTime = System.currentTimeMillis();
//        //timeLimit = startTime + (30 * 1000);
//
//        timeLimit = 5;
//
//        Stopwatch stopwatch = Stopwatch.createStarted();
//
//        while (stopwatch.elapsed(TimeUnit.SECONDS) <= 30) {
//
//            if (stopwatch.elapsed(TimeUnit.SECONDS) >= timeLimit) {
//                stopwatch.stop();
//                // Collect results
//                Result result = collectResults();
//                System.out.println(result);
//                // Update time limit
//                timeLimit += 5;
//                stopwatch.start();
//            }
//
//            boolean complete = true;
//            for (Agent agent : agents) {
//                if (!agent.atGoal()) {
//                    complete = false;
//                }
//            }
//            if (complete) {
//                break;
//            }
//
//            for (Agent agent : agents) {
//                agent.searchPhase();
//            }
//
//            for (Agent agent : agents) {
//                if (agent.nextNodeIsDefined()) {
//                    Node n = agent.getNextNode();
//
//                    if (PUSH && n.isOccupied() && n.getAgent().atGoal()) {
//                        n.getAgent().push();
//                    }
//
//                    if (!n.isOccupied()) {
//                        agent.moveToNextOnPath();
//                    }
//                }
//            }
//
//            time.increment();
//        }
//
//        stopwatch.stop();
//        Long timeTaken = stopwatch.elapsed(TimeUnit.SECONDS);
//    }

    private void npcController() {
        for (Agent agent : agents) {
            agent.searchPhase();
        }

        for (Agent agent : agents) {
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
           agents.add(new Agent(graph, s.get(i), t.get(i), expansions, vision, moves, time, 10*Math.sqrt(2)));
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

    /**
     * Get the time at which the execution of the algorithm should halt.
     * Due to limitation in the java language, it does not seem possible to enforce this time limit
     * such that the algorithm terminates exactly upon passing it, but we stop as soon as we can with
     * regular polling of the current time.
     * @return the time at which the algorithm should terminate
     */
    public long getTimeLimit() {
        return timeLimit;
    }
}
