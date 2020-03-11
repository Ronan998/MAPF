package BMAA;

import dataStructures.PriorityQueue;
import dataStructures.graph.Graph;
import dataStructures.graph.Node;

import java.time.temporal.TemporalUnit;
import java.util.*;

public class Agent{

    private Graph graph;

    private Node start;
    private Node goal;
    private Node currentNode;

    private Node previousNode;
    private Long lastMoveTime;

    private Integer completionTimeSteps;
    private Long completionTimeSeconds;
    private Double travelDistance;

    private int expansions;
    private double vision;
    private int moves;

    private List<Node> pathPrefix;
    private int currentPathIndex;

    private int limit = 0;

    private Time time;

    private List<Node> waypoints;
    private int currentWaypointIndex;

    private int waypointSpacing;
    private double closeness;

    private HashMap<Node, Double> heuristics = new HashMap<>();

    public Agent(Graph graph, Node start, Node goal,
                 int expansions, double vision, int moves, Time time, double closeness) {
        this.graph = graph;
        this.start = start;
        this.goal = goal;

        this.expansions = expansions;
        this.vision = vision;
        this.moves = moves;

        this.time = time;

        this.currentNode = start;

        this.pathPrefix = new ArrayList<>();
        this.pathPrefix.add(this.currentNode);

        this.currentPathIndex = 0;

        // Completion Time (Seconds) edge case: if the agent starts on its goal
        // we must set its completion time (seconds) to = 0
        // Completion Time (Time Steps) has the same edge case, so we must
        // set its completion time (time steps) to = 0 also
        if (this.start == this.goal) {
            this.completionTimeSeconds = 0L;
            this. completionTimeSteps = 0;
        }

        this.travelDistance = 0.00;

        this.lastMoveTime = 0L;

        this.closeness = closeness;
    }

    // -----------------------------------

    public void searchPhase() {
        if (!nextNodeIsDefined() || time.getTimeSteps() > limit) {
            SearchState state = search();
            PriorityQueue<Node> open = state.getOpen();
            Map<Node, Node> closed = state.getClosed();
            Map<Node, Double> gCosts = state.getGCosts();

            if (!open.isEmpty()) {
                Node n = open.get();
                double f = gCosts.get(n) + h(n);
                updateHeuristicValues(closed,gCosts, f);
                limit = time.getTimeSteps() + moves;
            }
        }
    }

    private SearchState search() {
        List<Node> paths = new ArrayList<>();
        int exp = 0;
        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<Node, Node> closed = new HashMap<>();
        Map<Node, Double> gCosts = new HashMap<>();

        open.put(currentNode, 0);
        gCosts.put(currentNode, 0.00);

        // TODO collapse parent into open data structure?
        Map<Node, Node> parents = new HashMap<>();
        parents.put(currentNode, null);

        Node n = null;
        while (!open.isEmpty()) {
            n = open.get();
            closed.put(n, parents.get(n));

            if (n == goal || exp > expansions) {
                List<Node> p = constructPath(closed, n);
                paths.addAll(p);

                this.pathPrefix = paths;
                this.currentPathIndex = 0;
                return new SearchState(open, closed, gCosts);
            }

            if (currentWaypoint() != goal) {
                if (n.octileDistance(currentWaypoint()) <= closeness) {
                    List<Node> p = constructPath(closed, n);
                    paths.addAll(p);
                    nextWaypoint();
                    open = new PriorityQueue<>();
                    closed = new HashMap<>();
                    gCosts = new HashMap<>();
                    parents = new HashMap<>();
                    this.heuristics = new HashMap<>();

                    // Chosee your next starting position for the next sub path to the next waypoint, and continue the search from there
                    for (Node neighbour : graph.getNeigbours(n)) {
                        double gCost = graph.getEdge(n, neighbour).getWeight();
                        open.put(neighbour, h(neighbour) + gCost);
                        gCosts.put(neighbour, gCost);
                        parents.put(neighbour, null);
                    }
                    continue;
                }
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
        this.currentPathIndex = 0;
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
        double h = n.octileDistance(currentWaypoint());
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
     * Returns whether or not the node the agent will next move to is defined.
     * An agents next node will be undefined if it has been pushed off its current path or it has reached the end of
     * its current path.
     * @return a boolean indicating if the agents next node is defined
     */
    public boolean nextNodeIsDefined() {
        if (this.currentNode == this.pathPrefix.get(this.currentPathIndex) &&
                this.currentPathIndex < this.pathPrefix.size() - 1) {
            return true;
        }
        return false;
    }

    /**
     * Returns the agents next node as defined in its path prefix.
     * @return the next node on the agents path prefix
     */
    public Node getNextNode() {
        if (this.currentNode == this.pathPrefix.get(this.currentPathIndex) &&
            this.currentPathIndex < this.pathPrefix.size() - 1) {
            return pathPrefix.get(currentPathIndex + 1);
        }
        throw new RuntimeException("Call to retrieve the next node but the next node was undefined");
    }

    // -----------------------------------

    /**
     * Returns the Node which the agent started at in the beginning of the computation.
     * @return the Node which the agent started at
     */
    public Node getStart() {
        return start;
    }

    /**
     * Returns the Node which is the agents goal.
     * @return the Node which is the agents goal
     */
    public Node getGoal() {
        return goal;
    }

    /**
     * Returns the node which the agent is currently occupying.
     * @return the node the agent is currently occupying
     */
    public Node getCurrentNode() {
        return currentNode;
    }

    /**
     * Return whether or not the agent currently occupies its goal location.
     * @return a boolean indicating if the agent is on its goal
     */
    public boolean atGoal() {
        return currentNode == goal;
    }

    /**
     * Update the agents state to reflect that it has moved between nodes.
     */
    public void moveToNextOnPath() {
        updateMetrics(this.pathPrefix.get(currentPathIndex + 1));

        this.currentNode.leave();
        this.currentPathIndex += 1;
        this.currentNode = this.pathPrefix.get(currentPathIndex);
        this.currentNode.enter(this);
    }

    /**
     * Move to adjacent node which is not the next node according to the current prefix path.
     * @param node the node to move to
     */
    private void moveTo(Node node){
        updateMetrics(node);

        this.currentNode.leave();
        this.currentNode = node;
        this.currentNode.enter(this);
    }

    /**
     * Move to any neighbouring node of the agents current node in the graph.
     * According to the BMAA algorithm, this method will only be called if the agent is in its goal node.
     *
     * @return the node it has been pushed to.
     */
    public Node push() {
        Set<Node> adjacentNodes = this.graph.getNeigbours(currentNode);

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
     * Update the part of the agents state used for recording performance measures.
     *
     * @param nextNode the node the agent will move to next
     */
    private void updateMetrics(Node nextNode) {
        // Metrics for completion rate
        this.previousNode = this.currentNode;
        this.lastMoveTime = this.time.milliSecondsElapsed();

        // Completion Time (Seconds) and Completion Time (Time-Steps)
        if (nextNode == goal) {
            this.completionTimeSeconds = this.time.milliSecondsElapsed();
            this.completionTimeSteps = this.time.getTimeSteps();
        }

        // Travel distance
        this.travelDistance += graph.getEdge(this.currentNode, nextNode).getWeight();
    }

    /**
     * Determine if the agent was at its goal before a given time limit.
     * @param timeLimit the time after which we should consider a move to be invalid
     * @return a boolean indicating if the agent was at its goal
     */
    public boolean atGoalBeforeTimeLimit(long timeLimit) {
        // Special case where the agent never moved
        if (previousNode == null) {
            return this.currentNode == this.goal;
        }
        // Otherwise compare the node against the agents correct position according to the time limit
        if (lastMoveTime < timeLimit) {
            return this.currentNode == this.goal;
        }
        else {
            return this.previousNode == this.goal;
        }
    }

    /**
     * Get the completion time (seconds) for the agent.
     *
     * Completion time (seconds) will be undefined if the agent is not at its goal. We return null in this case.
     *
     * Because we can only poll for exit after looping through all the agents in the BMAA npc-controller,
     * we have to take into account that an agent may have executed its move after the time limit.
     * If this is the case, we must determine completion time (seconds) for the agent's previous position.
     *
     * @param timeLimit the time after which moves are considered invalid
     * @return an Long value which is the agents completion time (seconds)
     */
    public Long getCompletionTimeSeconds(long timeLimit) {
        if (lastMoveTime < timeLimit) {
            if (this.currentNode == this.goal) {
                return this.completionTimeSeconds;
            }
            else {
                return null;
            }
        }
        else {
            if (this.previousNode == this.goal) {
                return completionTimeSeconds;
            }
            else {
                return null;
            }
        }
    }

    /**
     * Get the completion time (time steps) for the agent.
     *
     * Completion time (time steps) will be undefined if the agent is not at its goal. We return null in this case.
     *
     * Because we can only poll for exit after looping through all the agents in the BMAA npc-controller,
     * we have to take into account that an agent may have executed its move after the time limit.
     * If this is the case, we must determine completion time (time steps) for the agent's previous position.
     *
     * @param timeLimit the time after which moves are considered invalid
     * @return an Integer value which is the agents completion time (time steps)
     */
    public Integer getCompletionTimeSteps(long timeLimit) {
        if (lastMoveTime < timeLimit) {
            if (this.currentNode == this.goal) {
                return this.completionTimeSteps + 1;
            }
            else {
                return null;
            }
        }
        else {
            if (this.previousNode == this.goal) {
                return completionTimeSteps + 1;
            }
            else {
                return null;
            }
        }
    }

    /**
     * Return the travel distance of the agent.
     *
     * The travel distance is defined as the total sum of all edge costs which were traversed by the agent.
     *
     * Because we can only poll for exit after looping through all the agents in the BMAA npc-controller,
     * we have to take into account that an agent may have executed its move after the time limit.
     * If this is the case, we must determine travel distance for the agent's previous position by subtracting
     * the cost of the last edge traversed by the agent.
     *
     * @param timeLimit the time after which agent moves are considered invalid
     * @return a double which is the agents travel distance
     */
    public double getTravelDistance(long timeLimit) {
        if (lastMoveTime < timeLimit) {
            return this.travelDistance;
        }
        else {
            double invalidMoveCost = graph.getEdge(this.currentNode, this.previousNode).getWeight();
            return travelDistance - invalidMoveCost;
        }
    }

    // --------------------------------------------------------------------------------

    /**
     * Find the full path to the goal and reduce it to a set of waypoints the agent will follow.
     * Initiate the agents state to use the first waypoint.
     */
    public void init() {
        List<Node> fullPath = computeFullPath();
        this.waypointSpacing = (int) Math.ceil(Math.sqrt(fullPath.size()));
        waypoints = reduceToWaypoints(fullPath);
        this.currentWaypointIndex = 0;
    }

    /**
     * Tell the agent to focus on finding a path towards the next waypoint
     */
    private void nextWaypoint() {
        this.currentWaypointIndex += 1;
        if (currentWaypoint() == goal) {
            closeness = 0;
        }
    }

    private Node currentWaypoint() {
        return this.waypoints.get(currentWaypointIndex);
    }

    /**
     * Use the A* algorithm to compute a full path from the current position to the
     * goal position.
     * @return a path of nodes that will lead to the goal
     */
    public List<Node> computeFullPath() {
        Node start = this.start;
        Node goal = this.goal;

        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<Node, Node> closed = new HashMap<>();
        Map<Node, Double> gCosts = new HashMap<>();

        open.put(currentNode, 0);
        gCosts.put(currentNode, 0.00);

        Map<Node, Node> parents = new HashMap<>();
        parents.put(currentNode, null);

        Node n = null;
        while (!open.isEmpty()) {
            n = open.get();
            closed.put(n, parents.get(n));

            if (n == goal) {
                return constructPath(closed, n);
            }

            for (Node neighbour : graph.getNeigbours(n)) {
                double distance = n.euclideanDistance(neighbour);

//                if ((neighbour.isOccupied() && (neighbour != goal)) && distance < vision) {
//                    continue;
//                }

                if (!closed.containsKey(neighbour)) {
                    if (open.contains(neighbour)) {
                        double gCostOnRecord = gCosts.get(neighbour);
                        double newGCost = gCosts.get(n) + graph.getEdge(n, neighbour).getWeight();

                        if (newGCost < gCostOnRecord) {
                            open.update(neighbour, newGCost + neighbour.octileDistance(goal));
                            parents.replace(neighbour, n);
                            gCosts.replace(neighbour, newGCost);
                        }
                    }
                    else {
                        double g = gCosts.get(n) + graph.getEdge(n, neighbour).getWeight();

                        open.put(neighbour, g + neighbour.octileDistance(goal));
                        parents.put(neighbour, n);
                        gCosts.put(neighbour, g);
                    }
                }
            }
        }

        throw new RuntimeException("Goal is not reachable for agent with starting position " + start + " and goal position " + goal);
    }

    /**
     * Construct a path to a specified node n using a mapping from nodes to parent nodes on the path.
     * @param parents a mapping from nodes to their parents nodes on the path
     * @param target the node to compute a path to
     * @return an ordered list of nodes which is the path to the target node
     */
    private List<Node> constructPath(Map<Node, Node> parents, Node target) {
        List<Node> path = new ArrayList<>();
        Node n = target;
        Node parent = parents.get(n);
        while (parent != null) {
            path.add(0, n);
            n = parent;
            parent = parents.get(n);
        }
        path.add(0, n);
        for (Node element : path) {
            if (element == null) {
                for (Node e : path) System.out.println(e);
                System.exit(1);
            }
        }
        return path;
    }

    /**
     * From a full path, reduce it to a list of node which act as intermediate goals to the primary end goal.
     * The ordering of these nodes will be the same as they appear in the full path.
     * @param path the full path to the end goal
     * @return a list containing a subset of nodes of the input
     */
    private List<Node> reduceToWaypoints(List<Node> path) {
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

    /**
     * Decompose a list of sequential values into a list of fewer values, preserving the order of the original list
     * @param list the list to reduce to fewer elements
     * @param spacing the desired spacing between chosen elements
     * @param <T> The type of element the list contains
     * @return a decomposed list
     */
    public static <T> List<T> reduceToWaypoints(List<T> list, int spacing) {
        List<T> waypoints = new ArrayList<>();

        for (int i=0;i<list.size() - 1; i++) {
            waypoints.add(list.get(i));
            i += spacing;
        }
        // Always add the last element (the goal) as a waypoint
        waypoints.add(list.get(list.size() - 1));
        return waypoints;
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
                "Current Node: " + currentNode + "\n" +
                "Starting Node: " + start + "\n" +
                "Goal Node: " + goal +  "\n" +
                "Path: " + pathStr +  "\n" +
                "Time: " + time.getTimeSteps();

    }

}