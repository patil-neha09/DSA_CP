package com.adaptivehash.ui;

import com.adaptivehash.controller.HashController;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout;

/**
 * Panel displaying real-time metrics including total collisions, average probe counts,
 * load factor, and active probing strategy.
 */
public class StatisticsPanel extends JPanel {

    private final HashController controller;

    private JLabel operationsLabel;
    private JLabel collisionsLabel;
    private JLabel avgProbesLabel;
    private JLabel loadFactorLabel;
    private JLabel currentStrategyLabel;

    public StatisticsPanel(HashController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridLayout(5, 1, 6, 6));
        setBorder(BorderFactory.createTitledBorder("Real-time Statistics"));

        operationsLabel = new JLabel("Total Operations: 0");
        collisionsLabel = new JLabel("Total Collisions: 0");
        avgProbesLabel = new JLabel("Average Probes: 0.00");
        loadFactorLabel = new JLabel("Load Factor: 0.00");
        currentStrategyLabel = new JLabel("Active Strategy: Linear Probing");

        add(operationsLabel);
        add(collisionsLabel);
        add(avgProbesLabel);
        add(loadFactorLabel);
        add(currentStrategyLabel);
    }

    /**
     * Updates the displayed metric values from the controller.
     */
    public void updateMetrics() {
        operationsLabel.setText("Total Operations: " + controller.getStatistics().getTotalOperations());
        collisionsLabel.setText("Total Collisions: " + controller.getStatistics().getTotalCollisions());
        avgProbesLabel.setText(String.format("Average Probes: %.2f", controller.getStatistics().getAverageProbes()));
        loadFactorLabel.setText(String.format("Load Factor: %.2f", controller.getHashTable().loadFactor()));
        currentStrategyLabel.setText("Active Strategy: " + controller.getHashTable().getProbingStrategy().getName());
    }
}
