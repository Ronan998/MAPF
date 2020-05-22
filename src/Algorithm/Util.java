package Algorithm;

import DataStructures.graph.Node;

public class Util {

    public static void validateMove(Node a, Node b) {
        if (a.octileDistance(b) > Math.sqrt(2)) throw new RuntimeException("Attempted move from " + a + " to " + b);
    }
}
