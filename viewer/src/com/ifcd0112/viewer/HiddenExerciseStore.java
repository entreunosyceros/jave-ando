package com.ifcd0112.viewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/** Ejercicios del curso ocultos del índice (no se borran archivos del repo). */
public final class HiddenExerciseStore {

    public static final String FILE_NAME = "ocultos.properties";
    private static final String KEY = "ids";

    private HiddenExerciseStore() {}

    public static Set<String> loadHiddenIds(Path projectRoot) throws IOException {
        Path file = file(projectRoot);
        if (!Files.isRegularFile(file)) {
            return Set.of();
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        }
        String raw = props.getProperty(KEY, "").trim();
        if (raw.isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static void hide(Path projectRoot, String id) throws IOException {
        Set<String> ids = new LinkedHashSet<>(loadHiddenIds(projectRoot));
        ids.add(id);
        save(projectRoot, ids);
    }

    public static void unhide(Path projectRoot, String id) throws IOException {
        Set<String> ids = new LinkedHashSet<>(loadHiddenIds(projectRoot));
        ids.remove(id);
        save(projectRoot, ids);
    }

    public static void save(Path projectRoot, Set<String> ids) throws IOException {
        Path dir = CustomExerciseStore.directory(projectRoot);
        Files.createDirectories(dir);
        Properties props = new Properties();
        props.setProperty(KEY, String.join(",", ids));
        try (OutputStream out = Files.newOutputStream(file(projectRoot))) {
            props.store(out, "Ejercicios ocultos en JAVe-Ando");
        }
    }

    private static Path file(Path projectRoot) {
        return CustomExerciseStore.directory(projectRoot).resolve(FILE_NAME);
    }
}
