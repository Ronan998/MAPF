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
        npcController();
        return collectResults();
    }

    private Result collectResults() {
        double completionRate = Util.completionRate(this.agents);
        return new Result(completionRate);
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
        long startTime = System.currentTimeMillis();
        long limit = startTime + (30 * 1000);

        while (System.currentTimeMillis() < limit) {

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

        long elapsedTime = System.currentTimeMillis() - startTime;
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
}
