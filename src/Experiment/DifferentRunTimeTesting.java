package Experiment;

import BMAA.BMAA;
import Benchmark.Benchmark;
import Benchmark.Result;
import Benchmark.ProblemSet;
import Benchmark.Map;
import dataStructures.graph.Graph;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DifferentRunTimeTesting {

    public static List<Integer> timeLimits =
            List.of(1000, 1250, 1500, 1750,
                    2000, 2250, 2500, 2750,
                    3000, 3250, 3500, 3750,
                    4000, 4250, 4500, 4750,
                    5000, 5250, 5500, 5750,
                    6000, 6250, 6500, 6750,
                    7000, 7250, 7500, 7750,
                    8000, 8250, 8500, 8750,
                    9000, 9250, 9500, 9750,
                    10000, 12500, 15000, 17500,
                    20000, 22500, 25000, 27500,
                    30000, 32500, 35000, 37500,
                    40000, 42500, 45000, 47500,
                    50000);

    public static void main(String[] args) {
        experiment2(Benchmark.BMAA_TEST_MAPS);
    }

    /**
     * Run the algorithm against different time limits for different numbers of agents
     */
    public static void experiment1() {
        List<List<Result>> results = new ArrayList<>();

        for (int agentCount : Benchmark.BMAA_AGENT_COUNTS) {
            results.add(runForNAgents("maps/BGII-AR0504SR (512*512).map", agentCount));
        }

        // Group by stopping time
        java.util.Map<Integer, List<Result>> grouped =
                results.stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(Result::getTimeLimit, Collectors.toList()));

        // Record these results
        Path file = Path.of("logs/detail " + "BGII-AR0504SR (512*512)");
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
        file = Path.of("logs/averaged " + "BGII-AR0504SR (512*512)");
        writeToFile(file,
                List.of(Result.csvHeaders()),
                averaged.stream()
                        .map(Result::toCsvString)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Run the algorithm for different time limits, for different agent counts, for every map.
     */
    public static void experiment2(List<String> maps) {
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

            Graph graph = Map.graphFromMap(mapPath);
            ProblemSet problemSet = ProblemSet.randomProblemSet(graph, agentCount);

            List<Result> results = new BMAA(graph,
                    problemSet.getS(),
                    problemSet.getT(),
                    BMAA.DEFAULT_EXPANSIONS,
                    BMAA.DEFAULT_VISION,
                    BMAA.DEFAULT_MOVES,
                    false,
                    false).runWithMultipleTimeLimits(timeLimits);

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

    private static List<String> parseArgs(String[] args) {
        return Arrays.stream(args).collect(Collectors.toList());
    }

    private static List<String> reverse(List<String> l) {
        List<String> reversed = new ArrayList<>();
        for (int i=l.size() - 1; i>=0; i--) {
            String e = l.get(i);
            reversed.add(e);
        }
        return reversed;
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
