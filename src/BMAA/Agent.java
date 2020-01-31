package BMAA;

import dataStructures.PriorityQueue;
import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.util.*;

public class Agent{

    private Graph graph;

    private Node start;
    private Node goal;
    private Node current;

    private Node lastPosition;
    private long lastMoveTime;

    private Long lastTimeAtGoal;
    private Integer completionTimeSteps;
    private Double travelDistance;

    private int expansions;
    private double vision;
    private int moves;

    private List<Node> pathPrefix;
    private int currentNodeIndex;

    private int limit = 0;

    private Time time;

    private HashMap<Node, Double> heuristics = new HashMap<>();

    public Agent(Graph graph, Node start, Node goal,
                 int expansions, double vision, int moves, Time time) {
        this.graph = graph;
        this.start = start;
        this.goal = goal;

        this.expansions = expansions;
        this.vision = vision;
        this.moves = moves;

        this.time = time;

        this.lastPosition = start;
        this.current = start;

        this.pathPrefix = new ArrayList<>();
        this.pathPrefix.add(this.current);

        this.currentNodeIndex = 0;

        this.lastPosition = null;

        this.lastTimeAtGoal = null;
        this.completionTimeSteps = null;
        this.travelDistance = 0.00;

    }

    // -----------------------------------

    public void searchPhase() {
        if (!nextNodeIsDefined() || time.getTime() > limit) {
            SearchState state = search();
            PriorityQueue<Node> open = state.getOpen();
            Map<Node, Node> closed = state.getClosed();
            Map<Node, Double> gCosts = state.getGCosts();

            if (!open.isEmpty()) {
                Node n = open.get();
                double f = gCosts.get(n) + h(n);
                updateHeuristicValues(closed,gCosts, f);
                limit = time.getTime() + moves;
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

        Node n = null;
        while (!open.isEmpty()) {
            n = open.get();
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
        buildPath(closed, n);
        return new SearchState(open, closed, gCosts);
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

    /**
     * Get the node that the agent occupied in the previous time step
     * @return the node it occupied in the previous time step
     */
    public Node getLastPosition() {
        return lastPosition;
    }

    /**
     * Get the system time in which the agent last moved
     * @return the time the agent last moved
     */
    public long getLastMoveTime() {
        return lastMoveTime;
    }

    /**
     * Get the system time of which the agent last reached its goal
     * @return the time at which the agent last entered its goal node
     */
    public Long getLastTimeAtGoal() {
        return lastTimeAtGoal;
    }

    /**
     * Return whether or not the agent currently occupies its goal location
     * @return a boolean indicating if the agent is on its goal
     */
    public boolean atGoal() {
        return current == goal;
    }

    /**
     * Return whether or not the agent occupied its goal.
     * If the last time it moved was after the time limit, then we check if its last position was it goal.
     * @param timeLimit the time after which moves are considered invalid.
     * @return a boolean indicating if the agent is/was at its goal.
     */
    public boolean atGoal(long timeLimit) {
        if (lastMoveTime > timeLimit) {
            return getLastPosition() == getGoal();
        }
        else {
            return getCurrent() == getGoal();
        }
    }

    /**
     * Update the agents {@code current} node to be the next node it should move to
     * according to {@code pathPrefix}. It assumes that the next node it will move
     * to is clear of any obstacles.
     */
    public void moveToNextOnPath() {
        this.lastMoveTime = System.currentTimeMillis();
        this.lastPosition = this.current;

        this.current.leave();
        this.currentNodeIndex += 1;
        this.current = this.pathPrefix.get(currentNodeIndex);
        this.current.enter(this);
        updateCompletionTimeSeconds();
        updateCompletionTimeSteps();
        double cost = graph.getEdge(lastPosition, current).getWeight();
        updateTravelDistance(cost);
    }

    /**
     * Move to adjacent node which is not the next node according to the current prefix path.
     * @param node the node to move to.
     */
    private void moveTo(Node node){
        this.lastMoveTime = System.currentTimeMillis();
        this.lastPosition = this.current;
        this.current.leave();
        this.current = node;
        this.current.enter(this);
        updateCompletionTimeSeconds();
        updateCompletionTimeSteps();
        double cost = graph.getEdge(lastPosition, current).getWeight();
        updateTravelDistance(cost);
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

    /**
     * If the agent is at its goal, update the completion time in seconds to reflect this
     */
    private void updateCompletionTimeSeconds() {
        if (this.current == this.goal) {
            this.lastTimeAtGoal = System.currentTimeMillis();
        }
    }

    /**
     * Update the completion time (time steps) of the agent.
     * If the agent is not at its goal, its completion time (time steps is undefined)
     */
    private void updateCompletionTimeSteps() {
        if (this.current == goal) {
            this.completionTimeSteps = time.getTime();
        }
        else {
            this.completionTimeSteps = null;
        }
    }

    /**
     * Get the time step at which the agent reached its goal.
     * If the agent is not currently on its goal, then the completion time (time steps) is undefined (null)
     * We must add 1 to the return value because time steps begin at 0.
     * If by chance the agent starts on its goal, its completionTime will be
     * @return the time step at which the agent reached its goal.
     */
    public Integer getCompletionTimeSteps() {
        return completionTimeSteps;
    }

    /**
     * Add the cost of the edge just traversed to the agents travel distance.
     * This method should be called every time the agent moves.
     */
    private void updateTravelDistance(double cost) {
        this.travelDistance += cost;
    }

    /**
     * Get the travel distance of the agent.
     * Travel distance is defined as the sum of costs of the edges traversed by the agent.
     * @return a double which is the agents travel distance
     */
    public Double getTravelDistance() {
        return travelDistance;
    }

    /**
     * Print a summary of the agents current state
     * @return a multi-line string containing the agents current state
     */
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
                "Time: " + time.getTime();

    }

}