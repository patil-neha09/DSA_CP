package com.adaptivehash;

import com.adaptivehash.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for the Adaptive Collision Resolution Framework.
 */
public class Main {

    public static void main(String[] args) {
        // Run UI creation on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system Look & Feel for native desktop appearance
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to default Swing Look & Feel if system theme is unavailable
            }

            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
