package com.ifcd0112.viewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Historial de consultas de la consola SQL (persistido en el proyecto). */
public final class SqlHistoryStore {

    static final String FILE_NAME = "sql-historial.txt";
    static final String ENTRY_SEPARATOR = "\n---JAVE-ANDO-SQL---\n";
    static final int MAX_ENTRIES = 150;

    private SqlHistoryStore() {}

    public static List<String> load(Path projectRoot) throws IOException {
        Path file = file(projectRoot);
        if (!Files.isRegularFile(file)) {
            return new ArrayList<>();
        }
        String raw = Files.readString(file, StandardCharsets.UTF_8);
        if (raw.isBlank()) {
            return new ArrayList<>();
        }
        List<String> out = new ArrayList<>();
        for (String part : raw.split(ENTRY_SEPARATOR, -1)) {
            String entry = part.trim();
            if (!entry.isEmpty()) {
                out.add(entry);
            }
        }
        if (out.size() > MAX_ENTRIES) {
            return new ArrayList<>(out.subList(out.size() - MAX_ENTRIES, out.size()));
        }
        return out;
    }

    public static void save(Path projectRoot, List<String> entries) throws IOException {
        Path dir = CustomExerciseStore.directory(projectRoot);
        Files.createDirectories(dir);
        List<String> trimmed = entries.size() > MAX_ENTRIES
                ? entries.subList(entries.size() - MAX_ENTRIES, entries.size())
                : entries;
        String body = String.join(ENTRY_SEPARATOR, trimmed);
        if (!body.isEmpty()) {
            body = body + ENTRY_SEPARATOR;
        }
        Files.writeString(file(projectRoot), body, StandardCharsets.UTF_8);
    }

    public static Path file(Path projectRoot) {
        return CustomExerciseStore.directory(projectRoot).resolve(FILE_NAME);
    }
}
