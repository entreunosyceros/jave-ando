package com.ifcd0112.viewer;

import javax.swing.*;

public class Launcher {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // LAF del sistema
        }
        UiTheme.install();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(MainFrame.findProjectRoot());
            frame.setVisible(true);
        });
    }
}
