package com.ifcd0112.viewer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ExerciseCatalog {

    private final List<Exercise> exercises = new ArrayList<>();

    public ExerciseCatalog(Path projectRoot) {
        Path sol = projectRoot.resolve("soluciones");
        Path mod = projectRoot.resolve("modulo-poo");
        Path bbdd = projectRoot.resolve("modulo-bbdd");

        add("poo-01", "01 — Clases y objetos", "Programación orientada a objetos",
                mod.resolve("01-clases-y-objetos/ENUNCIADO.md"),
                sol.resolve("modulo-poo/01-clases-y-objetos"),
                Exercise.SolutionType.JAVA, "Main");

        add("poo-02", "02 — Encapsulación", "Programación orientada a objetos",
                mod.resolve("02-encapsulacion/ENUNCIADO.md"),
                sol.resolve("modulo-poo/02-encapsulacion"),
                Exercise.SolutionType.JAVA, "Main");

        add("poo-03", "03 — Constructores", "Programación orientada a objetos",
                mod.resolve("03-constructores/ENUNCIADO.md"),
                sol.resolve("modulo-poo/03-constructores"),
                Exercise.SolutionType.JAVA, "Main");

        add("poo-04", "04 — Herencia", "Programación orientada a objetos",
                mod.resolve("04-herencia/ENUNCIADO.md"),
                sol.resolve("modulo-poo/04-herencia"),
                Exercise.SolutionType.JAVA, "Main");

        add("poo-05", "05 — Polimorfismo", "Programación orientada a objetos",
                mod.resolve("05-polimorfismo/ENUNCIADO.md"),
                sol.resolve("modulo-poo/05-polimorfismo"),
                Exercise.SolutionType.JAVA, "Main");

        add("poo-06", "06 — Abstractas e interfaces", "Programación orientada a objetos",
                mod.resolve("06-abstractas-interfaces/ENUNCIADO.md"),
                sol.resolve("modulo-poo/06-abstractas-interfaces"),
                Exercise.SolutionType.JAVA, "Main");

        add("poo-07", "07 — Colecciones", "Programación orientada a objetos",
                mod.resolve("07-colecciones/ENUNCIADO.md"),
                sol.resolve("modulo-poo/07-colecciones"),
                Exercise.SolutionType.JAVA, "Main");

        add("poo-08", "08 — Excepciones", "Programación orientada a objetos",
                mod.resolve("08-excepciones/ENUNCIADO.md"),
                sol.resolve("modulo-poo/08-excepciones"),
                Exercise.SolutionType.JAVA, "Main");

        add("bbdd-01", "01 — Diseño E-R", "Bases de datos",
                bbdd.resolve("01-diseno-er/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/01-diseno-er/SOLUCION.md"),
                Exercise.SolutionType.MARKDOWN, null);

        add("bbdd-02", "02 — DDL", "Bases de datos",
                bbdd.resolve("02-ddl/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/02-ddl/crear_academia.sql"),
                Exercise.SolutionType.SQL, null);

        add("bbdd-03", "03 — Consultas SELECT", "Bases de datos",
                bbdd.resolve("03-consultas-select/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/03-consultas-select/consultas_select.sql"),
                Exercise.SolutionType.SQL, null);

        add("bbdd-04", "04 — Manipulación DML", "Bases de datos",
                bbdd.resolve("04-manipulacion-datos/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/04-manipulacion-datos/manipulacion_datos.sql"),
                Exercise.SolutionType.SQL, null);

        add("bbdd-05", "05 — JOINs", "Bases de datos",
                bbdd.resolve("05-joins/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/05-joins/joins_subconsultas.sql"),
                Exercise.SolutionType.SQL, null);

        add("jdbc-01", "01 — Conexión JDBC", "Acceso a datos con JDBC",
                bbdd.resolve("jdbc/01-conexion-basica/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/jdbc/01-conexion-basica"),
                Exercise.SolutionType.JAVA, "ListarAlumnosDemo");

        add("jdbc-02", "02 — CRUD JDBC", "Acceso a datos con JDBC",
                bbdd.resolve("jdbc/02-crud/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/jdbc/02-crud"),
                Exercise.SolutionType.JAVA, "Main");

        add("jdbc-03", "03 — Transacciones JDBC", "Acceso a datos con JDBC",
                bbdd.resolve("jdbc/03-transacciones/ENUNCIADO.md"),
                sol.resolve("modulo-bbdd/jdbc/03-transacciones"),
                Exercise.SolutionType.JAVA, "Main");

        add("proj-01", "Proyecto integrador — Biblioteca", "Proyecto integrador",
                bbdd.resolve("proyecto-integrador/ENUNCIADO.md"),
                Path.of(""),
                Exercise.SolutionType.NONE, null);
    }

    private void add(String id, String title, String module, Path enunciado, Path solution,
                     Exercise.SolutionType type, String mainClass) {
        exercises.add(new Exercise(
                id, title, module,
                enunciado, solution, type,
                Optional.ofNullable(mainClass)
        ));
    }

    public List<Exercise> getAll() {
        return List.copyOf(exercises);
    }
}
