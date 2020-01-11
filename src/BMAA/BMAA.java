package BMAA;

import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.util.ArrayList;
import java.util.List;

public class BMAA extends Algorithm{

    private final int EXPANSIONS;
    private final int VISION;
    private final int MOVES;
    private final boolean PUSH;
    private final boolean FLOW;
    //private final int LIMIT;

    private Graph graph;
    private List<Agent> agents;
    private Controller controller;

    public BMAA(Graph graph, List<Node> s, List<Node> t,
            int expansions, int vision, int moves, boolean push, boolean flow) {
        this.EXPANSIONS = expansions;
        this.VISION = vision;
        this.MOVES = moves;
        this.PUSH = push;
        this.FLOW = flow;

        this.graph = graph;
        createAgents(graph, s, t, expansions, vision, moves, push, flow);
        this.controller = new Controller(this.agents, push);
    }

    public void execute() {
        controller.run();
    }

    private void createAgents(Graph graph, List<Node> s, List<Node> t,
                                     int expansions, int vision, int moves, boolean push, boolean flow) {
        agents = new ArrayList<>();
        for (int i=0; i<s.size(); i++) {
           agents.add(new Agent(graph, s.get(i), t.get(i), expansions, vision, moves, push, flow));
        }
    }

    //TODO Finish this implementation
    @Override
    Result generateResults() {
        double completionRate =
                agents.stream()
                .filter(agent -> agent.atGoal())
                .count() / (double) agents.size();

        return new Result(completionRate, );
    }

}
