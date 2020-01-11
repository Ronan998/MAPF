package BMAA;

import dataStructures.PriorityQueue;
import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.util.*;

public class Agent implements BaseAgent{

    private Graph graph;

    private Node start;
    private Node goal;
    private Node current;

    private int expansions;
    private int vision;
    private int moves;
    private boolean push;
    private boolean flow;

    private List<Node> pathPrefix;
    private int currentNodeIndex;

    private int limit = 0;

    private int makespan;

    private HashMap<Node, Double> heuristics = new HashMap<>();

    public Agent(Graph graph, Node start, Node goal,
                 int expansions, int vision, int moves, boolean push, boolean flow) {
        this.graph = graph;
        this.start = start;
        this.goal = goal;

        this.expansions = expansions;
        this.vision = vision;
        this.moves = moves;
        this.push = push;
        this.flow = flow;

        this.current = start;

        this.pathPrefix = new ArrayList<>();
        this.pathPrefix.add(this.current);

        this.currentNodeIndex = 0;


    }

    // -----------------------------------

    public void searchPhase() {
        if (!nextNodeIsDefined() || Controller.getTime() > limit) {
            SearchState state = search();
            PriorityQueue<Node> open = state.getOpen();
            Map<Node, Node> closed = state.getClosed();
            Map<Node, Double> gCosts = state.getGCosts();

            if (!open.isEmpty()) {
                Node n = open.get();
                double f = gCosts.get(n) + h(n);
                updateHeuristicValues(closed,gCosts, f);
                limit = Controller.getTime() + moves;
            }
        }
    }

    private SearchState search() {
        int exp = 0;
        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<Node, Node> closed = new HashMap<>();
        Map<Node, Double> gCosts = new HashMap<>();

        open.put(current, 0);
        gCosts.put(current, 0.00);

        // TODO collapse parent into open data structure?
        Map<Node, Node> parents = new HashMap<>();
        parents.put(current, null);

        while (!open.isEmpty()) {
            Node n = open.get();
            closed.put(n, parents.get(n));

            if (n == goal || exp > expansions) {
                buildPath(closed, n);
                return new SearchState(open, closed, gCosts);
            }

            for (Node neighbour : graph.getNeigbours(n)) {
                double distance = n.euclideanDistance(neighbour);

                if ((neighbour.isOccupied() && (neighbour != goal)) && distance < vision) {
                    continue;
                }

                if (!closed.containsKey(neighbour)) {
                    if (open.contains(neighbour)) {
                        double gCostOnRecord = gCosts.get(neighbour);
                        double newGCost = gCosts.get(n) + graph.getEdge(n, neighbour).getWeight();

                        if (newGCost < gCostOnRecord) {
                            open.update(neighbour, newGCost + h(neighbour));
                            parents.replace(neighbour, n);
                            gCosts.replace(neighbour, newGCost);
                        }
                    }
                    else {
                        double g = gCosts.get(n) + graph.getEdge(n, neighbour).getWeight();

                        open.put(neighbour, g + h(neighbour));
                        parents.put(neighbour, n);
                        gCosts.put(neighbour, g);
                    }
                }
            }
            exp += 1;
        }

        throw new RuntimeException("Failed to find goal");
    }

    private void buildPath(Map<Node, Node> closed, Node n) {
        ArrayList<Node> newPath = new ArrayList<>();
        Node x = n;
        Node parent = closed.get(n);
        while (parent != null) {
            newPath.add(0, x);
            x = parent;
            parent = closed.get(x);
        }
        newPath.add(0, x);

        this.pathPrefix = newPath;
        this.currentNodeIndex = 0;
    }

    private void updateHeuristicValues(Map<Node, Node> closed, Map<Node, Double> gCosts, double f) {
        for (Node node : closed.keySet()) {
            heuristics.replace(node, f - gCosts.get(node));
        }
    }

    private double h(Node n) {
        if (heuristics.containsKey(n)) {
            return heuristics.get(n);
        }
        double h = n.octileDistance(goal);
        heuristics.put(n, h);
        return h;
    }

    private class SearchState {
        private PriorityQueue<Node> open;
        private Map<Node, Node> closed;
        private Map<Node, Double> gCosts;

        public SearchState(PriorityQueue<Node> open, Map<Node, Node> closed, Map<Node, Double> gCosts) {
            this.open = open;
            this.closed = closed;
            this.gCosts = gCosts;
        }

        public PriorityQueue<Node> getOpen() {
            return open;
        }

        public Map<Node, Node> getClosed() {
            return closed;
        }

        public Map<Node, Double> getGCosts() {
            return gCosts;
        }
    }

    // -----------------------------------

    /**
     * Returns whether or not there exists a node on the agents prefix path
     * after the {@code current} node.
     * If the agent is somehow on a node that is not on our current prefix path
     * then out next node is undefined.
     * @return if the agent has a node to move to next
     */
    public boolean nextNodeIsDefined() {
        if (this.current == this.pathPrefix.get(this.currentNodeIndex) &&
                this.currentNodeIndex < this.pathPrefix.size() - 1) {
            return true;
        }
        return false;
    }

    /**
     * Returns the next node on the path {@code prefixPath}.
     * It assumes that a node exists on the {@code prefixPath} that is after the current node.
     * @return the next node on the agents {@code prefixPath}
     */
    public Node getNextNode() {
        if (this.current == this.pathPrefix.get(this.currentNodeIndex) &&
            this.currentNodeIndex < this.pathPrefix.size() - 1) {
            return pathPrefix.get(currentNodeIndex + 1);
        }
        return null;
    }

    // -----------------------------------


    /**
     * Returns the node which the agent started at.
     * @return the starting node of the agent
     */
    public Node getStart() {
        return start;
    }

    /**
     * Returns the node which the agent is trying to compute a path to.
     * @return the goal node of the agent
     */
    public Node getGoal() {
        return goal;
    }

    /**
     * Returns the node which the agent is currently occupying.
     * @return the node the agent is currently occupying
     */
    public Node getCurrent() {
        return current;
    }

    public boolean atGoal() {
        return current == goal;
    }

    /**
     * Update the agents {@code current} node to be the next node it should move to
     * according to {@code pathPrefix}. It assumes that the next node it will move
     * to is clear of any obstacles.
     */
    public void moveToNextOnPath() {
        this.current.leave();
        this.currentNodeIndex += 1;
        this.current = this.pathPrefix.get(currentNodeIndex);
        this.current.enter(this);
    }

    /**
     * Move to adjacent node which is not the next node according to the current prefix path.
     * @param node the node to move to.
     */
    private void moveTo(Node node){
        this.current.leave();
        this.current = node;
        this.current.enter(this);
    }

    /**
     * Move the any neighbouring node in the graph.
     * Should only be called if the agent is in its goal node.
     *
     * @return the node it has been pushed to.
     */
    public Node push() {
        Set<Node> adjacentNodes = this.graph.getNeigbours(current);

        for (Node node : adjacentNodes) {
            if (!node.isOccupied()) {
                moveTo(node);
                return node;
            }
        }

        return null;
    }

    // ----------------------------------------------------------------------


    @Override
    public int makespan() {
        return this.makespan;
    }

    public String getSummary() {
        String pathStr = "[";
        for (Node n : pathPrefix) {
            pathStr += n + ", ";
        }
        return "Agent" + "\n" +
                "----------------" + "\n" +
                "Current Node: " + current + "\n" +
                "Starting Node: " + start + "\n" +
                "Goal Node: " + goal +  "\n" +
                "Path: " + pathStr +  "\n" +
                "Time: " + Controller.getTime();

    }

}