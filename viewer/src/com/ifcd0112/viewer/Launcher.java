package com.ifcd0112.viewer;

import javax.swing.*;

public class Launcher {

    public static void main(String[] args) {
        PlatformSupport.installSwingLookAndFeel();
        UiTheme.install();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(MainFrame.findProjectRoot());
            frame.setVisible(true);
        });
    }
}
