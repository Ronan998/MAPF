package DataStructures.graph;

public class Edge implements Comparable<Edge>{

    private double weight;
    private Node a;
    private Node b;

    public Edge(Node a, Node b, double weight) {
        this.a = a;
        this.b = b;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "(" + a + "--" + b + " : " + weight + ")";
    }

    // ------------------------------------------------------

    public Node getEitherNode() {
        return this.a;
    }

    public Node getOppositeNode(Node node) {
        if (node == a) {
            return b;
        }
        else {
            return a;
        }
    }

    public double getWeight() {
        return this.weight;
    }

    // -----------------------------------------------------

    @Override
    public int compareTo(Edge other) {
        return Double.compare(this.weight, other.getWeight());
    }

}
