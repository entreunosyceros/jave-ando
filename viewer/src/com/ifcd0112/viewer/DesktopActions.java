package com.ifcd0112.viewer;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Abrir carpeta o terminal en el sistema (Windows, Linux, macOS, WSL). */
public final class DesktopActions {

    private DesktopActions() {}

    public static void openFolder(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IOException("No existe la carpeta: " + directory);
        }
        Path dir = directory.toAbsolutePath().normalize();

        if (PlatformSupport.isWsl()) {
            openFolderViaWindowsHost(dir);
            return;
        }
        if (PlatformSupport.isWindows()) {
            openFolderWindows(dir);
            return;
        }
        if (PlatformSupport.isMac()) {
            new ProcessBuilder("open", dir.toString()).start();
            return;
        }
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(dir.toFile());
                return;
            }
        }
        tryLinuxOpenFolder(dir);
    }

    public static void openTerminal(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            Files.createDirectories(directory);
        }
        Path dir = directory.toAbsolutePath().normalize();

        if (PlatformSupport.isWsl()) {
            openTerminalViaWindowsHost(dir);
            return;
        }

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
        throw new IOException(buildTerminalErrorMessage(dir, errors));
    }

    private static void openFolderWindows(Path dir) throws IOException {
        try {
            new ProcessBuilder("explorer.exe", dir.toString()).start();
            return;
        } catch (IOException ignored) {
            // fallback Desktop
        }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(dir.toFile());
            return;
        }
        throw new IOException("No se puede abrir el explorador de archivos.");
    }

    private static void openFolderViaWindowsHost(Path linuxDir) throws IOException {
        String winPath = toWindowsPath(linuxDir);
        List<String[]> attempts = List.of(
                new String[]{"cmd.exe", "/c", "start", "", "explorer.exe", winPath},
                new String[]{"explorer.exe", winPath},
                new String[]{
                        "powershell.exe", "-NoProfile", "-Command",
                        "Start-Process explorer.exe -ArgumentList '" + escapePowerShell(winPath) + "'"
                }
        );
        IOException last = tryProcesses(attempts, linuxDir);
        if (last != null) {
            throw new IOException(
                    "No se pudo abrir la carpeta en el Explorador de Windows.\n"
                            + "Ruta Windows: " + winPath + "\n"
                            + "Abre manualmente esa ruta o en WSL: explorer.exe \"$(wslpath -w '"
                            + linuxDir + "')\"",
                    last
            );
        }
    }

    private static void openTerminalViaWindowsHost(Path linuxDir) throws IOException {
        String winPath = toWindowsPath(linuxDir);
        List<String[]> attempts = List.of(
                new String[]{"wt.exe", "-d", winPath},
                new String[]{"cmd.exe", "/c", "start", "wt.exe", "-d", winPath},
                new String[]{
                        "powershell.exe", "-NoExit", "-NoProfile", "-Command",
                        "Set-Location -LiteralPath '" + escapePowerShell(winPath) + "'"
                },
                new String[]{"cmd.exe", "/c", "start", "cmd.exe", "/k", "cd /d \"" + escapeCmdQuotes(winPath) + "\""},
                new String[]{
                        "cmd.exe", "/c", "start", "JAVe-Ando", "cmd.exe", "/k",
                        "cd /d \"" + escapeCmdQuotes(winPath) + "\""
                }
        );
        IOException last = tryProcesses(attempts, linuxDir);
        if (last != null) {
            throw new IOException(
                    buildTerminalErrorMessage(linuxDir, List.of(last.getMessage()))
                            + "\n\nEn WSL prueba en una terminal de Windows:\n  cd /d "
                            + winPath,
                    last
            );
        }
    }

    private static IOException tryProcesses(List<String[]> attempts, Path workDir) {
        IOException last = null;
        for (String[] cmd : attempts) {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                if (workDir != null && Files.isDirectory(workDir)) {
                    pb.directory(workDir.toFile());
                }
                pb.start();
                return null;
            } catch (IOException e) {
                last = e;
            }
        }
        return last;
    }

    /**
     * Convierte ruta Linux (/home/...) a ruta Windows (C:\... o \\wsl.localhost\...).
     */
    static String toWindowsPath(Path linuxPath) throws IOException {
        String path = linuxPath.toAbsolutePath().normalize().toString();
        try {
            Process process = new ProcessBuilder("wslpath", "-w", path)
                    .redirectErrorStream(true)
                    .start();
            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                output = reader.readLine();
            }
            if (process.waitFor(5, TimeUnit.SECONDS) && output != null && !output.isBlank()) {
                return output.trim();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException ignored) {
            // wslpath no disponible
        }

        String distro = System.getenv("WSL_DISTRO_NAME");
        if (distro == null || distro.isBlank()) {
            distro = "Ubuntu";
        }
        if (path.startsWith("/")) {
            return "\\\\wsl.localhost\\" + distro + path.replace('/', '\\');
        }
        throw new IOException("No se pudo convertir la ruta a Windows: " + path);
    }

    private static void tryLinuxOpenFolder(Path dir) throws IOException {
        List<String[]> attempts = List.of(
                new String[]{"xdg-open", dir.toString()},
                new String[]{"gio", "open", dir.toString()}
        );
        IOException last = tryProcesses(attempts, dir);
        if (last != null) {
            throw new IOException("No se puede abrir el explorador de archivos en este sistema.", last);
        }
    }

    private static List<String[]> terminalCommands(Path dir) {
        String path = dir.toString();
        List<String[]> list = new ArrayList<>();
        if (PlatformSupport.isWindows()) {
            list.add(new String[]{"wt.exe", "-d", path});
            list.add(new String[]{
                    "powershell.exe", "-NoExit", "-NoProfile", "-Command",
                    "Set-Location -LiteralPath '" + escapePowerShell(path) + "'"
            });
            list.add(new String[]{
                    "cmd.exe", "/c", "start", "JAVe-Ando", "cmd.exe", "/k",
                    "cd /d \"" + escapeCmdQuotes(path) + "\""
            });
            return list;
        }
        if (PlatformSupport.isMac()) {
            list.add(new String[]{"open", "-a", "Terminal", path});
            return list;
        }
        list.add(new String[]{"gnome-terminal", "--working-directory=" + path});
        list.add(new String[]{"konsole", "--workdir", path});
        list.add(new String[]{"xfce4-terminal", "--working-directory=" + path});
        list.add(new String[]{"x-terminal-emulator", "-e", "bash -lc 'cd \"" + escapeBash(path) + "\"; exec bash'"});
        list.add(new String[]{"xterm", "-e", "bash -lc 'cd \"" + escapeBash(path) + "\"; exec bash'"});
        return list;
    }

    private static String buildTerminalErrorMessage(Path dir, List<String> errors) {
        return "No se encontró un terminal compatible.\n"
                + "Abre manualmente una terminal en:\n  " + dir
                + "\n\nIntentos:\n- " + String.join("\n- ", errors);
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
