package BMAA;

import com.google.common.base.Stopwatch;
import dataStructures.graph.Node;

import java.util.List;

public class Controller {

    private List<Agent> agents;

    private static int time = 0;
    private final boolean ALLOWED_PUSH;

    public Controller(List<Agent> agents, boolean push) {
        this.agents = agents;
        this.ALLOWED_PUSH = push;
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
    public void run() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        while (true) {

            boolean complete = true;
            for (Agent agent : agents) {
                if (!agent.atGoal()) {
                    complete = false;
                }
            }
            if (complete) {
                stopwatch.stop();
                return;
            }

            for (Agent agent : agents) {
                agent.searchPhase();
            }

            for (Agent agent : agents) {
                if (agent.nextNodeIsDefined()) {
                    Node n = agent.getNextNode();

                    if (ALLOWED_PUSH && n.isOccupied() && n.getAgent().atGoal()) {
                        n.getAgent().push();
                    }

                    if (!n.isOccupied()) {
                        agent.moveToNextOnPath();
                    }
                }
            }

            time += 1;
        }
    }

    // ------------------------------------------------------------------------------------------

    public static int getTime() {
        return time;
    }
}
