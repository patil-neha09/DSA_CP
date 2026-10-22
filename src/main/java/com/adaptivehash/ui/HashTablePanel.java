package com.adaptivehash.ui;

import com.adaptivehash.controller.HashController;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 * Visual representation panel displaying the internal array slots of the hash table,
 * showing keys, values, and slot states (Empty, Occupied, Deleted).
 */
public class HashTablePanel extends JPanel {

    private final HashController controller;
    private final JLabel statusLabel;

    public HashTablePanel(HashController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Hash Table Grid Visualization"));

        statusLabel = new JLabel("Hash table visualization will be rendered here.", JLabel.CENTER);
        statusLabel.setForeground(Color.DARK_GRAY);
        add(statusLabel, BorderLayout.CENTER);
    }

    /**
     * Re-renders the hash table slots based on the current state in controller.
     */
    public void refreshView() {
        // Skeleton method: Slot rendering will be implemented here
        statusLabel.setText(String.format("Capacity: %d | Size: %d | Load Factor: %.2f",
                controller.getHashTable().capacity(),
                controller.getHashTable().size(),
                controller.getHashTable().loadFactor()));
    }
}
