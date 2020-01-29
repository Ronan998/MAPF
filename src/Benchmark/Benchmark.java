package Benchmark;

import BMAA.BMAA;
import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class Benchmark {

    // Common benchmark information
    public static final List<Integer> BMAA_AGENT_COUNTS = List.of(25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400,
            600, 800, 1000, 1200, 1400, 1600, 1800, 2000);
    public static final List<String> BMAA_TEST_MAPS =
            List.of("maps/BGII-AR0414SR (320*281).map",
                    "maps/BGII-AR0414SR (512*512).map",
                    "maps/BGII-AR0504SR (512*512).map",
                    "maps/BGII-AR0701SR (512*512).map",
                    "maps/WCIII-blastedlands (512*512).map",
                    "maps/WCIII-duskwood (512*512).map",
                    "maps/WCIII-golemsinthemist (512*512).map",
                    "maps/DAO-lak304d (193*193).map",
                    "maps/DAO-lak307d (84*84).map",
                    "maps/DAO-lgt300d (747*531).map");

    private Map map;

    private Graph graph;
    private List<Node> s;
    private List<Node> t;


    public Benchmark(String mapFile) {
        this.map = new Map(mapFile);
        this.graph = map.getGraph();

        this.s = randomNodeList(map.getGraph(), 2000);
        this.t = randomNodeList(map.getGraph(), 2000);

    }

    public Benchmark(Graph graph, List<Node> s, List<Node> t) {
        this.graph = graph;
        this.s = s;
        this.t = t;
    }
    // -----------------------------------------------------------------------------

//    public java.util.Map<Integer, Double> OLDrunBmaa(int expansions, double vision, int moves, boolean push, boolean flow) {
//        List<Integer> agentCounts = List.of(25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400,
//                600, 800, 1000, 1200, 1400, 1600, 1800, 2000);
//
//        java.util.Map<Integer, Double> completionRates = new HashMap<>();
//
//        for (int agentLimit : agentCounts) {
//
//            List<Double> runs = new ArrayList<>();
//            for (int i = 0; i < 10; i++) {
//                runs.add(new BMAA(this.graph,
//                        this.s.subList(0, agentLimit - 1),
//                        this.s.subList(0, agentLimit - 1),
//                        expansions,
//                        vision,
//                        moves,
//                        push,
//                        flow).npcController());
//            }
//
//            // Average the results
//            double avgCompletionRate = runs.stream()
//                    .mapToDouble(completionRate -> completionRate)
//                    .sum() / (double) runs.size();
//
//            // Put the averaged result of 10 runs in for that amount of agents
//            completionRates.put(agentLimit, avgCompletionRate);
//
//        }
//
//        return completionRates;
//    }

    public Benchmark(Graph graph, int numAgents) {
        this.graph = graph;

        java.util.Map<String, List<Node>> randomST = randomST(graph, numAgents);
        this.s = randomST.get("s");
        this.t = randomST.get("t");
    }

    public Result runBmaa(int expansions, double vision, int moves, boolean push, boolean flow) {
        return new BMAA(graph, s, t, expansions, vision, moves, push, flow).run();
    }

    // -----------------------------------------------------------------------------

    /**
     * Generate lists of starting and target nodes
     * @param amount the amount of pairs of nodes to be returned
     * @return a HashMap containing the two lists
     */
    public static java.util.Map<String, List<Node>> randomST(Graph graph, int amount) {
        List<Node> starts = new ArrayList<>();
        List<Node> targets = new ArrayList<>();

        List<Node> nodes = new ArrayList<>(graph.nodes());

        for (int i = 0; i < amount; i++){
            Random rand = new Random();

            Node start = nodes.get(rand.nextInt(nodes.size()));

            while (starts.contains(start)) {
                start = nodes.get(rand.nextInt(nodes.size()));
            }

            Node target = nodes.get(rand.nextInt(nodes.size()));

            while (targets.contains(target)) {
                target = nodes.get(rand.nextInt(nodes.size()));
            }

            while (!graph.pathExists(start, target)) {
                start = nodes.get(rand.nextInt(nodes.size()));

                while (starts.contains(start)) {
                    start = nodes.get(rand.nextInt(nodes.size()));
                }

                target = nodes.get(rand.nextInt(nodes.size()));

                while (targets.contains(target)) {
                    target = nodes.get(rand.nextInt(nodes.size()));
                }
            }

            starts.add(start);
            targets.add(target);
        }

        return java.util.Map.of("s", starts, "t", targets);
    }

    /**
     * Get a random node list without replacement
     * @param graph the graph from which to generate the node list
     * @param amount the amount of unique nodes required
     * @return a list of unique nodes
     */
    public static List<Node> randomNodeList(Graph graph, int amount) {
        List<Node> result = new ArrayList<>();

        List<Node> nodes = new ArrayList<>(graph.nodes());

        for (int i = 0; i < amount; i++) {
            Random rand = new Random();
            Node randomNode = nodes.get(rand.nextInt(nodes.size()));

            while (result.contains(randomNode)) {
                randomNode = nodes.get(rand.nextInt(nodes.size()));
            }

            result.add(randomNode);
        }

        return result;
    }
}
