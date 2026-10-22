package com.adaptivehash.ui;

import com.adaptivehash.controller.HashController;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Main application window for the Adaptive Collision Resolution Framework.
 */
public class MainFrame extends JFrame {

    private final HashController controller;

    private DashboardPanel dashboardPanel;
    private HashTablePanel hashTablePanel;
    private StatisticsPanel statisticsPanel;
    private HistoryPanel historyPanel;

    public MainFrame() {
        this.controller = new HashController();
        initComponents();
    }

    private void initComponents() {
        setTitle("Adaptive Collision Resolution Framework");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setLayout(new BorderLayout());

        dashboardPanel = new DashboardPanel(controller);
        hashTablePanel = new HashTablePanel(controller);
        statisticsPanel = new StatisticsPanel(controller);
        historyPanel = new HistoryPanel(controller);

        // Top control dashboard
        add(dashboardPanel, BorderLayout.NORTH);

        // Center split pane: visual hash table on left, statistics/history tabs on right
        JTabbedPane rightTabbedPane = new JTabbedPane();
        rightTabbedPane.addTab("Statistics", statisticsPanel);
        rightTabbedPane.addTab("History Log", historyPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, hashTablePanel, rightTabbedPane);
        splitPane.setResizeWeight(0.65);

        add(splitPane, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public HashController getController() {
        return controller;
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public HashTablePanel getHashTablePanel() {
        return hashTablePanel;
    }

    public StatisticsPanel getStatisticsPanel() {
        return statisticsPanel;
    }

    public HistoryPanel getHistoryPanel() {
        return historyPanel;
    }
}
