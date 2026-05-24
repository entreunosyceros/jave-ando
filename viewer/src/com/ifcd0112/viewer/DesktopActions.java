package com.ifcd0112.viewer;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Abrir carpeta o terminal en el sistema (Windows, Linux, macOS). */
public final class DesktopActions {

    private DesktopActions() {}

    public static void openFolder(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IOException("No existe la carpeta: " + directory);
        }
        Path dir = directory.toAbsolutePath().normalize();
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(dir.toFile());
                return;
            }
        }
        if (PlatformSupport.isWindows()) {
            try {
                new ProcessBuilder("explorer.exe", dir.toString()).start();
                return;
            } catch (IOException ignored) {
                // siguiente fallback
            }
        }
        throw new IOException("No se puede abrir el explorador de archivos en este sistema.");
    }

    public static void openTerminal(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            Files.createDirectories(directory);
        }
        Path dir = directory.toAbsolutePath().normalize();
        List<String[]> attempts = terminalCommands(dir);
        List<String> errors = new ArrayList<>();
        for (String[] cmd : attempts) {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.directory(dir.toFile());
                pb.start();
                return;
            } catch (IOException e) {
                errors.add(String.join(" ", cmd) + " → " + e.getMessage());
            }
        }
        throw new IOException(
                "No se encontró un terminal compatible.\n"
                        + "Abre manualmente CMD o PowerShell y ejecuta:\n  cd /d "
                        + dir
                        + "\n\nIntentos:\n- "
                        + String.join("\n- ", errors)
        );
    }

    private static List<String[]> terminalCommands(Path dir) {
        String path = dir.toString();
        List<String[]> list = new ArrayList<>();
        if (PlatformSupport.isWindows()) {
            // Windows Terminal (si está instalado)
            list.add(new String[]{"wt.exe", "-d", path});
            // PowerShell en la carpeta (rutas con espacios: -LiteralPath)
            list.add(new String[]{
                    "powershell.exe", "-NoExit", "-NoProfile", "-Command",
                    "Set-Location -LiteralPath '" + escapePowerShell(path) + "'"
            });
            // Símbolo del sistema clásico
            list.add(new String[]{
                    "cmd.exe", "/c", "start", "JAVe-Ando", "cmd.exe", "/k",
                    "cd /d \"" + escapeCmdQuotes(path) + "\""
            });
            return list;
        }
        if (PlatformSupport.isMac()) {
            list.add(new String[]{
                    "open", "-a", "Terminal", path
            });
            return list;
        }
        list.add(new String[]{"gnome-terminal", "--working-directory=" + path});
        list.add(new String[]{"konsole", "--workdir", path});
        list.add(new String[]{"xfce4-terminal", "--working-directory=" + path});
        list.add(new String[]{"x-terminal-emulator", "-e", "bash -lc 'cd \"" + escapeBash(path) + "\"; exec bash'"});
        list.add(new String[]{
                "xterm", "-e", "bash -lc 'cd \"" + escapeBash(path) + "\"; exec bash'"
        });
        return list;
    }

    private static String escapeCmdQuotes(String path) {
        return path.replace("\"", "\"\"");
    }

    private static String escapePowerShell(String path) {
        return path.replace("'", "''");
    }

    private static String escapeBash(String path) {
        return path.replace("\"", "\\\"");
    }
}
