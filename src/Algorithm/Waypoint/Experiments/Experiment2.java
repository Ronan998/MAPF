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
 * Purpose of these experiments in this class is to get data on how completion rate scales over time
 * for the Waypoint algorithm.
 */
public class Experiment2 {

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

            for (int agentCount : Benchmark.BMAA_AGENT_COUNTS) {
                System.out.println("Running Waypoint BMAA on " + fileName + " with " + agentCount + " agents");
                runForNAgents(mapPath, agentCount);
            }

        }
    }

    /**
     * Run the algorithm and return a collection of results for different time limits.
     */
    private static void runForNAgents(String mapPath, int agentCount) {
        List<List<Result>> instanceResults = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

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

            for (Result result : results) {
                writeToFile(currentLogPath, List.of(result.toCsvString()));
            }
            instanceResults.add(results);
        }
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

