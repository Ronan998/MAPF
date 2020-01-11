package BMAA;

import dataStructures.graph.Node;

public interface BaseAgent {

    Node getStart();

    Node getGoal();

    Node getCurrent();

    boolean atGoal();

    // TODO make sure makespan is correct
    int makespan();

    void moveToGoal();

}
