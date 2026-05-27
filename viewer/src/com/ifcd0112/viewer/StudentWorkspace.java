package com.ifcd0112.viewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Carpeta {@code trabajo/} del alumno (paralela a {@code soluciones/}), sin modificar la solución oficial.
 */
public final class StudentWorkspace {

    public static final String FOLDER_NAME = "trabajo";

    private StudentWorkspace() {}

    public static Path root(Path projectRoot) {
        return projectRoot.resolve(FOLDER_NAME);
    }

    public static Path directory(Exercise exercise, Path projectRoot) {
        Path trabajo = root(projectRoot);
        Path soluciones = projectRoot.resolve("soluciones");
        Path solution = exercise.solutionPath();

        if (solution != null && !solution.toString().isEmpty()) {
            Path abs = solution.toAbsolutePath().normalize();
            Path solRoot = soluciones.toAbsolutePath().normalize();
            if (PlatformSupport.isSubPath(abs, solRoot)) {
                Path rel = solRoot.relativize(abs);
                if (Files.isRegularFile(abs)) {
                    rel = rel.getParent();
                }
                if (rel != null) {
                    return trabajo.resolve(rel);
                }
            }
        }

        Path enunciado = exercise.enunciadoPath();
        if (enunciado != null && enunciado.getParent() != null) {
            Path enunParent = enunciado.getParent().toAbsolutePath().normalize();
            Path modPoo = projectRoot.resolve("modulo-poo").normalize();
            Path modBbdd = projectRoot.resolve("modulo-bbdd").normalize();
            if (PlatformSupport.isSubPath(enunParent, modPoo)) {
                return trabajo.resolve("modulo-poo").resolve(enunParent.getFileName());
            }
            if (PlatformSupport.isSubPath(enunParent, modBbdd)) {
                return trabajo.resolve("modulo-bbdd").resolve(modBbdd.relativize(enunParent));
            }
        }
        return trabajo.resolve(exercise.id());
    }

    /** Archivo principal de trabajo (SQL/Markdown); para Java es el directorio. */
    public static Path primaryFile(Exercise exercise, Path projectRoot) {
        Path dir = directory(exercise, projectRoot);
        if (exercise.solutionType() == Exercise.SolutionType.JAVA) {
            return dir;
        }
        Path solution = exercise.solutionPath();
        if (solution != null && !solution.toString().isEmpty() && Files.isRegularFile(solution)) {
            return dir.resolve(solution.getFileName());
        }
        return dir.resolve("mi_trabajo.md");
    }

    public static boolean exists(Exercise exercise, Path projectRoot) {
        Path dir = directory(exercise, projectRoot);
        if (!Files.exists(dir)) {
            return false;
        }
        if (exercise.solutionType() == Exercise.SolutionType.JAVA) {
            try (Stream<Path> s = Files.walk(dir)) {
                return s.anyMatch(p -> p.toString().endsWith(".java"));
            } catch (IOException e) {
                return false;
            }
        }
        Path file = primaryFile(exercise, projectRoot);
        return Files.isRegularFile(file);
    }

    public static void ensureDirectory(Exercise exercise, Path projectRoot) throws IOException {
        Files.createDirectories(directory(exercise, projectRoot));
    }

    public static boolean isEmpty(Exercise exercise, Path projectRoot) {
        return !exists(exercise, projectRoot);
    }

    public static String readContent(Exercise exercise, Path projectRoot) throws IOException {
        if (exercise.solutionType() == Exercise.SolutionType.JAVA) {
            Path dir = directory(exercise, projectRoot);
            if (!Files.isDirectory(dir)) {
                return "";
            }
            return SolutionReader.readDirectoryContent(dir);
        }
        Path file = primaryFile(exercise, projectRoot);
        if (!Files.isRegularFile(file)) {
            return "";
        }
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    public static void save(Exercise exercise, Path projectRoot, String content) throws IOException {
        if (exercise.solutionType() == Exercise.SolutionType.JAVA) {
            SolutionWriter.saveToDirectory(directory(exercise, projectRoot), content);
        } else {
            Path file = primaryFile(exercise, projectRoot);
            Files.createDirectories(file.getParent());
            Files.writeString(file, SolutionWriter.ensureTrailingNewline(content), StandardCharsets.UTF_8);
        }
    }

    /** Copia la solución oficial al espacio del alumno (solo si está vacío o con confirmación externa). */
    public static void copyFromOfficialSolution(Exercise exercise, Path projectRoot) throws IOException {
        Path target = directory(exercise, projectRoot);
        Files.createDirectories(target);
        Path official = exercise.solutionPath();
        if (official == null || official.toString().isEmpty()) {
            return;
        }
        if (Files.isDirectory(official)) {
            try (Stream<Path> walk = Files.walk(official)) {
                for (Path src : walk.filter(Files::isRegularFile).toList()) {
                    Path rel = official.relativize(src);
                    Path dest = target.resolve(rel);
                    Path parent = dest.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(src, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } else if (Files.isRegularFile(official)) {
            Files.copy(official, primaryFile(exercise, projectRoot),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void createJavaStub(Exercise exercise, Path projectRoot) throws IOException {
        Path dir = directory(exercise, projectRoot);
        Files.createDirectories(dir);
        String mainClass = exercise.mainClassName().orElse("Main");
        Path mainFile = dir.resolve(mainClass + ".java");
        if (!Files.exists(mainFile)) {
            String stub = """
                    public class %s {
                        public static void main(String[] args) {
                            // TODO: resuelve el ejercicio aquí
                        }
                    }
                    """.formatted(mainClass);
            Files.writeString(mainFile, stub, StandardCharsets.UTF_8);
        }
    }
}
