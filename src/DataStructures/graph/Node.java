package DataStructures.graph;

import Algorithm.Agent;
import Error.CollisionException;

public class Node{

    private final String element;
    private final int x;
    private final int y;
    private Agent occupier = null;

    public Node(String element, int x, int y) {
        this.element = element;
        this.x = x;
        this.y = y;
    }

    // ------------------------------------------------------------------------------------------

    public Agent getAgent() {
        return this.occupier;
    }

    public boolean isOccupied() {
        return occupier != null;
    }

    public void enter(Agent agent) {
        if (this.occupier != null) {
            throw new CollisionException("Vertex collision at " + this.toString() +
                    "between occupying agent " + this.occupier +  " and " + agent);
        }
        this.occupier = agent;
    }

    public void leave() {
        this.occupier = null;
    }

    // ------------------------------------------------------------------------------------------

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double euclideanDistance(Node other) {
        int dx = x - other.getX();
        int dy = y - other.getY();
        return Math.sqrt((dx * dx) + (dy * dy));
    }

    public double octileDistance(Node other) {
        int dx = Math.abs(x - other.getX());
        int dy = Math.abs(y - other.getY());
        return (dx + dy) + (Math.sqrt(2) - 2) * Math.min(dx, dy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return "(" + this.x + "," + this.y + ")";
    }

    public String getElement() {
        return this.element;
    }
}
