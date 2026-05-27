package com.ifcd0112.viewer;

import java.awt.Point;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Posiciones de tablas en el diagrama del esquema (por base de datos). */
public final class SchemaDiagramLayoutStore {

    static final String DIR_NAME = "esquema-diagrama";

    private SchemaDiagramLayoutStore() {}

    public static Map<String, Point> load(Path projectRoot, String databaseName) throws IOException {
        Path file = file(projectRoot, databaseName);
        if (!Files.isRegularFile(file)) {
            return Map.of();
        }
        Map<String, Point> out = new HashMap<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int eq = trimmed.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String table = trimmed.substring(0, eq).trim();
            String coords = trimmed.substring(eq + 1).trim();
            int comma = coords.indexOf(',');
            if (comma <= 0) {
                continue;
            }
            try {
                int x = Integer.parseInt(coords.substring(0, comma).trim());
                int y = Integer.parseInt(coords.substring(comma + 1).trim());
                out.put(table, new Point(x, y));
            } catch (NumberFormatException ignored) {
                // línea inválida
            }
        }
        return out;
    }

    public static void save(Path projectRoot, String databaseName, Map<String, Point> positions) throws IOException {
        Path dir = CustomExerciseStore.directory(projectRoot).resolve(DIR_NAME);
        Files.createDirectories(dir);
        StringBuilder body = new StringBuilder();
        body.append("# Posiciones del diagrama JAVe-Ando — ").append(databaseName).append('\n');
        for (Map.Entry<String, Point> e : positions.entrySet()) {
            Point p = e.getValue();
            body.append(e.getKey()).append('=').append(p.x).append(',').append(p.y).append('\n');
        }
        Files.writeString(file(projectRoot, databaseName), body.toString(), StandardCharsets.UTF_8);
    }

    public static Path file(Path projectRoot, String databaseName) {
        return CustomExerciseStore.directory(projectRoot)
                .resolve(DIR_NAME)
                .resolve(safeFileName(databaseName) + ".layout");
    }

    private static String safeFileName(String databaseName) {
        if (databaseName == null || databaseName.isBlank()) {
            return "_default";
        }
        String safe = databaseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safe.isEmpty() ? "_default" : safe;
    }
}
