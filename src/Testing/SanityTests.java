package Testing;

import BMAA.BMAA;
import Benchmark.Map;
import Benchmark.ProblemSet;
import Benchmark.Result;
import dataStructures.graph.Graph;

import java.util.List;

public class SanityTests {

    public static void main(String[] args) {
        Graph graph = Map.graphFromMap("maps/WCIII-blastedlands (512*512).map");
        ProblemSet problemSet = ProblemSet.randomProblemSet(graph, 1000);
        System.out.println(problemSet);

        List<Integer> stopTimes = List.of(
                5 * 1000,
                10 * 1000,
                15 * 1000,
                20 * 1000,
                25 * 1000,
                30 * 1000
        );

        java.util.Map<Long, Result> results =
                new BMAA(graph,
                        problemSet.getS(),
                        problemSet.getT(),
                        32,
                        Math.sqrt(2),
                        32,
                        false,
                        false).runWithMultipleTimeLimits(stopTimes);

        results.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .forEach(
                        entry -> {
                            System.out.println(entry.getKey());
                            System.out.println(entry.getValue());
                            System.out.println();
                        }
                );
    }
}
