package com.adaptivehash.ui;

import com.adaptivehash.controller.HashController;
import com.adaptivehash.core.HashTable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

/**
 * Visual representation panel displaying the internal array slots of the hash table,
 * showing slot indices, keys, values, and slot states (Empty, Occupied, Deleted).
 */
public class HashTablePanel extends JPanel {

    private final HashController controller;
    private final JLabel statusLabel;
    private final DefaultTableModel tableModel;
    private final JTable slotTable;

    public HashTablePanel(HashController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Hash Table Grid Visualization"));

        statusLabel = new JLabel("Capacity: 17 | Size: 0 | Deleted: 0 | Load Factor: 0.00", JLabel.CENTER);
        statusLabel.setForeground(Color.DARK_GRAY);
        add(statusLabel, BorderLayout.NORTH);

        String[] columnNames = {"Slot Index", "State", "Key", "Value"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        slotTable = new JTable(tableModel);
        slotTable.setFillsViewportHeight(true);
        slotTable.setRowHeight(22);

        // Custom renderer for slot state coloring
        slotTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String state = (String) table.getModel().getValueAt(row, 1);
                if (!isSelected) {
                    if ("OCCUPIED".equals(state)) {
                        c.setBackground(new Color(230, 247, 230)); // light green
                    } else if ("DELETED".equals(state)) {
                        c.setBackground(new Color(255, 235, 235)); // light red
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        add(new JScrollPane(slotTable), BorderLayout.CENTER);
        refreshView();
    }

    /**
     * Re-renders the hash table slots based on the current state in controller.
     */
    public void refreshView() {
        HashTable<String, String> ht = controller.getHashTable();
        statusLabel.setText(String.format("Capacity: %d | Active Size: %d | Deleted Slots: %d | Load Factor: %.2f | Strategy: %s",
                ht.capacity(),
                ht.size(),
                ht.getDeletedCount(),
                ht.loadFactor(),
                ht.getProbingStrategy().getName()));

        tableModel.setRowCount(0);
        int cap = ht.capacity();
        for (int i = 0; i < cap; i++) {
            HashTable.Entry<String, String> entry = ht.getTable()[i];
            if (entry == null) {
                tableModel.addRow(new Object[]{i, "EMPTY", "-", "-"});
            } else if (entry.isDeleted()) {
                tableModel.addRow(new Object[]{i, "DELETED", String.valueOf(entry.getKey()), String.valueOf(entry.getValue())});
            } else {
                tableModel.addRow(new Object[]{i, "OCCUPIED", String.valueOf(entry.getKey()), String.valueOf(entry.getValue())});
            }
        }
    }
}
