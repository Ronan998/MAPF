package Algorithm.Waypoint.Experiments;

import Algorithm.Waypoint.WaypointBmaa;
import Benchmark.Benchmark;
import Benchmark.Result;
import Benchmark.ProblemSet;
import Benchmark.ProblemMap;
import DataStructures.graph.Graph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Purpose of this experiment is to test Waypoint BMAA (not timing full path construction) on the long convex obstacle map,
 * where we restrict agent starting positions to the bottom half of the map and
 * agent goals to the top of the map.
 */
public class Experiment5 {

    public static Path currentLogPath;

    public static void main(String[] args) {
        experiment1(List.of("maps/long-obstacle (512*512).map"));
    }

    /**
     * Run the algorithm for different time limits, for different agent counts, for every map.
     */
    public static void experiment1(List<String> maps) {
        for (String mapPath : maps) {
            String fileName =
                    mapPath.replaceAll("maps/", "")
                            .replaceAll(".map", "")
                            .replaceAll(" ", "_") + ".csv";
            currentLogPath = Path.of("logs/" + fileName);
            writeToFile(currentLogPath, List.of(Result.csvHeaders()));

//            List<List<Result>> results = new ArrayList<>();

            for (int agentCount : Benchmark.BMAA_AGENT_COUNTS) {
                System.out.println("Running Waypoint BMAA on " + fileName + " with " + agentCount + " agents");
                runForNAgents(mapPath, agentCount);
            }

//            // Group by stopping time
//            java.util.Map<Integer, List<Result>> grouped =
//                    results.stream()
//                            .flatMap(List::stream)
//                            .collect(Collectors.groupingBy(Result::getTimeLimit, Collectors.toList()));
//
//            // Record these results
//            String fileName =
//                    mapPath.replaceAll("maps/", "")
//                            .replaceAll(".map", "")
//                            .replaceAll(" ", "_") + ".csv";
//            Path file = Path.of("logs/detail_" + fileName);
//            writeToFile(file, List.of(Result.csvHeaders()),
//                    grouped.entrySet().stream().flatMap(entry -> entry.getValue().stream())
//                            .map(Result::toCsvString)
//                            .collect(Collectors.toList()));
//
//            // Average the results across different agent counts
//            List<Result> averaged = new ArrayList<>();
//            for (List<Result> r : grouped.values()) {
//                averaged.add(Result.averageDifferentAgentCountsResults(r));
//            }
//
//            // At the end we have a result for each time limit averaged across different agent counts
//            file = Path.of("logs/averaged_" + fileName);
//            writeToFile(file,
//                    List.of(Result.csvHeaders()),
//                    averaged.stream()
//                            .map(Result::toCsvString)
//                            .collect(Collectors.toList())
//            );
        }
    }

    /**
     * Run the algorithm and return a collection of results for different time limits.
     */
    private static void runForNAgents(String mapPath, int agentCount) {

        // ------------------------------------------------
        List<List<Result>> instanceResults = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            Graph graph = ProblemMap.graphFromMap(mapPath);
            ProblemSet problemSet = ProblemSet.fromRegions(graph,
                    agentCount,
                    new ProblemSet.Region(60, 0, 440, 480),
                    new ProblemSet.Region(60, 420, 440, 512));

            List<Result> results = new WaypointBmaa(graph,
                    problemSet.getS(),
                    problemSet.getT(),
                    WaypointBmaa.DEFAULT_EXPANSIONS,
                    WaypointBmaa.DEFAULT_VISION,
                    WaypointBmaa.DEFAULT_MOVES,
                    false,
                    false).runWithMultipleTimeLimits(Benchmark.TIME_LIMITS);

            for (Result result : results) {
                writeToFile(currentLogPath, List.of(result.toCsvString()));
            }
            instanceResults.add(results);
        }

//        // Group by time limit
//        java.util.Map<Integer, List<Result>> grouped =
//                instanceResults.stream()
//                        .flatMap(List::stream)
//                        .collect(Collectors.groupingBy(Result::getTimeLimit, Collectors.toList()));
//
//        // Average the instances of each time limit
//        List<Result> averaged = new ArrayList<>();
//        for (List<Result> instanceGroup : grouped.values()) {
//            averaged.add(Result.averageInstanceResults(instanceGroup));
//        }
//
//        // ------------------------------------------------
//        return averaged;
    }

    @SafeVarargs
    private static void writeToFile(Path file, List<String>... lines) {
        List<String> output =
                Arrays.stream(lines)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        try {
            if (!Files.exists(file)) {
                Files.write(file, output);
            }
            else {
                Files.write(file, output, StandardOpenOption.APPEND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

