package Benchmark;

import BMAA.BMAA;
import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class Benchmark {

    // Common benchmark information
    public static final List<Integer> BMAA_AGENT_COUNTS = List.of(25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400,
            600, 800, 1000, 1200, 1400, 1600, 1800, 2000);
    public static final List<String> BMAA_TEST_MAPS =
            List.of("maps/BGII-AR0414SR (320*280).map",
                    "maps/BGII-AR0414SR (512*512).map",
                    "maps/BGII-AR0504SR (512*512).map",
                    "maps/BGII-AR0701SR (512*512).map",
                    "maps/WCIII-blastedlands (512*512).map",
                    "maps/WCIII-duskwood (512*512).map",
                    "maps/WCIII-golemsinthemist (512*512).map",
                    "maps/DAO-lak304d (193*193).map",
                    "maps/DAO-lak307d (84*84).map",
                    "maps/DAO-lgt300d (747*531).map");

    private Graph graph;
    private List<Node> s;
    private List<Node> t;

    public Benchmark(Graph graph, List<Node> s, List<Node> t) {
        this.graph = graph;
        this.s = s;
        this.t = t;
    }

    // -----------------------------------------------------------------------------

    public Result runBmaa(int expansions, double vision, int moves, boolean push, boolean flow) {
        return new BMAA(graph, s, t, expansions, vision, moves, push, flow).runWithTimeLimit(Duration.ofSeconds(30));
    }

    // -----------------------------------------------------------------------------
}
