package Common;

import BMAA.BMAA;
import Benchmark.Benchmark;
import Benchmark.Map;
import Benchmark.Result;
import Common.Util;
import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Tests {

    private static final Logger LOGGER = Logger.getLogger(Tests.class.getName());

    public static void testBMAA() {
        LOGGER.info("Running BMAA with parameters from paper. Performing on one map");
        java.util.Map<String, Result> mapResults = new HashMap<>();

        for (String mapName : List.of("maps/BGII-AR0414SR (320*280).map")) {//Benchmark.BMAA_TEST_MAPS) {
            LOGGER.info("Testing on map " + mapName);

            java.util.Map<Integer, Result> agentCountResults = new HashMap<>();

            for (int agentCount : Benchmark.BMAA_AGENT_COUNTS) {
                LOGGER.info("Testing with " + agentCount + " agents");

                List<Result> instanceResults = new ArrayList<>();

                for (int i=0; i<10; i++) {
                    Graph graph = Map.graphFromMap(mapName);

                    java.util.Map<String, List<Node>> randomST = Benchmark.randomST(graph, agentCount);
                    List<Node> s = randomST.get("s");
                    List<Node> t = randomST.get("t");

                    Result result = new Benchmark(graph, s, t).runBmaa(32, Math.sqrt(2), 32, false, false);

                    LOGGER.info("Instance run, completion rate: " + result.getCompletionRate());
                    instanceResults.add(result);
                }

                Result averageInstanceResult = Result.averageResults(instanceResults);
                LOGGER.info("Average completion rate on instances: " + averageInstanceResult.getCompletionRate());
                agentCountResults.put(agentCount, averageInstanceResult);
            }

            Result averageAgentCountResult = Result.averageResults(agentCountResults.values());
            mapResults.put(mapName, averageAgentCountResult);
        }

        mapResults.entrySet()
                .forEach((entry -> {
                    LOGGER.info("Map: " + entry.getKey() + " Completion Rate: " + entry.getValue().getCompletionRate());
                }));
    }

    public static void setupLogger() {
        LOGGER.setLevel(Level.INFO);
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tl:%1$tM:%1$tS.%1$tN %5$s%6$s%n");


        try {
            FileHandler fh = new FileHandler("logs/" + Util.currentDateTime("MM-dd HH:mm") + " BMAA benchmarking");
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        LOGGER.info(new Date().toString());
    }

    public static void main(String[] args) {
        setupLogger();
        testBMAA();
    }

}
