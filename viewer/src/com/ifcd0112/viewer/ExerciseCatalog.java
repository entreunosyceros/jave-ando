package com.ifcd0112.viewer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ExerciseCatalog {

    private final Path projectRoot;
    private final List<Exercise> exercises = new ArrayList<>();
    private final Set<String> customIds = new HashSet<>();
    private final Set<String> hiddenIds = new HashSet<>();

    public ExerciseCatalog(Path projectRoot) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        loadHidden();
        loadBuiltin();
        loadCustom();
    }

    public Path projectRoot() {
        return projectRoot;
    }

    public List<Exercise> getAll() {
        return List.copyOf(exercises);
    }

    public List<Exercise> getCustomExercises() {
        return exercises.stream().filter(ex -> customIds.contains(ex.id())).toList();
    }

    public boolean isCustom(String id) {
        return customIds.contains(id);
    }

    public boolean isHidden(String id) {
        return hiddenIds.contains(id);
    }

    public boolean idExists(String id) {
        return exercises.stream().anyMatch(ex -> ex.id().equals(id));
    }

    public Optional<Exercise> findById(String id) {
        return exercises.stream().filter(ex -> ex.id().equals(id)).findFirst();
    }

    /** Ejercicios del curso que están ocultos y pueden restaurarse. */
    public List<Exercise> getRestorableHidden() {
        return buildBuiltinTemplates().stream()
                .filter(ex -> hiddenIds.contains(ex.id()))
                .toList();
    }

    public void addCustom(CustomExerciseRecord record) throws IOException {
        validateRecord(record);
        if (idExists(record.id()) || hiddenIds.contains(record.id())) {
            throw new IOException("Ya existe un ejercicio con el id: " + record.id());
        }
        CustomExerciseStore.save(projectRoot, record);
        customIds.add(record.id());
        exercises.add(toExercise(record));
    }

    /**
     * Quita el ejercicio del índice.
     * Los añadidos por el usuario: borra su .properties.
     * Los del curso: se ocultan (archivos intactos; se pueden restaurar).
     */
    public void removeFromIndex(String id) throws IOException {
        if (!idExists(id)) {
            throw new IOException("No hay ningún ejercicio con el id: " + id);
        }
        exercises.removeIf(ex -> ex.id().equals(id));
        if (customIds.contains(id)) {
            CustomExerciseStore.delete(projectRoot, id);
            customIds.remove(id);
        } else {
            HiddenExerciseStore.hide(projectRoot, id);
            hiddenIds.add(id);
        }
    }

    public void restoreHidden(String id) throws IOException {
        if (!hiddenIds.contains(id)) {
            throw new IOException("Este ejercicio no está oculto: " + id);
        }
        Exercise builtin = buildBuiltinTemplates().stream()
                .filter(ex -> ex.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IOException("No se puede restaurar: " + id));
        HiddenExerciseStore.unhide(projectRoot, id);
        hiddenIds.remove(id);
        exercises.add(builtin);
    }

    private void loadHidden() {
        try {
            hiddenIds.addAll(HiddenExerciseStore.loadHiddenIds(projectRoot));
        } catch (IOException ignored) {
            // sin archivo de ocultos
        }
    }

    private void loadCustom() {
        try {
            loadCustomFromDisk();
        } catch (IOException ignored) {
            // sin carpeta o sin permisos
        }
    }

    private void loadCustomFromDisk() throws IOException {
        for (CustomExerciseRecord record : CustomExerciseStore.loadAll(projectRoot)) {
            if (idExists(record.id()) || hiddenIds.contains(record.id())) {
                continue;
            }
            customIds.add(record.id());
            exercises.add(toExercise(record));
        }
    }

    private void validateRecord(CustomExerciseRecord record) throws IOException {
        if (record.id() == null || !record.id().matches("[a-zA-Z0-9][a-zA-Z0-9._-]*")) {
            throw new IOException("El id debe empezar por letra o número y solo usar letras, números, . _ -");
        }
        if (record.title() == null || record.title().isBlank()) {
            throw new IOException("El título es obligatorio.");
        }
        Path enunciado = resolveRelative(record.enunciadoPath());
        if (!Files.isRegularFile(enunciado)) {
            throw new IOException("No se encuentra el enunciado: " + enunciado);
        }
        if (!enunciado.toString().toLowerCase().endsWith(".md")) {
            throw new IOException("El enunciado debe ser un archivo .md");
        }
        if (record.solutionPath() != null && !record.solutionPath().isBlank()) {
            Path sol = resolveRelative(record.solutionPath());
            if (!Files.exists(sol)) {
                throw new IOException("No se encuentra la solución: " + sol);
            }
        }
    }

    private Exercise toExercise(CustomExerciseRecord record) {
        Path enunciado = resolveRelative(record.enunciadoPath());
        Path solution = record.solutionPath() == null || record.solutionPath().isBlank()
                ? Path.of("")
                : resolveRelative(record.solutionPath());
        return new Exercise(
                record.id(),
                record.title(),
                record.module(),
                enunciado,
                solution,
                record.solutionType(),
                record.mainClassOptional(),
                Optional.empty()
        );
    }

    private Path resolveRelative(String relative) {
        return projectRoot.resolve(relative.replace('\\', '/')).normalize();
    }

    private void loadBuiltin() {
        for (Exercise ex : buildBuiltinTemplates()) {
            if (!hiddenIds.contains(ex.id())) {
                exercises.add(ex);
            }
        }
    }

    private List<Exercise> buildBuiltinTemplates() {
        List<Exercise> list = new ArrayList<>();
        Path sol = projectRoot.resolve("soluciones");
        Path mod = projectRoot.resolve("modulo-poo");
        Path bbdd = projectRoot.resolve("modulo-bbdd");

        list.add(builtin("poo-01", "01 — Clases y objetos", "Programación orientada a objetos",
                mod.resolve("01-clases-y-objetos/ENUNCIADO.md"),
                sol.resolve("modulo-poo/01-clases-y-objetos"),
                Exercise.SolutionType.JAVA, "Main"));

        list.add(builtin("poo-02", "02 — Encapsulación", "Programación orientada a objetos",
                mod.resolve("02-encapsulacion/ENUNCIADO.md"),
                sol.resolve("modulo-poo/02-encapsulacion"),
                Exercise.SolutionType.JAVA, "Main"));

        list.add(builtin("poo-03", "03 — Constructores", "Programación orientada a objetos",
                mod.resolve("03-constructores/ENUNCIADO.md"),
                sol.resolve("modulo-poo/03-constructores"),
                Exercise.SolutionType.JAVA, "Main"));

        list.add(builtin("poo-04", "04 — Herencia", "Programación orientada a objetos",
                mod.resolve("04-herencia/ENUNCIADO.md"),
                sol.resolve("modulo-poo/04-herencia"),
                Exercise.SolutionType.JAVA, "Main"));

        list.add(builtin("poo-05", "05 — Polimorfismo", "Programación orientada a objetos",
                mod.resolve("05-polimorfismo/ENUNCIADO.md"),
                sol.resolve("modulo-poo/05-polimorfismo"),
                Exercise.SolutionType.JAVA, "Main"));

        list.add(builtin("poo-06", "06 — Abstractas e interfaces", "Programación orientada a objetos",
                mod.resolve("06-abstractas-interfaces/ENUNCIADO.md"),
                sol.resolve("modulo-poo/06-abstractas-interfaces"),
                Exercise.SolutionType.JAVA, "Main"));

        list.add(builtin("poo-07", "07 — Colecciones", "Programación orientada a objetos",
                mod.resolve("07-colecciones/ENUNCIADO.md"),
                sol.resolve("modulo-poo/07-colecciones"),
                Exercise.SolutionType.JAVA, "Main"));

        list.add(builtin("poo-08", "08 — Excepciones", "Programación orientada a objetos",
                mod.resolve("08-excepciones/ENUNCIADO.md"),
                sol.resolve("modulo-poo/08-excepciones"),
                Exercise.SolutionType.JAVA, "Main"));

        list.add(builtin("bbdd-01", "01 — Diseño E-R", "Bases de datos",
                bbdd.resolve("01-diseno-er/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/01-diseno-er/SOLUCION.md"),
                Exercise.SolutionType.MARKDOWN, null,
                projectRoot.resolve("sql/01_crear_base_datos.sql")));

        list.add(builtin("bbdd-02", "02 — DDL", "Bases de datos",
                bbdd.resolve("02-ddl/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/02-ddl/crear_academia.sql"),
                Exercise.SolutionType.SQL, null));

        list.add(builtin("bbdd-03", "03 — Consultas SELECT", "Bases de datos",
                bbdd.resolve("03-consultas-select/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/03-consultas-select/consultas_select.sql"),
                Exercise.SolutionType.SQL, null));

        list.add(builtin("bbdd-04", "04 — Manipulación DML", "Bases de datos",
                bbdd.resolve("04-manipulacion-datos/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/04-manipulacion-datos/manipulacion_datos.sql"),
                Exercise.SolutionType.SQL, null));

        list.add(builtin("bbdd-05", "05 — JOINs", "Bases de datos",
                bbdd.resolve("05-joins/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/05-joins/joins_subconsultas.sql"),
                Exercise.SolutionType.SQL, null));

        list.add(builtin("jdbc-01", "01 — Conexión JDBC", "Acceso a datos con JDBC",
                bbdd.resolve("jdbc/01-conexion-basica/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/jdbc/01-conexion-basica"),
                Exercise.SolutionType.JAVA, "ListarAlumnosDemo"));

        list.add(builtin("jdbc-02", "02 — CRUD JDBC", "Acceso a datos con JDBC",
                bbdd.resolve("jdbc/02-crud/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/jdbc/02-crud"),
                Exercise.SolutionType.JAVA, "Main"));

        list.add(builtin("jdbc-03", "03 — Transacciones JDBC", "Acceso a datos con JDBC",
                bbdd.resolve("jdbc/03-transacciones/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/jdbc/03-transacciones"),
                Exercise.SolutionType.JAVA, "Main"));

        list.add(builtin("proj-01", "Proyecto integrador — Biblioteca", "Proyecto integrador",
                bbdd.resolve("proyecto-integrador/ENUNCIADO.md"),
                Path.of(""),
                Exercise.SolutionType.NONE, null));

        return list;
    }

    private static Exercise builtin(String id, String title, String module, Path enunciado, Path solution,
                                    Exercise.SolutionType type, String mainClass) {
        return builtin(id, title, module, enunciado, solution, type, mainClass, null);
    }

    private static Exercise builtin(String id, String title, String module, Path enunciado, Path solution,
                                    Exercise.SolutionType type, String mainClass, Path sqlRun) {
        return new Exercise(
                id, title, module,
                enunciado, solution, type,
                Optional.ofNullable(mainClass),
                Optional.ofNullable(sqlRun)
        );
    }
}
