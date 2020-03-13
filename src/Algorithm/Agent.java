package Algorithm;

import DataStructures.graph.Node;

public abstract class Agent {

    private Node start;
    private Node goal;
    private Node currentNode;

    /**
     * Returns the Node which the agent started at in the beginning of the computation.
     * @return the Node which the agent started at
     */
    public Node getStart() {
        return start;
    }

    /**
     * Set the agents start node, this should only be done once.
     * @param node a Node which is the agents starting position
     */
    public void setStart(Node node) {
        this.start = node;
    }

    /**
     * Returns the Node which is the agents goal.
     * @return the Node which is the agents goal
     */
    public Node getGoal() {
        return goal;
    }

    /**
     * Set the goal node of the agent.
     * @param node the new goal node of the agent
     */
    public void setGoal(Node node) {
        this.goal = node;
    }

    /**
     * Returns the node which the agent is currently occupying.
     * @return the node the agent is currently occupying
     */
    public Node getCurrentNode() {
        return currentNode;
    }

    /**
     * Set the current node of the agent
     * @param node
     */
    public void setCurrentNode(Node node) {
        this.currentNode = node;
    }

    /**
     * Return whether or not the agent currently occupies its goal location.
     * @return a boolean indicating if the agent is on its goal
     */
    public boolean atGoal() {
        return currentNode == goal;
    }

    // Specific to a BMAA agent, but just to make things work
    public abstract Node push();

}
