package Benchmark;

import DataStructures.graph.Graph;
import DataStructures.graph.Node;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class to construct a graph object from a MAPF map file.
 */
public class ProblemMap {

    private static final java.util.Map<Character, String> passableTerrains =
            java.util.Map.of('.', "Normal ground",
                            'S', "Shallow water");

    // Private constructor to restrict instantiation
    private ProblemMap() {}

    // ----------------------------------------------------------------------

    public static Graph graphFromMap(String path) {
        try {
            Graph graph = new Graph(false);

            List<char[]> raw =  Files.lines(new File(path).toPath())
                    .filter(line -> !line.startsWith("width") &&
                            !line.startsWith("height") &&
                            !line.startsWith("map") &&
                            !line.startsWith("type"))
                    .map(line -> line.replaceAll("\n", "").toCharArray())
                    .collect(Collectors.toList());

            List<List<Node>> structure = new ArrayList<>();
            int x;
            int y = 0;
            for (char[] line : raw) {

                List<Node> nodeLine = new ArrayList<>();
                x = 0;

                for (char character : line) {
                    Node n;
                    if (passableTerrains.containsKey(character)) {
                        n = graph.addNode(String.valueOf(character), x, y);
                    }
                    else {
                        n = null;
                    }
                    nodeLine.add(n);
                    x += 1;
                }

                structure.add(nodeLine);
                y += 1;
            }

            List<Node> lineAbove = null;
            for (List<Node> line : structure) {
                for (Node n : line) {
                    if (n != null) {
                        addEdges(graph, line, lineAbove, n.getX());
                    }
                }
                lineAbove = line;
            }

            return graph;
        }
        catch (FileNotFoundException e) {
            System.out.println(e);
            System.exit(1);
        }
        catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
        return null;
    }

    private static void addEdges(Graph graph, List<Node> processedLine, List<Node> lineAbove, int x) {
        Node n = processedLine.get(x);
        if (n != null) {
            if (x != 0 && processedLine.get(x - 1) != null) {
                graph.addEdge(1, n, processedLine.get(x - 1));
            }
            if (lineAbove != null) {
                if (lineAbove.get(x) != null) {
                    graph.addEdge(1, n, lineAbove.get(x));
                }
                if (x != 0 && lineAbove.get(x - 1) != null) {
                    graph.addEdge(Math.sqrt(2), n, lineAbove.get(x - 1));
                }
                if (x != lineAbove.size() - 1 && lineAbove.get(x + 1) != null) {
                    graph.addEdge(Math.sqrt(2), n, lineAbove.get(x + 1));
                }
            }

        }
    }

}
