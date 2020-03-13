package Visualisation;

import Algorithm.Bmaa.BmaaAgent;
import DataStructures.graph.Graph;

import javax.swing.*;

import java.util.List;

public class Visualisation{

    MyCanvas canvas;

    public Visualisation(Graph graph, List<BmaaAgent> agents) {
        JFrame frame = new JFrame("Visualisation");
        canvas = new MyCanvas(graph, agents);
        canvas.setSize(400,400);
        frame.getContentPane().add(canvas);
        //frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void update() {
        canvas.repaint();
    }
}
