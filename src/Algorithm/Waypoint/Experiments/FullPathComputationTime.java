package Algorithm.Waypoint.Experiments;

import Algorithm.Waypoint.WaypointBmaa;
import Algorithm.Waypoint.WaypointAgent;
import Algorithm.Time;
import Benchmark.Benchmark;
import Benchmark.ProblemMap;
import Benchmark.ProblemSet;
import com.google.common.base.Stopwatch;
import DataStructures.graph.Graph;
import DataStructures.graph.Node;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is to hold experiments for determining how much time it takes to calculate full paths for
 * different amounts of agents and on different maps.
 */
public class FullPathComputationTime {

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
                    Graph graph = ProblemMap.graphFromMap(map);
                    ProblemSet problemSet = ProblemSet.randomProblemSet(graph, agentCount);
                    List<WaypointAgent> agents = createAgents(graph, problemSet.getS(), problemSet.getT());

                    List<List<Node>> paths = new ArrayList<>();
                    Stopwatch stopwatch = Stopwatch.createStarted();

                    for (WaypointAgent agent : agents) {
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
    private static List<WaypointAgent> createAgents(Graph graph, List<Node> s, List<Node> t) {
        Time time = new Time();

        int expansions = WaypointBmaa.DEFAULT_EXPANSIONS;
        double vision = WaypointBmaa.DEFAULT_VISION;
        int moves = WaypointBmaa.DEFAULT_MOVES;

        List<WaypointAgent> agents = new ArrayList<>();
        for (int i=0; i<s.size(); i++) {
            agents.add(new WaypointAgent(graph, s.get(i), t.get(i), expansions, vision, moves, time, 10*Math.sqrt(2)));
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
