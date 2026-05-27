package com.ifcd0112.viewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public final class SolutionReader {

    private SolutionReader() {}

    public static String readSolutionContent(Exercise exercise) throws IOException {
        Path path = exercise.solutionPath();
        if (path == null || path.toString().isEmpty()) {
            return "Este ejercicio aún no tiene solución publicada.";
        }
        if (!Files.exists(path)) {
            return "Solución no disponible.\nRuta esperada: " + path;
        }
        if (Files.isDirectory(path)) {
            return readDirectoryContent(path);
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public static String readDirectoryContent(Path dir) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Stream<Path> files = Files.walk(dir)) {
            files.filter(p -> p.toString().endsWith(".java"))
                    .sorted(Comparator.comparing(Path::getFileName))
                    .forEach(p -> {
                        sb.append("// ===== ").append(dir.relativize(p)).append(" =====\n");
                        try {
                            sb.append(Files.readString(p, StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            sb.append("// Error al leer: ").append(e.getMessage());
                        }
                        sb.append("\n\n");
                    });
        }
        if (sb.isEmpty()) {
            return "No se encontraron archivos de solución en: " + dir;
        }
        return sb.toString().trim();
    }

    public static boolean solutionExists(Exercise exercise) {
        Path path = exercise.solutionPath();
        if (path == null || path.toString().isEmpty()) {
            return false;
        }
        return Files.exists(path);
    }
}
