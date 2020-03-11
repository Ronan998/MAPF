package Experiment;

import BMAA.BMAA;
import BMAA.Agent;
import BMAA.Time;
import Benchmark.Benchmark;
import Benchmark.Map;
import Benchmark.ProblemSet;
import Benchmark.Result;
import com.google.common.base.Stopwatch;
import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ComparisonTesting {

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
        experiment1();
    }

    /**
     * The focus of this experiment is to determine how costly it is to plan full paths, paying no attention to other agents.
     */
    public static void experiment1() {
        List<String> maps = List.of("maps/BGII-AR0414SR (320*280).map",
                "maps/BGII-AR0414SR (512*512).map",
                "maps/BGII-AR0504SR (512*512).map",
                "maps/BGII-AR0701SR (512*512).map");

        List<String> lines = new ArrayList<>();
        lines.add("map_name,agent_count,average_path_building_time,average_path_decomp_time");

        for (String map : maps) {

            for (int agentCount : Benchmark.BMAA_AGENT_COUNTS) {
                List<Duration> fullPathTimes = new ArrayList<>();
                List<Duration> decompTimes = new ArrayList<>();

                for (int i = 0; i < 10; i++) {
                    Graph graph = Map.graphFromMap(map);
                    ProblemSet problemSet = ProblemSet.randomProblemSet(graph, agentCount);
                    List<Agent> agents = createAgents(graph, problemSet.getS(), problemSet.getT());

                    List<List<Node>> paths = new ArrayList<>();
                    Stopwatch stopwatch = Stopwatch.createStarted();

                    for (Agent agent : agents) {
                        List<Node> path = agent.computeFullPath();
                        paths.add(path);
                    }
                    stopwatch.stop();
                    fullPathTimes.add(stopwatch.elapsed());


                    stopwatch = Stopwatch.createStarted();
                    for (List<Node> path : paths) {
                        reduceToWaypoints(path);
                    }
                    stopwatch.stop();
                    decompTimes.add(stopwatch.elapsed());

                }

                // Average out the instances
                double averageFullPathConstructionTime = fullPathTimes.stream()
                        .mapToLong(Duration::toMillis)
                        .average()
                        .orElseThrow(RuntimeException::new);

                double averageDecompTime = decompTimes.stream()
                        .mapToLong(Duration::toMillis)
                        .average()
                        .orElseThrow(RuntimeException::new);

                String mapName = map.replaceAll("maps/", "").replaceAll(" ", "");
                lines.add(mapName + "," + agentCount + "," + averageFullPathConstructionTime + "," + averageDecompTime);
            }
        }

        writeToFile(Path.of("logs/fullPathTimes.csv"), lines);
    }

    /**
     * Create a list of agents. Only to be used for experiment1 above.
     */
    private static List<Agent> createAgents(Graph graph, List<Node> s, List<Node> t) {
        Time time = new Time();

        int expansions = BMAA.DEFAULT_EXPANSIONS;
        double vision = BMAA.DEFAULT_VISION;
        int moves = BMAA.DEFAULT_MOVES;

        List<Agent> agents = new ArrayList<>();
        for (int i=0; i<s.size(); i++) {
            agents.add(new Agent(graph, s.get(i), t.get(i), expansions, vision, moves, time, 10*Math.sqrt(2)));
        }
        return agents;
    }

    /**
     * From a full path, reduce it to a list of node which act as intermediate goals to the primary end goal.
     * The ordering of these nodes will be the same as they appear in the full path.
     * @param path the full path to the end goal
     * @return a list containing a subset of nodes of the input
     */
    private static List<Node> reduceToWaypoints(List<Node> path) {
        int waypointSpacing = (int) Math.ceil(Math.sqrt(path.size()));
        List<Node> waypoints = new ArrayList<>();

        int fullLength = path.size();

        int index = waypointSpacing;
        while (index < fullLength - 1) {
            waypoints.add(path.get(index));
            index += waypointSpacing;
        }
        waypoints.add(path.get(fullLength - 1));

        return waypoints;
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
