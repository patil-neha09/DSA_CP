package com.adaptivehash.ui;

import com.adaptivehash.controller.HashController;
import com.adaptivehash.statistics.OperationMetrics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/**
 * Panel presenting a sequential history log of all operations performed on the hash table.
 */
public class HistoryPanel extends JPanel {

    private final HashController controller;
    private final DefaultTableModel tableModel;
    private final JTable historyTable;

    public HistoryPanel(HashController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Operation History Log"));

        String[] columnNames = {"Type", "Key", "Initial Hash", "Final Slot", "Probes", "Collisions", "Success"};
        tableModel = new DefaultTableModel(columnNames, 0);
        historyTable = new JTable(tableModel);

        add(new JScrollPane(historyTable), BorderLayout.CENTER);
    }

    /**
     * Appends an operation record to the visual history log table.
     *
     * @param metrics the metrics from the operation
     */
    public void addOperationRecord(OperationMetrics metrics) {
        if (metrics == null) return;
        tableModel.addRow(new Object[]{
                metrics.getOperationType(),
                metrics.getKey(),
                metrics.getInitialHash(),
                metrics.getFinalSlotIndex(),
                metrics.getProbeCount(),
                metrics.getCollisionCount(),
                metrics.isSuccessful()
        });
    }

    /**
     * Clears all rows in the history table.
     */
    public void clearHistory() {
        tableModel.setRowCount(0);
    }
}
