package Algorithm.Bmaa.Experiments;

import Algorithm.Bmaa.Bmaa;
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
 * Purpose of this experiment is to test Waypoint BMAA (timing full path construction) on the long convex obstacle map,
 * where we restrict agent starting positions to the bottom half of the map and
 * agent goals to the top of the map.
 */
public class Experiment4 {

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
            ProblemSet problemSet = ProblemSet.fromRegions(graph,
                    agentCount,
                    new ProblemSet.Region(60, 0, 440, 480),
                    new ProblemSet.Region(60, 420, 440, 512));

            List<Result> results = new Bmaa(graph,
                    problemSet.getS(),
                    problemSet.getT(),
                    Bmaa.DEFAULT_EXPANSIONS,
                    Bmaa.DEFAULT_VISION,
                    Bmaa.DEFAULT_MOVES,
                    false,
                    false).runWithMultipleTimeLimits(Benchmark.TIME_LIMITS);

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

