package Benchmark;

import dataStructures.PriorityQueue;
import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Scenario {

    private final File file;

    private Graph graph;
    private List<Node> s;
    private List<Node> t;

    private Integer agentLimit;

    public Scenario(String file, Graph graph) {
        this.file = new File(file);
        this.graph = graph;
        process();
    }

    public int getAgentLimit() {
        return agentLimit;
    }

    public void setAgentLimit(int agentLimit) {
        this.agentLimit = agentLimit;
    }

    public List<Node> getS() {
        if (agentLimit != null) {
            return this.s.subList(0, agentLimit);
        }
        return this.s;
    }

    public List<Node> getT() {
        if (agentLimit != null) {
            return this.t.subList(0, agentLimit);
        }
        return this.t;
    }

    public int numAgents() {
        return this.s.size();
    }

    // ---------------------------------------------

    private void process() {
        try {
            this.s = Files.lines(file.toPath())
                    .filter(line -> !line.startsWith("version"))
                    .map(line -> line.split("\t"))
                    .map(line -> graph.getNodeByCoords(Integer.parseInt(line[4]), Integer.parseInt(line[5])))
                    .collect(Collectors.toList());

            this.t = Files.lines(file.toPath())
                    .filter(line -> !line.startsWith("version"))
                    .map(line -> line.split("\t"))
                    .map(line -> graph.getNodeByCoords(Integer.parseInt(line[6]), Integer.parseInt(line[7])))
                    .collect(Collectors.toList());
        }
        catch (Exception e) {}
    }

    // -----------------------------------------------------------------------------------------

    public boolean allPathsExist() {
        for (int i=0; i < this.s.size(); i++) {
            Node start = this.s.get(i);
            Node goal = this.t.get(i);

            if (pathExists(start, goal) == false) return false;

        }
        return true;
    }

    private boolean pathExists(Node start, Node end) {
        PriorityQueue<Node> open = new PriorityQueue<>();
        Set<Node> closed = new HashSet<>();
        int i = 0;
        open.put(start, i);

        while (!open.isEmpty()) {
            Node n = open.get();
            closed.add(n);

            if (n == end) {
                return true;
            }

            Set<Node> neighbours = graph.getNeigbours(n);
            for (Node neighbour : neighbours) {
                if  (!closed.contains(neighbour) && !open.contains(neighbour)) {
                    i += 1;
                    open.put(neighbour, i);
                }
            }
        }

        return false;
    }

}
