package com.ifcd0112.viewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/** Persistencia en {@code ejercicios-personalizados/*.properties}. */
public final class CustomExerciseStore {

    public static final String FOLDER_NAME = "ejercicios-personalizados";

    private CustomExerciseStore() {}

    public static Path directory(Path projectRoot) {
        return projectRoot.resolve(FOLDER_NAME);
    }

    public static List<CustomExerciseRecord> loadAll(Path projectRoot) throws IOException {
        Path dir = directory(projectRoot);
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        List<CustomExerciseRecord> list = new ArrayList<>();
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.toString().endsWith(".properties"))
                    .filter(p -> !p.getFileName().toString().equals(HiddenExerciseStore.FILE_NAME))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .forEach(p -> {
                        try {
                            list.add(readFile(p));
                        } catch (IOException ignored) {
                            // archivo corrupto: se omite
                        }
                    });
        }
        return list;
    }

    public static void save(Path projectRoot, CustomExerciseRecord record) throws IOException {
        Path dir = directory(projectRoot);
        Files.createDirectories(dir);
        Path file = dir.resolve(record.id() + ".properties");
        Properties props = toProperties(record);
        try (OutputStream out = Files.newOutputStream(file)) {
            props.store(out, "Ejercicio personalizado JAVe-Ando");
        }
    }

    public static void delete(Path projectRoot, String id) throws IOException {
        Path file = directory(projectRoot).resolve(id + ".properties");
        Files.deleteIfExists(file);
    }

    public static boolean exists(Path projectRoot, String id) {
        return Files.isRegularFile(directory(projectRoot).resolve(id + ".properties"));
    }

    private static CustomExerciseRecord readFile(Path file) throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        }
        String id = props.getProperty("id", file.getFileName().toString().replace(".properties", ""));
        String title = require(props, "title");
        String module = props.getProperty("module", "Ejercicios añadidos");
        String enunciado = require(props, "enunciadoPath");
        String solution = props.getProperty("solutionPath", "");
        Exercise.SolutionType type = parseType(props.getProperty("solutionType", "NONE"));
        String mainClass = props.getProperty("mainClassName", "");
        return new CustomExerciseRecord(id, title, module, enunciado, solution, type, mainClass);
    }

    private static Properties toProperties(CustomExerciseRecord r) {
        Properties p = new Properties();
        p.setProperty("id", r.id());
        p.setProperty("title", r.title());
        p.setProperty("module", r.module());
        p.setProperty("enunciadoPath", r.enunciadoPath());
        p.setProperty("solutionPath", r.solutionPath() != null ? r.solutionPath() : "");
        p.setProperty("solutionType", r.solutionType().name());
        if (r.mainClassName() != null && !r.mainClassName().isBlank()) {
            p.setProperty("mainClassName", r.mainClassName());
        }
        return p;
    }

    private static String require(Properties p, String key) throws IOException {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) {
            throw new IOException("Falta la propiedad: " + key);
        }
        return v.trim();
    }

    private static Exercise.SolutionType parseType(String raw) {
        try {
            return Exercise.SolutionType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Exercise.SolutionType.NONE;
        }
    }
}
