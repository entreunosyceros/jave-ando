package com.ifcd0112.viewer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.SwingUtilities;

/** Icono en la bandeja del sistema con menú equivalente al de la ventana principal. */
public final class SystemTrayManager {

    private static TrayIcon trayIcon;
    private static boolean installed;
    private static volatile boolean exiting;

    private SystemTrayManager() {}

    public static boolean isInstalled() {
        return installed;
    }

    public static boolean isExiting() {
        return exiting;
    }

    public static boolean install(MainFrame frame, Path projectRoot) {
        if (!SystemTray.isSupported()) {
            return false;
        }
        if (installed) {
            return true;
        }
        Image image = loadTrayImage(projectRoot);
        if (image == null) {
            return false;
        }
        try {
            PopupMenu popup = buildPopupMenu(frame);
            trayIcon = new TrayIcon(image, AppInfo.NAME, popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() >= 2) {
                        SwingUtilities.invokeLater(frame::showMainWindow);
                    }
                }
            });
            SystemTray.getSystemTray().add(trayIcon);
            installed = true;
            return true;
        } catch (Exception ex) {
            trayIcon = null;
            installed = false;
            return false;
        }
    }

    public static void shutdown() {
        if (trayIcon != null && SystemTray.isSupported()) {
            try {
                SystemTray.getSystemTray().remove(trayIcon);
            } catch (Exception ignored) {
                // bandeja no disponible al salir
            }
        }
        trayIcon = null;
        installed = false;
    }

    public static void exitApplication(MainFrame frame) {
        exiting = true;
        shutdown();
        frame.dispose();
        System.exit(0);
    }

    private static PopupMenu buildPopupMenu(MainFrame frame) {
        PopupMenu menu = new PopupMenu();

        menu.add(item("Abrir " + AppInfo.NAME, frame::showMainWindow));
        menu.addSeparator();
        menu.add(item("Cerrar", () -> exitApplication(frame)));
        menu.addSeparator();
        menu.add(item("— Herramientas —", null));
        menu.add(item("Abrir carpeta del enunciado", frame::openEnunciadoFolder));
        menu.add(item("Abrir carpeta «mi código»", frame::openStudentWorkFolder));
        menu.add(item("Terminal en carpeta del enunciado", frame::openEnunciadoTerminal));
        menu.add(item("Terminal en «mi código»", frame::openStudentWorkTerminal));
        menu.addSeparator();
        menu.add(item("Configurar conexión SQL…", frame::openSqlConfigDialog));
        menu.add(item("Inicializar base de datos (script 01)…", frame::runInitDatabaseScript));

        menu.addSeparator();
        menu.add(item("— Base de datos —", null));
        menu.add(item("Seleccionar base de datos…", frame::openSelectDatabaseDialog));
        menu.add(item("Consola SQL…", frame::openSqlConsole));
        menu.add(item("Ver esquema gráfico…", frame::openSchemaViewer));

        menu.addSeparator();
        menu.add(item("— Ejercicios —", null));
        menu.add(item("Añadir ejercicio…", frame::addCustomExercise));
        menu.add(item("Eliminar ejercicio…", frame::removeCustomExercise));
        menu.add(item("Restaurar ejercicios ocultos…", frame::restoreHiddenExercises));

        menu.addSeparator();
        menu.add(item("— Ver —", null));
        menu.add(item("Alternar tema claro / oscuro", frame::toggleThemeFromTray));

        menu.addSeparator();
        menu.add(item("— Ayuda —", null));
        menu.add(item("Cómo añadir y gestionar ejercicios…", frame::openExerciseHelp));
        menu.add(item("Acerca de " + AppInfo.NAME + "…", frame::openAbout));

        return menu;
    }

    private static MenuItem item(String label, Runnable action) {
        MenuItem item = new MenuItem(label);
        if (action != null) {
            item.addActionListener(e -> SwingUtilities.invokeLater(action));
        } else {
            item.setEnabled(false);
        }
        return item;
    }

    private static Image loadTrayImage(Path projectRoot) {
        Path logo = projectRoot.resolve("img/logo.png");
        if (!Files.isRegularFile(logo)) {
            return iconToTrayImage(AppIcons.appIcon());
        }
        try {
            java.awt.image.BufferedImage src = javax.imageio.ImageIO.read(logo.toFile());
            if (src == null) {
                return iconToTrayImage(AppIcons.appIcon());
            }
            int size = 24;
            java.awt.image.BufferedImage scaled = new java.awt.image.BufferedImage(
                    size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, size, size, null);
            g.dispose();
            return scaled;
        } catch (Exception e) {
            return iconToTrayImage(AppIcons.appIcon());
        }
    }

    private static Image iconToTrayImage(javax.swing.Icon icon) {
        int size = 24;
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        return bi;
    }
}
