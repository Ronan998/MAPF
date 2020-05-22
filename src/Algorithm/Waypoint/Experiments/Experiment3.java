package Algorithm.Waypoint.Experiments;

import Algorithm.Bmaa.BmaaAgent;
import Benchmark.ProblemMap;
import Benchmark.ProblemSet;
import DataStructures.PriorityQueue;
import DataStructures.graph.Graph;
import DataStructures.graph.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Experiment3 {

    public static void main(String[] args) {
        Graph graph = ProblemMap.graphFromMap("maps/BGII-AR0504SR (512*512).map");

        ProblemSet problemSet = ProblemSet.randomProblemSet(graph, 200);
        System.out.println(problemSet.toString());

        for (int i=0; i<problemSet.getS().size(); i++) {
            Node start = problemSet.getS().get(i);
            Node end = problemSet.getT().get(i);

            double pathCost = getPathCost(graph, start, end);
            System.out.println("Start: " + start);
            System.out.println("End: " + end);
            System.out.println("Cost: " + pathCost);
            System.out.println();
        }
    }

    private static double getPathCost(Graph graph, Node start, Node end) {
        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<Node, Node> closed = new HashMap<>();
        Map<Node, Double> gCosts = new HashMap<>();

        open.put(start, 0);
        gCosts.put(start, 0.00);

        Map<Node, Node> parents = new HashMap<>();
        parents.put(start, null);

        Node n = null;
        while (!open.isEmpty()) {
            n = open.get();
            closed.put(n, parents.get(n));

            if (n == end) {
                List<Node> path = buildPath(closed, n);
                return determineCost(graph, path);
            }

            for (Node neighbour : graph.getNeigbours(n)) {

                if (!closed.containsKey(neighbour)) {
                    if (open.contains(neighbour)) {
                        double gCostOnRecord = gCosts.get(neighbour);
                        double newGCost = gCosts.get(n) + graph.getEdge(n, neighbour).getWeight();

                        if (newGCost < gCostOnRecord) {
                            open.update(neighbour, newGCost + neighbour.octileDistance(end));
                            parents.replace(neighbour, n);
                            gCosts.replace(neighbour, newGCost);
                        }
                    } else {
                        double g = gCosts.get(n) + graph.getEdge(n, neighbour).getWeight();

                        open.put(neighbour, g + neighbour.octileDistance(end));
                        parents.put(neighbour, n);
                        gCosts.put(neighbour, g);
                    }
                }
            }
        }
        throw new RuntimeException("No path found");
    }

    public static List<Node> buildPath(Map<Node, Node> closed, Node end) {
        ArrayList<Node> path = new ArrayList<>();

        Node node = end;
        while (node != null) {
            path.add(0, node);
            node = closed.get(node);
        }
        return path;
    }

    private static double determineCost(Graph g, List<Node> path) {
        double cost = 0.00;
        for (int i=0; i<path.size() - 1; i++) {
            Node a = path.get(i);
            Node b = path.get(i+1);
            cost += g.getEdge(a, b).getWeight();
        }
        return cost;
    }
}
