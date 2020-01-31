package BMAA;

import Benchmark.Result;
import Common.Util;
import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.util.ArrayList;
import java.util.List;

public class BMAA{

    private final int EXPANSIONS;
    private final double VISION;
    private final int MOVES;
    private final boolean PUSH;
    private final boolean FLOW;

    private Graph graph;
    private List<Agent> agents;

    public Time time = new Time();

    private long startTime;
    private long elapsedTime;
    private long endTime;
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

    public Result run() {
        startTime = System.currentTimeMillis();
        timeLimit = startTime + (30 * 1000);
        npcController();
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        return collectResults();

    }

    private Result collectResults() {

        // Completion Rate
        int agentsAtGoal = 0;
        for (Agent agent : agents) {
            if (agent.atGoal(timeLimit)) {
                agentsAtGoal += 1;
            }
        }

        double completionRate = (double) agentsAtGoal / (double) agents.size();

        // Completion Time (Seconds)
        double averageCompletionTimeSeconds =
                agents.stream()
                .mapToDouble(agent -> {
                    if (agent.atGoal()) {
                        // special case where agent started at its goal
                        if (agent.getLastTimeAtGoal() == null) {
                            return 0;
                        }
                        return Util.milliSecondsToSeconds(Util.elapsedTimeMillis(startTime, agent.getLastTimeAtGoal()));
                    }
                    else {
                        return 30;
                    }
                })
                .sum() / (double) agents.size();

        // Completion Time (Time steps)
        int max =
                agents.stream()
                .filter(agent -> agent.getCompletionTimeSteps() != null)
                .mapToInt(agent -> agent.getCompletionTimeSteps() + 1)
                .max()
                .orElseThrow(RuntimeException::new);

        double averageCompletionTimeSteps =
                agents.stream()
                .mapToInt(agent -> {
                    if (agent.getCompletionTimeSteps() == null) {
                        return max;
                    }
                    else {
                        return agent.getCompletionTimeSteps() + 1;
                    }
                })
                .average()
                .orElseThrow(RuntimeException::new);

        // Travel distances
        double averageTravelDistance =
                agents.stream()
                .mapToDouble(agent -> agent.getTravelDistance())
                .average()
                .orElseThrow(RuntimeException::new);

        return new Result(agents.size(),
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
    public void npcController() {
        startTime = System.currentTimeMillis();
        timeLimit = startTime + (30 * 1000);

        while (System.currentTimeMillis() < timeLimit) {

            boolean complete = true;
            for (Agent agent : agents) {
                if (!agent.atGoal()) {
                    complete = false;
                }
            }
            if (complete) {
                break;
            }

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

            time.increment();
        }

    }

    // ------------------------------------------------------------------------------------------

    private void createAgents(Graph graph, List<Node> s, List<Node> t,
                                     int expansions, double vision, int moves, boolean push, boolean flow) {
        agents = new ArrayList<>();
        for (int i=0; i<s.size(); i++) {
           agents.add(new Agent(graph, s.get(i), t.get(i), expansions, vision, moves, time));
        }
    }

    // ------------------------------------------------------------------------------------------


    /**
     * The time at which execution of the algorithm started
     * @return the time at which the algorithm started.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * The amount of time which elapsed during execution of the algorithm
     * @return the actual execution time of the algorithm
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Get the actual time at which the algorithm terminated.
     * @return the actual time which the algorithm terminated
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Get the System time at which the execution of the algorithm should halt.
     * Due to limitation in the java language, it does not seem possible to enforce this time limit
     * such that the algorithm terminates exactly upon passing it, but we stop as soon as we can with
     * regular polling of the current time.
     * @return the time at which the algorithm should terminate
     */
    public long getTimeLimit() {
        return timeLimit;
    }
}
