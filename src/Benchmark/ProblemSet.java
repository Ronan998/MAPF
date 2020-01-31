package Benchmark;

import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class ProblemSet {

    private Graph graph;
    private List<Node> s;
    private List<Node> t;

    public ProblemSet(Graph graph, List<Node> s, List<Node> t) {
        this.graph = graph;
        this.s = s;
        this.t = t;
    }

    /**
     * Return a problem set instance of with randomised lists of start nodes and target nodes
     * @param graph the graph for which to generate the problem set on
     * @param numAgents the amount of agents this problem set should have
     * @return the randomised problem set
     */
    public static ProblemSet randomProblemSet(Graph graph, int numAgents) {

        if (numAgents > (graph.numNodes() / 2)) {
            throw new RuntimeException("Error: Not enough graph space to create agents");
        }

        List<Node> s = new ArrayList<>();
        List<Node> t = new ArrayList<>();

        List<Node> nodes = new ArrayList<>(graph.nodes());

        for (int i = 0; i < numAgents; i++) {
            Random rand = new Random();

            Node start = nodes.get(rand.nextInt(nodes.size()));

            while (s.contains(start)) {
                start = nodes.get(rand.nextInt(nodes.size()));
            }

            Node target = nodes.get(rand.nextInt(nodes.size()));

            while (t.contains(target)) {
                target = nodes.get(rand.nextInt(nodes.size()));
            }

            while (!graph.pathExists(start, target)) {
                start = nodes.get(rand.nextInt(nodes.size()));

                while (s.contains(start)) {
                    start = nodes.get(rand.nextInt(nodes.size()));
                }

                target = nodes.get(rand.nextInt(nodes.size()));

                while (t.contains(target)) {
                    target = nodes.get(rand.nextInt(nodes.size()));
                }
            }

            s.add(start);
            t.add(target);
        }
        return new ProblemSet(graph, s, t);
    }

    // ---------------------------------------------------------------------------------------

    /**
     * Get the longest distance of any start,target pair in the problem set
     * @return the longest distance in the problem set
     */
    public double longestPathDistance() {
        return IntStream.range(0, s.size())
                .mapToDouble(index -> {
                    Node start = s.get(index);
                    Node target = t.get(index);
                    return start.octileDistance(target);
                })
                .max()
                .orElseThrow(RuntimeException::new);
    }

    /**
     * Get the average distance of all start,target pairs in the problem set
     * @return the average distance between all start, target pairs
     */
    public double averagePathDistance() {
        return IntStream.range(0, s.size())
                .mapToDouble(index -> {
                    Node start = s.get(index);
                    Node target = t.get(index);
                    return start.octileDistance(target);
                })
                .average()
                .orElseThrow(RuntimeException::new);
    }

    /**
     * Get the shortest distance between any start, target pair in the problem set
     * @return
     */
    public double shortestPathDistance() {
        return IntStream.range(0, s.size())
                .mapToDouble(index -> {
                    Node start = s.get(index);
                    Node target = t.get(index);
                    return start.octileDistance(target);
                })
                .min()
                .orElseThrow(RuntimeException::new);
    }

    /**
     * Get the number of start,target pairs in the problem set
     * @return the number of start, target pairs
     */
    public int numAgents() {
        return this.s.size();
    }

    /**
     * Get the graph which the problem set is for
     * @return the graph for which the problem set is for
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Get the list of starting nodes as defined by the problem set
     * @return the list of starting nodes
     */
    public List<Node> getS() {
        return s;
    }

    /**
     * Get the list of target nodes as defined by the problem set
     * @return
     */
    public List<Node> getT() {
        return t;
    }

    /**
     * Print the Start and Target lists of nodes. Should only be used for small problem sets so it is readable
     */
    public void printST() {
        String startstr = "";
        for (Node node : this.s) {
            startstr += node + " ";
        }
        String targetstr = "";
        for (Node node : this.t) {
            targetstr += node + " ";
        }
        System.out.println("s: " + startstr);
        System.out.println("t: " + targetstr);
    }

    @Override
    public String toString() {
        return "Problem Set: " + "\n" +
                "\tNumber of agents: " + this.numAgents() + "\n" +
                "\tShortest distance: " + this.shortestPathDistance() + "\n" +
                "\tLongest distance: " + this.longestPathDistance() + "\n" +
                "\tAverage distance: " + this.averagePathDistance();
    }
}
