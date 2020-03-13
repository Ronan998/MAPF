package DataStructures.graph;

import DataStructures.PriorityQueue;

import java.util.*;

public class Graph {

    private Map<Node, Map<Node, Edge>> structure = new HashMap<>();
    private final boolean directed;

    private Map<Integer, Map<Integer, Node>> pointMapping = new HashMap<>();

    public Graph(boolean directed) {
        this.directed = directed;
    }

    // ------------------------------------------------------------

    @Override
    public String toString() {

        String hstr = "|V| = " + numNodes() + "; |E| = " + numEdges();

        String vstr = "\nVertices: ";
        for (Node node : nodes()) {
            vstr += node + "-";
        }

        String estr = "\nEdges: ";
        for (Edge edge : edges()) {
            estr += edge + " ";
        }

        return hstr + vstr + estr;
    }

    public Set<Node> nodes() {
        return this.structure.keySet();
    }

    public Set<Edge> edges() {

        Set<Edge> edges = new HashSet<>();

        for (Map<Node, Edge> neigbourSet : this.structure.values()) {
            for (Edge edge : neigbourSet.values()) {
                edges.add(edge);
            }
        }

        return edges;
    }

    public int numNodes() {
        return this.structure.keySet().size();
    }

    public int numEdges() {
        return structure.entrySet().stream()
                .mapToInt((Map.Entry<Node, Map<Node, Edge>> nodeEntry) -> nodeEntry.getValue().size())
                .sum() / 2;
    }

        // ------------------------------------------------------------

    public Node getNodeByLabel(String label) {
        for (Node node : nodes()) {
            if (node.getElement().equals(label)) {
                return node;
            }
        }
        return null;
    }

    public Node getNodeByCoords(int x, int y) {
        return this.pointMapping.get(x).get(y);
    }

    public Edge getEdge(Node a, Node b) {
        return this.structure.get(a).get(b);
    }

    // ------------------------------------------------------------

    public int degree(Node node) {
        return this.structure.get(node).size();
    }

    public Set<Edge> getEdges(Node node) {
        Map<Node, Edge> neigbours = this.structure.get(node);
        return new HashSet<Edge>(neigbours.values());
    }

    // ------------------------------------------------------------

    public Node addNode(String label, int x, int y) {
        Node node = new Node(label, x, y);
        this.structure.put(node, new HashMap<Node, Edge>());
        if (!this.pointMapping.containsKey(x)) {
            this.pointMapping.put(x, new HashMap<>());
        }
        this.pointMapping.get(x).put(y, node);
        return node;
    }

    public Edge addEdge(double weight, Node a, Node b) {
        // TODO make sure this is right for directed graph
        Edge edge =  new Edge(a, b, weight);
        this.structure.get(a).put(b, edge);
        if (!directed) {
            this.structure.get(b).put(a, edge);
        }
        return edge;
    }

    // ------------------------------------------------------------

    public void removeNode(Node node) {
        Set<Node> neighbours = this.structure.get(node).keySet();

        for (Node neighbour : neighbours) {
            this.structure.get(neighbour).remove(node);
        }

        this.structure.remove(node);
    }

    public void removeEdge(Edge edge) {
        Node a = edge.getEitherNode();
        Node b = edge.getOppositeNode(a);

        this.structure.get(a).remove(b);
        this.structure.get(b).remove(a);
    }

    // ------------------------------------------------------------

    public Set<Node> getNeigbours(Node n) {
        return this.structure.get(n).keySet();
    }

    // ------------------------------------------------------------

    public ArrayList<Node> leastCost(Node start, String goal) {
        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<Node, Double> costs = new HashMap<>();
        costs.put(start, 0.00);
        Map<Node, Node> closed = new HashMap<>();
        Map<Node, Node> parents = new HashMap<>();
        parents.put(start, null);

        open.put(start, 0);
        while (!open.isEmpty()) {
            Node n = open.get();
            closed.put(n, parents.get(n));
            if (n.getElement().equals(goal)) {
                return buildPath(closed, n);
            }

            for (Node neighbour : getNeigbours(n)) {
                if (!closed.containsKey(neighbour)) {

                    // New cost of the node
                    double currentPathCost = costs.get(n) + getEdge(n, neighbour).getWeight();
                    // current cost on record, maybe we dont have one because this is the first time we have come across the node
                    double pathCostOnRecord;

                    if (costs.containsKey(neighbour)) {
                        pathCostOnRecord = costs.get(neighbour);
                    }
                    else {
                        pathCostOnRecord = Double.POSITIVE_INFINITY;
                    }

                    if (currentPathCost < pathCostOnRecord) {
                        // we want to reflect this new found path cost in our open queue and whatever extra structures we are making use of
                        // update the open queue
                        if (open.contains(neighbour)) {
                            open.update(neighbour, currentPathCost);
                            costs.replace(neighbour, currentPathCost);
                            parents.replace(neighbour, n);
                        }
                        else {
                            // first time we come across this node
                            open.put(neighbour, currentPathCost);
                            costs.put(neighbour, currentPathCost);
                            parents.put(neighbour, n);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Build a path from start vertex to goal vertex in the form of a list, from a mapping from
     * vertex -> previous vertex.
     * @param record a mapping from vertex to previous vertex.
     * @param end the goal vertex.
     * @return an ArrayList of the path.
     */
    private ArrayList<Node> buildPath(Map<Node, Node> record, Node end) {
        ArrayList<Node> path = new ArrayList<>();

        Node node = end;
        while (node != null) {
            path.add(0, node);
            node = record.get(node);
        }
        return path;
    }

    /**
     * Determine if a path exists between two nodes.
     * @param a Node at one end of the path.
     * @param b Node at the other end of the path.
     * @return true if a path exists, false otherwise.
     */
    public boolean pathExists(Node a, Node b) {
        PriorityQueue<Node> open = new PriorityQueue<>();
        Set<Node> closed = new HashSet<>();

        open.put(a, 0);
        while (!open.isEmpty()) {
            Node n = open.get();
            closed.add(n);

            if (n == b) {
                return true;
            }

            Set<Node> neighbours = getNeigbours(n);
            for (Node neighbour : neighbours) {

                if (!closed.contains(neighbour)) {

                    if (!open.contains(neighbour)) {
                        open.put(neighbour, n.octileDistance(b));
                    }
                }
            }
        }

        return false;
    }
}
