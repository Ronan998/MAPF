package Benchmark;

import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Map {

    private final String fileName;
    private final File file;
    private final Graph graph = new Graph(false);

    private final java.util.Map<Character, String> passableTerrains =
            java.util.Map.of('.', "Normal ground",
                            'S', "Shallow water");

    private List<List<Node>> listStructure = new ArrayList<>();

    private int height;
    private int width;

    // ----------------------------------------------------------------------

    public Map(String fileName) {
        this.fileName = fileName;
        this.file = new File(fileName);
        process();
    }

    public String getName() {
        return file.getName();
    }

    public Graph getGraph() {
        return graph;
    }

    public Node getNodeFromCoordinate(int x, int y) {
        return listStructure.get(y).get(x);
    }

    private void process() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            reader.readLine();
            this.height = Integer.parseInt(reader.readLine().split(" ")[1]);
            this.width = Integer.parseInt(reader.readLine().split(" ")[1]);

            List<char[]> raw =  Files.lines(file.toPath())
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
                        addEdges(line, lineAbove, n.getX());
                    }
                }
                lineAbove = line;
            }

        }
        catch (FileNotFoundException e) {
            throw new RuntimeException("Could not find file " + fileName);
        }
        catch (IOException e) {
            throw new RuntimeException();
        }
    }

    // ----------------------------------------------------------------------

    private void addEdges(List<Node> processedLine, List<Node> lineAbove, int x) {
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

    private Node addPoint(char type, int x, int y) {
        return graph.addNode(String.valueOf(type), x, y);
    }

    public static void main(String[] args) {
        Map map = new Map("8grid.map");
        System.out.println(map.getGraph());
    }

}