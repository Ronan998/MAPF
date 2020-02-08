package Testing;

import BMAA.BMAA;
import Benchmark.Benchmark;
import Benchmark.Map;
import Benchmark.ProblemSet;
import Benchmark.Result;
import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class DifferentRunTimes {

    public static Logger LOGGER = Logger.getLogger(DifferentRunTimes.class.getName());
    private static int INSTANCES_PER_AGENT_COUNT = 10;

    private static List<Integer> stopTimes =
            List.of(500,
                    750,
                    1000,
                    1250,
                    1500,
                    1750,
                    2500,
                    5000,
                    7500,
                    10000,
                    12500,
                    15000,
                    17500,
                    20000,
                    22500,
                    25000,
                    27500,
                    30000);

    /**
     * Test code for running the BMAA algorithm on maps and collecting results for the algorithm
     * at different stopping times of the algorithm.
     *
     * The aim of this experiment is to find some results that we can relate to the results produced by the
     * authors of tthe paper.
     */
    public static void main(String[] args) {
        String map = "maps/BGII-AR0414SR (512*512).map";
        Graph graph = Map.graphFromMap(map);
        java.util.Map<Long, Result> results = run10Instances(graph, 500);

        System.out.println("Number of agents: 25");
        results.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .forEach(entry -> {
                    System.out.println("Stop time: " + entry.getKey());
                    System.out.println(entry.getValue());
                });

        /*
        java.util.Map<String, Result> mapResults = new HashMap<>();

        List<String> testMaps = List.of("maps/BGII-AR0414SR (512*512).map");


        String map = "maps/BGII-AR0414SR (512*512).map";
        Graph graph = Map.graphFromMap(map);

        // Run the experiments, getting for each agent count, different results at different stop times
        java.util.Map<Integer, java.util.Map<Long, Result>> results = new HashMap<>();

        System.out.println("Beginning algorithm execution...");
        for (int agentCount : Benchmark.BMAA_AGENT_COUNTS) {
            results.put(agentCount, run10Instances(graph, agentCount));
        }

        // Group the results of different agent counts by stop times
        java.util.Map<Long, List<Result>> aggregated = aggregateResultsByStopTime(results);
        // Print this for recording
        for (java.util.Map.Entry<Long, List<Result>> entry : aggregated.entrySet()) {
            System.out.println("Stop time: " + entry.getKey());
            System.out.println("-------------------------------");
            for (Result r : entry.getValue()) {
                System.out.println(r);
                System.out.println();
            }
        }


        // Average the results for the same stop time across the different agent counts
        java.util.Map<Long, Result> averaged = averageResults(aggregated);

        // Print results
        System.out.println("Map: " + map + "\n");
        for (java.util.Map.Entry<Long, Result> entry : averaged.entrySet()) {
            System.out.println("Stopping time: " + entry.getKey());
            System.out.println(entry.getValue());
        }
         */
//        List<java.util.Map<Long, Result>> stopTimeResults = testMaps.stream()
//                // Running the experiments
//                .map(testMap -> {
//                    // Mapping from agentCount -> Mapping (different stop times -> result)
//                    java.util.Map<Integer, java.util.Map<Long, Result>> agentCountResults = new HashMap<>();
//
//                    for (int agentCount : Benchmark.BMAA_AGENT_COUNTS) {
//                        // Get mapping of stopTime -> result (for one agent count)
//                        java.util.Map<Long, Result> resultsPerStopTime =
//                                run10Instances(Map.graphFromMap(testMap), agentCount);
//
//                        agentCountResults.put(agentCount, resultsPerStopTime);
//                    }
//
//                    return agentCountResults;
//                })
//                // Aggregating the results to Map(StopTime -> List<Result>)
//                .map(DifferentRunTimes::aggregateResultsByStopTime)
//                .map(DifferentRunTimes::averageResults)
//                .collect(Collectors.toList());
    }


    /**
     * Run 10 instances of the BMAA algorithm and return the averaged results
     * @param graph the graph to run instances on
     * @return a mapping between stop times and the average results for that stop time
     */
    private static java.util.Map<Long, Result> run10Instances(Graph graph, Integer agentCount) {
        // A list of instance results
        // where an instance result is a mapping from stop time in that instance -> result
        // List of (Map of (stop time -> result))
        List<java.util.Map<Long, Result>> results = new ArrayList<>();

        for (int i = 0; i < INSTANCES_PER_AGENT_COUNT; i++) {

            ProblemSet problemSet = ProblemSet.randomProblemSet(graph, agentCount);

            List<Node> s = problemSet.getS();
            List<Node> t = problemSet.getT();

            // Mapping from timelimit -> result for that timeLimit
            java.util.Map<Long, Result> instanceResult =
                    new BMAA(graph, s, t, 32, Math.sqrt(2), 32, false, false)
                            .runWithMultipleTimeLimits(stopTimes);

            results.add(instanceResult);
        }

        // Average all the results for the 5 seconds stop time, all the results for the 10 second stop time, and so on...

        // First Aggregate results of the same stop time into lists
        java.util.Map<Long, List<Result>> aggregated = new HashMap<>();

        for (java.util.Map<Long, Result> instanceResults : results) {
            for (java.util.Map.Entry<Long, Result> entry : instanceResults.entrySet()) {
                Long stopTime = entry.getKey();
                Result result = entry.getValue();

                if (aggregated.containsKey(stopTime)) {
                    aggregated.get(stopTime).add(result);
                }
                else {
                    List<Result> newList = new ArrayList<>();
                    newList.add(result);
                    aggregated.put(stopTime, newList);
                }
            }
        }

        // Average the results of each stop time
        java.util.Map<Long, Result> averaged = new HashMap<>();

        for (java.util.Map.Entry<Long, List<Result>> entry : aggregated.entrySet()) {
            averaged.put(entry.getKey(), Result.averageInstanceResults(entry.getValue()));
        }

        return averaged;
    }

    /**
     * Aggregates results objects by stop time.
     * This method averages the results for a given stop time across all agent counts.
     *
     * @param results a mapping from agent count to another mapping from stop time to result
     * @return a mapping from stop to the average result for that stop time
     */
    private static java.util.Map<Long, List<Result>> aggregateResultsByStopTime(java.util.Map<Integer, java.util.Map<Long, Result>> results) {
        java.util.Map<Long, List<Result>> stopTimeToResults = new HashMap<>();

        for (java.util.Map.Entry<Integer, java.util.Map<Long, Result>> result : results.entrySet()) {
            int agentCount = result.getKey();
            java.util.Map<Long, Result> agentCountToDifferentStopTimeResults = result.getValue();

            for (java.util.Map.Entry<Long, Result> entry : agentCountToDifferentStopTimeResults.entrySet()) {
                long stopTime = entry.getKey();
                Result stopTimeResult = entry.getValue();

                if (stopTimeToResults.containsKey(stopTime)) {
                    stopTimeToResults.get(stopTime).add(stopTimeResult);
                }
                else {
                    List<Result> newList = new ArrayList<>();
                    newList.add(stopTimeResult);
                    stopTimeToResults.put(stopTime, newList);
                }
            }
        }

        return stopTimeToResults;
    }

    private static java.util.Map<Long, Result> averageResults(java.util.Map<Long, List<Result>> stopTimeToResults) {
        java.util.Map<Long, Result> stopTimeToAverageResult = new HashMap<>();
        for (java.util.Map.Entry<Long, List<Result>> result : stopTimeToResults.entrySet()) {
            stopTimeToAverageResult.put(result.getKey(), Result.averageDifferentAgentCountsResults(result.getValue()));
        }

        return stopTimeToAverageResult;
    }
}
