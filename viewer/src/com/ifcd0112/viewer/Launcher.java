package com.ifcd0112.viewer;

import javax.swing.*;

public class Launcher {

    public static void main(String[] args) {
        try {
            PlatformSupport.installSwingLookAndFeel();
            UiTheme.install();

            SwingUtilities.invokeLater(() -> {
                try {
                    MainFrame frame = new MainFrame(MainFrame.findProjectRoot());
                    frame.setVisible(true);
                } catch (Throwable t) {
                    showFatalError(t);
                }
            });
        } catch (Throwable t) {
            showFatalError(t);
        }
    }

    private static void showFatalError(Throwable t) {
        t.printStackTrace();
        String msg = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
        try {
            JOptionPane.showMessageDialog(
                    null,
                    "No se pudo iniciar JAVe-Ando:\n\n" + msg
                            + "\n\nComprueba JDK 17+ (java -version) y vuelve a ejecutar run.bat o run.sh.",
                    "Error al iniciar",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Throwable ignored) {
            System.err.println("Error al iniciar JAVe-Ando: " + msg);
        }
        System.exit(1);
    }
}
