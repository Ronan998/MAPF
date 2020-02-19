package Experiment;

import BMAA.BMAA;
import Benchmark.Benchmark;
import Benchmark.Map;
import Benchmark.ProblemSet;
import com.google.common.base.Stopwatch;
import dataStructures.graph.Graph;
import Benchmark.Result;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BasicTesting {

    public static void main(String[] args) {
        experiment3();
    }

    /**
     * Run a single instance of a BMAA on a map, for one agent count, to ensure that basic elements of my programming
     * works.
     */
    public static void experiment1() {
        String map = "maps/BGII-AR0504SR (512*512).map";
        Graph graph = Map.graphFromMap(map);
        ProblemSet problemSet = ProblemSet.randomProblemSet(graph, 2000);
        System.out.println(problemSet);

        System.out.println();
        System.out.println("Beginning execution");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Long start = System.currentTimeMillis();
        Result result = new BMAA(graph,
                problemSet.getS(),
                problemSet.getT(),
                BMAA.DEFAULT_EXPANSIONS,
                BMAA.DEFAULT_VISION,
                BMAA.DEFAULT_MOVES,
                false,
                false).runWithTimeLimit(Duration.ofSeconds(30));
        stopwatch.stop();
        Long end = System.currentTimeMillis();
        System.out.println("Finished execution");
        System.out.println("Millis elapsed (Stopwatch): " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println("Millis elapsed (System.currentTimeMillis): " + (end - start));

        System.out.println(result);
    }

    /**
     * This tests main goal is to try running multiple instances of algorithm and averaging a result.
     */
    public static void experiment2() {
        String map = "maps/BGII-AR0504SR (512*512).map";

        List<Result> results = new ArrayList<>();
        List<ProblemSet> problemSets = new ArrayList<>();

        for (int i=0; i<10; i++) {
            Graph graph = Map.graphFromMap(map);
            ProblemSet problemSet = ProblemSet.randomProblemSet(graph, 500);

            Stopwatch stopwatch = Stopwatch.createStarted();
            Result result = new BMAA(graph,
                    problemSet.getS(),
                    problemSet.getT(),
                    BMAA.DEFAULT_EXPANSIONS,
                    BMAA.DEFAULT_VISION,
                    BMAA.DEFAULT_MOVES,
                    false,
                    false).runWithTimeLimit(Duration.ofSeconds(30));

            problemSets.add(problemSet);
            results.add(result);

            System.out.println(problemSet);
            System.out.println(result);
            System.out.println("Elapsed (ms): " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        }

        // Average instances
        Result averaged = Result.averageInstanceResults(results);

        System.out.println();
        System.out.println("Problem sets");
        System.out.println("---------------------------");
        System.out.println(ProblemSet.csvHeader());
        for (ProblemSet ps : problemSets) {
            System.out.println(ps.csvStatistics());
        }

        System.out.println();
        System.out.println("Results");
        System.out.println("---------------------------");
        System.out.println(Result.csvHeaders());
        for (Result r : results) {
            System.out.println(r.toCsvString());
        }

        System.out.println();
        System.out.println("Averaged");
        System.out.println("---------------------------");
        System.out.println(averaged.toCsvString());
    }

    /**
     * This experiment is to a basic test of running the algorithm at multiple agent counts, making sure the results look
     * ok, collecting data to be used in answering questions such as why do we average over multiple agent counts?
     */
    public static void experiment3() {
        String map = "maps/BGII-AR0504SR (512*512).map";

        List<Result> results = new ArrayList<>();

        for (int n : Benchmark.BMAA_AGENT_COUNTS) {
            List<Result> instanceResults = new ArrayList<>();

            for (int i=0; i<10; i++) {
                Graph graph = Map.graphFromMap(map);
                ProblemSet problemSet = ProblemSet.randomProblemSet(graph, n);

                Result result = new BMAA(graph,
                        problemSet.getS(),
                        problemSet.getT(),
                        BMAA.DEFAULT_EXPANSIONS,
                        BMAA.DEFAULT_VISION,
                        BMAA.DEFAULT_MOVES,
                        false,
                        false).runWithTimeLimit(Duration.ofSeconds(30));

                instanceResults.add(result);
                System.out.println(n + " agents, instance " + i);
            }

            results.add(Result.averageInstanceResults(instanceResults));
        }

        System.out.println();
        System.out.println("Result per agent count");
        System.out.println("------------------------");
        System.out.println(Result.csvHeaders());
        for (Result r : results) {
            System.out.println(r.toCsvString());
        }

        Result averaged = Result.averageDifferentAgentCountsResults(results);

        System.out.println();
        System.out.println("Averaged result across all agent counts");
        System.out.println("------------------------");
        System.out.println(Result.csvHeaders());
        System.out.println(averaged.toCsvString());
    }

}
