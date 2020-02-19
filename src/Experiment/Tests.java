package Experiment;

import Benchmark.Benchmark;
import Benchmark.Map;
import Benchmark.Result;
import Benchmark.ProblemSet;
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
    public static final int INSTANCES_PER_AGENT_COUNT = 10;

    public static FileHandler currentFH;

    public static void testBMAA() {
        java.util.Map<String, Result> mapResults = new HashMap<>();

        for (String mapName : List.of("maps/BGII-AR0414SR (512*512).map")) { //Benchmark.BMAA_TEST_MAPS) {
            logToFile(mapName);
            LOGGER.info("Running BMAA with parameters: expansions - 32, vision - sqrt 2, moves - 32, push - false, flow - false");
            LOGGER.info("Map: " + mapName);

            java.util.Map<Integer, Result> agentCountResults = new HashMap<>();

            for (int agentCount : Benchmark.BMAA_AGENT_COUNTS) {
                LOGGER.info("\n");
                LOGGER.info("Testing with " + agentCount + " agents");

                List<Result> instanceResults = new ArrayList<>();

                for (int i=0; i<INSTANCES_PER_AGENT_COUNT; i++) {
                    LOGGER.info("");
                    LOGGER.info("Instance " + i);
                    Graph graph = Map.graphFromMap(mapName);

                    ProblemSet problemSet = ProblemSet.randomProblemSet(graph, agentCount);
                    LOGGER.info(problemSet.toString());

                    List<Node> s = problemSet.getS();
                    List<Node> t = problemSet.getT();

                    Result result = new Benchmark(graph, s, t).runBmaa(32, Math.sqrt(2), 32, false, false);

                    LOGGER.info(result.toString());
                    instanceResults.add(result);
                }

                // Average runs of the same number of agents but random problem sets
                Result averageInstanceResult = Result.averageInstanceResults(instanceResults);
                LOGGER.info("Results averaged over " + INSTANCES_PER_AGENT_COUNT + " instances:");
                LOGGER.info(averageInstanceResult.toString());
                agentCountResults.put(agentCount, averageInstanceResult);
            }

            // Average results over the runs with different number of agents
            Result averageAgentCountResult = Result.averageDifferentAgentCountsResults(agentCountResults.values());
            LOGGER.info("Average result over all numbers of agents: ");
            LOGGER.info(averageAgentCountResult.toString());
            mapResults.put(mapName, averageAgentCountResult);
            removeFH();
        }
    }

    public static void setupLogger() {
        LOGGER.setLevel(Level.INFO);
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tl:%1$tM:%1$tS.%1$tN %5$s%6$s%n");
    }

    private static void logToFile(String mapName) {
        try {
            FileHandler fh = new FileHandler("logs/" + Util.currentDateTime("MM-dd HH:mm") + " BMAA benchmarking");
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
            currentFH = fh;
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        LOGGER.info(new Date().toString());
    }

    private static void removeFH() {
        currentFH.close();
        LOGGER.removeHandler(currentFH);
    }

    public static void main(String[] args) {
        setupLogger();
        testBMAA();
    }

    public static void simpleTest() {
        Graph graph = Map.graphFromMap("maps/simpleGrid.map");

        ProblemSet problemSet = ProblemSet.randomProblemSet(graph, 1);
        problemSet.printST();
        System.out.println(problemSet);
        Result result = new Benchmark(graph, problemSet.getS(), problemSet.getT())
                .runBmaa(32, Math.sqrt(2), 32, false, false);

        System.out.println(result);
    }

}
