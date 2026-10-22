package com.adaptivehash.ui;

import com.adaptivehash.controller.HashController;
import com.adaptivehash.probing.DoubleHashing;
import com.adaptivehash.probing.LinearProbing;
import com.adaptivehash.probing.QuadraticProbing;
import com.adaptivehash.statistics.OperationMetrics;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Optional;

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
        wireEvents();
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

    private void wireEvents() {
        dashboardPanel.getInsertButton().addActionListener(e -> {
            String key = dashboardPanel.getKeyField().getText().trim();
            String value = dashboardPanel.getValueField().getText().trim();
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a key to insert.", "Input Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (value.isEmpty()) {
                value = key; // Default value to key
            }

            OperationMetrics metrics = controller.insert(key, value);
            refreshAllViews(metrics);
            dashboardPanel.getKeyField().setText("");
            dashboardPanel.getValueField().setText("");
        });

        dashboardPanel.getSearchButton().addActionListener(e -> {
            String key = dashboardPanel.getKeyField().getText().trim();
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a key to search.", "Input Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Optional<String> val = controller.search(key);
            OperationMetrics metrics = controller.getHashTable().getLastOperationMetrics();
            refreshAllViews(metrics);

            if (val.isPresent()) {
                JOptionPane.showMessageDialog(this, "Key '" + key + "' found! Value = " + val.get(), "Search Found", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Key '" + key + "' not found.", "Search Result", JOptionPane.WARNING_MESSAGE);
            }
        });

        dashboardPanel.getDeleteButton().addActionListener(e -> {
            String key = dashboardPanel.getKeyField().getText().trim();
            if (key.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a key to delete.", "Input Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean deleted = controller.delete(key);
            OperationMetrics metrics = controller.getHashTable().getLastOperationMetrics();
            refreshAllViews(metrics);

            if (deleted) {
                JOptionPane.showMessageDialog(this, "Key '" + key + "' deleted (marked with tombstone).", "Delete Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Key '" + key + "' not found for deletion.", "Delete Result", JOptionPane.WARNING_MESSAGE);
            }
        });

        dashboardPanel.getClearButton().addActionListener(e -> {
            controller.clear();
            hashTablePanel.refreshView();
            statisticsPanel.updateMetrics();
            historyPanel.clearHistory();
        });

        dashboardPanel.getStrategyComboBox().addActionListener(e -> {
            String selected = (String) dashboardPanel.getStrategyComboBox().getSelectedItem();
            if ("Quadratic Probing".equals(selected)) {
                controller.switchProbingStrategy(new QuadraticProbing());
            } else if ("Double Hashing".equals(selected)) {
                controller.switchProbingStrategy(new DoubleHashing());
            } else {
                controller.switchProbingStrategy(new LinearProbing());
            }
            hashTablePanel.refreshView();
            statisticsPanel.updateMetrics();
        });

        dashboardPanel.getAutoAdaptCheckBox().addActionListener(e -> {
            boolean enabled = dashboardPanel.getAutoAdaptCheckBox().isSelected();
            controller.getDecisionEngine().setAutoAdaptEnabled(enabled);
        });
    }

    private void refreshAllViews(OperationMetrics metrics) {
        hashTablePanel.refreshView();
        statisticsPanel.updateMetrics();
        if (metrics != null) {
            historyPanel.addOperationRecord(metrics);
        }
        // Sync combo box with potentially adapted strategy
        String currentStrategy = controller.getHashTable().getProbingStrategy().getName();
        if (!currentStrategy.equals(dashboardPanel.getStrategyComboBox().getSelectedItem())) {
            dashboardPanel.getStrategyComboBox().setSelectedItem(currentStrategy);
        }
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
