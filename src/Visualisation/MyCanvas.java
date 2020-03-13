package Visualisation;

import Algorithm.Bmaa.BmaaAgent;
import DataStructures.graph.Graph;
import DataStructures.graph.Node;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class MyCanvas extends JPanel {

    private static final int border = 20;
    private static final int spacing = 50;
    private static final int pointSize = 1;

    private Graph graph;
    private List<BmaaAgent> agents;

    public MyCanvas(Graph graph, List<BmaaAgent> agents) {
        this.graph = graph;
        this.agents = agents;
        //this.setDoubleBuffered(false);
    }

    @Override
    public void paint(Graphics g) {
        for (Node node : graph.nodes()) {
            paintNode(g, node);
        }
        for (BmaaAgent agent : agents) {
            paintAgent(g, agent);
        }
    }

    public static void paintAgent(Graphics g, BmaaAgent agent) {
        if (agent.atGoal()) {
            g.setColor(Color.GREEN);
        }
        else {
            g.setColor(Color.RED);
        }
        Node node = agent.getCurrentNode();
        int x = (node.getX() * spacing) + border;
        int y = (node.getY() * spacing) + border;

        g.drawArc(x / 50, y / 50, pointSize, pointSize, 0, 360);
    }

    public static void paintNode(Graphics g, Node node) {
        g.setColor(Color.BLACK);
        int x = (node.getX() * spacing) + border;
        int y = (node.getY() * spacing) + border;

        g.drawArc(x / 50, y / 50, pointSize, pointSize, 0, 360);
    }


}
