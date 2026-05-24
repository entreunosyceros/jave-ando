package com.ifcd0112.viewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HelpContent {

    private HelpContent() {}

    public static String loadAddExerciseGuide(Path projectRoot) {
        Path readme = projectRoot.resolve("README.md");
        if (Files.isRegularFile(readme)) {
            try {
                String all = Files.readString(readme, StandardCharsets.UTF_8);
                String section = extractSection(
                        all,
                        "## Cómo añadir nuevos ejercicios",
                        "## Orden recomendado"
                );
                if (!section.isBlank()) {
                    return section.trim();
                }
            } catch (IOException ignored) {
                // contenido integrado
            }
        }
        return fallbackGuide();
    }

    private static String extractSection(String markdown, String startHeading, String endHeading) {
        int start = markdown.indexOf(startHeading);
        if (start < 0) {
            return "";
        }
        int end = markdown.indexOf(endHeading, start + startHeading.length());
        if (end < 0) {
            return markdown.substring(start);
        }
        return markdown.substring(start, end);
    }

    private static String fallbackGuide() {
        return """
                ## Cómo añadir nuevos ejercicios

                Cada ejercicio necesita archivos en disco y una entrada en `ExerciseCatalog.java`.

                ### POO
                - Enunciado: `modulo-poo/NN-nombre/ENUNCIADO.md`
                - Solución: `soluciones/modulo-poo/NN-nombre/*.java`
                - Id: `poo-NN`, módulo: `Programación orientada a objetos`, tipo: `JAVA`, clase `Main`

                ### Bases de datos
                - Enunciado: `modulo-bbdd/NN-nombre/ENUNCIADO.md`
                - Solución SQL: archivo `.sql` en `soluciones/modulo-bbdd/`
                - Solución teoría: `SOLUCION.md`
                - Id: `bbdd-NN`, módulo: `Bases de datos`

                ### JDBC
                - Enunciado: `modulo-bbdd/jdbc/NN-nombre/ENUNCIADO.md`
                - Solución: carpeta Java en `soluciones/modulo-bbdd/jdbc/`
                - Id: `jdbc-NN`, módulo: `Acceso a datos con JDBC`

                Tras editar el catálogo, recompila: `./viewer/run.sh`
                """;
    }
}
