package com.adaptivehash.ui;

import com.adaptivehash.controller.HashController;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;

/**
 * Control panel containing user input controls for hash table operations
 * and strategy configuration.
 */
public class DashboardPanel extends JPanel {

    private final HashController controller;

    private JTextField keyField;
    private JTextField valueField;
    private JButton insertButton;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JComboBox<String> strategyComboBox;
    private JCheckBox autoAdaptCheckBox;

    public DashboardPanel(HashController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        add(new JLabel("Key:"));
        keyField = new JTextField(8);
        add(keyField);

        add(new JLabel("Value:"));
        valueField = new JTextField(8);
        add(valueField);

        insertButton = new JButton("Insert");
        searchButton = new JButton("Search");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");

        add(insertButton);
        add(searchButton);
        add(deleteButton);
        add(clearButton);

        add(new JLabel("Strategy:"));
        strategyComboBox = new JComboBox<>(new String[]{"Linear Probing", "Quadratic Probing", "Double Hashing"});
        add(strategyComboBox);

        autoAdaptCheckBox = new JCheckBox("Adaptive Mode", true);
        add(autoAdaptCheckBox);
    }

    public JTextField getKeyField() {
        return keyField;
    }

    public JTextField getValueField() {
        return valueField;
    }

    public JButton getInsertButton() {
        return insertButton;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JComboBox<String> getStrategyComboBox() {
        return strategyComboBox;
    }

    public JCheckBox getAutoAdaptCheckBox() {
        return autoAdaptCheckBox;
    }
}
