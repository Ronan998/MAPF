package Algorithm.Waypoint.Experiments;

import Algorithm.Waypoint.WaypointBmaa;
import Benchmark.Benchmark;
import Benchmark.Result;
import Benchmark.ProblemSet;
import Benchmark.ProblemMap;
import DataStructures.graph.Graph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Purpose of these experiments in this class is to get data on how completion rate scales over time
 * for the Waypoint algorithm.
 */
public class Experiment2 {

    public static void main(String[] args) {
        experiment1(Benchmark.BMAA_TEST_MAPS);
    }

    /**
     * Run the algorithm for different time limits, for different agent counts, for every map.
     */
    public static void experiment1(List<String> maps) {
        for (String mapPath : maps) {

            List<List<Result>> results = new ArrayList<>();

            for (int agentCount : Benchmark.BMAA_AGENT_COUNTS) {
                results.add(runForNAgents(mapPath, agentCount));
            }

            // Group by stopping time
            java.util.Map<Integer, List<Result>> grouped =
                    results.stream()
                            .flatMap(List::stream)
                            .collect(Collectors.groupingBy(Result::getTimeLimit, Collectors.toList()));

            // Record these results
            String fileName =
                    mapPath.replaceAll("maps/", "")
                            .replaceAll(".map", "")
                            .replaceAll(" ", "_") + ".csv";
            Path file = Path.of("logs/detail_" + fileName);
            writeToFile(file, List.of(Result.csvHeaders()),
                    grouped.entrySet().stream().flatMap(entry -> entry.getValue().stream())
                            .map(Result::toCsvString)
                            .collect(Collectors.toList()));

            // Average the results across different agent counts
            List<Result> averaged = new ArrayList<>();
            for (List<Result> r : grouped.values()) {
                averaged.add(Result.averageDifferentAgentCountsResults(r));
            }

            // At the end we have a result for each time limit averaged across different agent counts
            file = Path.of("logs/averaged_" + fileName);
            writeToFile(file,
                    List.of(Result.csvHeaders()),
                    averaged.stream()
                            .map(Result::toCsvString)
                            .collect(Collectors.toList())
            );
        }
    }

    /**
     * Run the algorithm and return a collection of results for different time limits.
     */
    private static List<Result> runForNAgents(String mapPath, int agentCount) {

        // ------------------------------------------------
        List<List<Result>> instanceResults = new ArrayList<>();

        for (int i = 0; i < 1; i++) {

            Graph graph = ProblemMap.graphFromMap(mapPath);
            ProblemSet problemSet = ProblemSet.randomProblemSet(graph, agentCount);

            List<Result> results = new WaypointBmaa(graph,
                    problemSet.getS(),
                    problemSet.getT(),
                    WaypointBmaa.DEFAULT_EXPANSIONS,
                    WaypointBmaa.DEFAULT_VISION,
                    WaypointBmaa.DEFAULT_MOVES,
                    false,
                    false).runWithMultipleTimeLimitsWithFulPathConstruction(Benchmark.TIME_LIMITS);

            instanceResults.add(results);
        }

        // Group by time limit
        java.util.Map<Integer, List<Result>> grouped =
                instanceResults.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.groupingBy(Result::getTimeLimit, Collectors.toList()));

        // Average the instances of each time limit
        List<Result> averaged = new ArrayList<>();
        for (List<Result> instanceGroup : grouped.values()) {
            averaged.add(Result.averageInstanceResults(instanceGroup));
        }

        // ------------------------------------------------
        return averaged;
    }

    @SafeVarargs
    private static void writeToFile(Path file, List<String>... lines) {
        List<String> output =
                Arrays.stream(lines)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

        try {
            Files.write(file, output);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

