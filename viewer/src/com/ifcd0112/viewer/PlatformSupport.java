package com.ifcd0112.viewer;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import java.awt.Font;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Rutas, JDK y fuentes UI compatibles con Windows y Linux (y macOS).
 */
public final class PlatformSupport {

    private PlatformSupport() {}

    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    public static boolean isMac() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("mac") || os.contains("darwin");
    }

    public static boolean isLinux() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("linux") || os.contains("nix") || os.contains("nux");
    }

    /** WSL / Linux embebido en Windows: Desktop.open y terminales GTK no funcionan. */
    public static boolean isWsl() {
        if (!isLinux()) {
            return false;
        }
        String distro = System.getenv("WSL_DISTRO_NAME");
        if (distro != null && !distro.isBlank()) {
            return true;
        }
        try {
            String version = Files.readString(Path.of("/proc/version"));
            return version.toLowerCase().contains("microsoft");
        } catch (IOException | SecurityException ignored) {
            return false;
        }
    }

    /** Linux nativo con escritorio, no WSL ni servidor sin GUI. */
    public static boolean isDesktopLinux() {
        return isLinux() && !isWsl();
    }

    /**
     * En Linux, ibus + GtkLookAndFeel rompen teclas muertas en Swing (hay que pulsar ´ y la vocal varias veces).
     * Usar Nimbus/Metal y desactivar el marco IME de Java en campos de texto.
     */
    public static void installSwingLookAndFeel() {
        try {
            if (isDesktopLinux()) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        return;
                    }
                }
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception ignored) {
            // LAF por defecto de la JVM
        }
    }

    /** Teclado español: ´ + a → á en {@link javax.swing.text.JTextComponent}. */
    public static void configureKeyboardInput(JTextComponent field) {
        if (isLinux() && field instanceof JComponent jc) {
            jc.enableInputMethods(false);
        }
    }

    /** Familias tipográficas UI por sistema (Segoe en Windows, Ubuntu/Cantarell en Linux). */
    public static String[] uiFontFamilies() {
        if (isWindows()) {
            return new String[]{"Segoe UI", "Segoe UI Symbol", Font.SANS_SERIF};
        }
        if (isMac()) {
            return new String[]{"SF Pro Text", "Helvetica Neue", ".AppleSystemUIFont", Font.SANS_SERIF};
        }
        return new String[]{"Segoe UI", "Ubuntu", "Cantarell", "Noto Sans", "DejaVu Sans", Font.SANS_SERIF};
    }

    public static String[] monoFontFamilies() {
        if (isWindows()) {
            return new String[]{"Consolas", "Cascadia Mono", Font.MONOSPACED};
        }
        if (isMac()) {
            return new String[]{"SF Mono", "Menlo", "Monaco", Font.MONOSPACED};
        }
        return new String[]{"Consolas", "DejaVu Sans Mono", "Liberation Mono", "Ubuntu Mono", Font.MONOSPACED};
    }

    public static String[] lightTitleFontFamilies() {
        if (isWindows()) {
            return new String[]{"Segoe UI Light", "Segoe UI", Font.SANS_SERIF};
        }
        if (isMac()) {
            return new String[]{"SF Pro Display", "Helvetica Neue", Font.SANS_SERIF};
        }
        return new String[]{"Segoe UI Light", "Ubuntu Light", "Cantarell Light", "Segoe UI", Font.SANS_SERIF};
    }

    /**
     * Localiza la raíz del repositorio (README.md + modulo-poo + viewer) aunque el
     * proceso se lance desde otra carpeta o desde el IDE.
     */
    public static Path findProjectRoot() {
        Set<Path> starts = new LinkedHashSet<>();
        starts.add(Paths.get("").toAbsolutePath().normalize());

        Path codeBase = codeSourceLocation();
        if (codeBase != null) {
            Path walk = codeBase;
            for (int depth = 0; depth < 6 && walk != null; depth++, walk = walk.getParent()) {
                starts.add(walk);
            }
        }

        for (Path start : starts) {
            Path found = walkUpForRepoRoot(start);
            if (found != null) {
                return found;
            }
        }
        return Paths.get("").toAbsolutePath().normalize();
    }

    private static Path walkUpForRepoRoot(Path from) {
        Path p = from.toAbsolutePath().normalize();
        while (p != null) {
            if (isRepoRoot(p)) {
                return p;
            }
            p = p.getParent();
        }
        return null;
    }

    private static boolean isRepoRoot(Path p) {
        return Files.isRegularFile(p.resolve("README.md"))
                && Files.isDirectory(p.resolve("modulo-poo"))
                && Files.isDirectory(p.resolve("viewer"));
    }

    private static Path codeSourceLocation() {
        try {
            var source = Launcher.class.getProtectionDomain().getCodeSource();
            if (source == null || source.getLocation() == null) {
                return null;
            }
            Path loc = Paths.get(source.getLocation().toURI()).toAbsolutePath().normalize();
            if (Files.isRegularFile(loc)) {
                return loc.getParent();
            }
            if (Files.isDirectory(loc)) {
                return loc;
            }
        } catch (Exception ignored) {
            // URI o permisos
        }
        return null;
    }

    /**
     * Resuelve {@code javac} / {@code java}: JAVA_HOME/bin primero, luego PATH del sistema.
     */
    public static String resolveJdkTool(String name) {
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome != null && !javaHome.isBlank()) {
            Path bin = Paths.get(javaHome.trim(), "bin", toolFileName(name));
            if (Files.isExecutable(bin)) {
                return bin.toAbsolutePath().toString();
            }
        }
        return name;
    }

    private static String toolFileName(String name) {
        return isWindows() ? name + ".exe" : name;
    }

    /**
     * Comprueba si {@code child} está dentro de {@code parent} (en Windows ignora mayúsculas
     * y mezcla de / y \).
     */
    public static boolean isSubPath(Path child, Path parent) {
        if (child == null || parent == null) {
            return false;
        }
        Path c = child.toAbsolutePath().normalize();
        Path p = parent.toAbsolutePath().normalize();
        if (isWindows() || isMac()) {
            String cs = c.toString().replace('\\', '/');
            String ps = p.toString().replace('\\', '/');
            if (!ps.endsWith("/")) {
                ps = ps + "/";
            }
            return cs.equalsIgnoreCase(ps.substring(0, ps.length() - 1))
                    || cs.toLowerCase().startsWith(ps.toLowerCase());
        }
        return c.startsWith(p);
    }

    /** Mensaje si no se encuentra el JDK en PATH (típico en Windows sin configurar variables). */
    public static String jdkNotFoundHint() {
        if (isWindows()) {
            return "No se encontró el JDK en PATH.\n"
                    + "Instala JDK 17+ y añade JAVA_HOME (p. ej. C:\\Program Files\\Java\\jdk-17)\n"
                    + "y %JAVA_HOME%\\bin al PATH del sistema, o reinicia el terminal tras instalar.";
        }
        return "No se encontró javac/java en PATH.\n"
                + "Instala JDK 17+ (OpenJDK) y comprueba: java -version, javac -version";
    }

    public static boolean looksLikeJdkMissing(IOException e) {
        if (e == null || e.getMessage() == null) {
            return false;
        }
        String msg = e.getMessage().toLowerCase();
        return msg.contains("cannot run program")
                || msg.contains("no such file")
                || msg.contains("error=2")
                || msg.contains("createprocess")
                || msg.contains("no se encuentra el archivo");
    }

    /** Genera out/sources.txt con rutas relativas a {@code viewerDir} (para javac @file). */
    public static List<String> listViewerSources(Path viewerDir) throws IOException {
        Path src = viewerDir.resolve("src");
        List<String> lines = new ArrayList<>();
        if (!Files.isDirectory(src)) {
            return lines;
        }
        try (var walk = Files.walk(src)) {
            walk.filter(p -> p.toString().endsWith(".java"))
                    .map(p -> viewerDir.relativize(p).toString().replace('\\', '/'))
                    .sorted()
                    .forEach(lines::add);
        }
        return lines;
    }

    public static void writeSourcesFile(Path viewerDir) throws IOException {
        Path outDir = viewerDir.resolve("out");
        Files.createDirectories(outDir);
        Path sourcesFile = outDir.resolve("sources.txt");
        List<String> sources = listViewerSources(viewerDir);
        Files.write(sourcesFile, sources, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static Font pickFont(String[] families, int style, int size) {
        for (String family : families) {
            Font candidate = new Font(family, style, size);
            if (fontMatchesFamily(candidate, family)) {
                return candidate;
            }
        }
        return new Font(Font.SANS_SERIF, style, size);
    }

    /** Carga imágenes con rutas que contienen espacios (común en Windows). */
    public static ImageIcon loadImageIcon(Path path) {
        try {
            return new ImageIcon(path.toUri().toURL());
        } catch (MalformedURLException e) {
            return new ImageIcon(path.toString());
        }
    }

    private static boolean fontMatchesFamily(Font font, String requested) {
        if (Font.SANS_SERIF.equals(requested) || Font.MONOSPACED.equals(requested)) {
            return true;
        }
        String actual = font.getFamily().toLowerCase();
        String want = requested.toLowerCase();
        if (actual.contains(want) || want.contains(actual)) {
            return true;
        }
        // Java sustituye por Dialog si la familia no existe
        return !actual.equals("dialog") && !actual.equals("sansserif");
    }
}
