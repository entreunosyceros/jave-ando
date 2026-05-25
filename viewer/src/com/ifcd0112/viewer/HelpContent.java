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
                ## Cómo añadir y gestionar ejercicios

                ### Añadir (recomendado)

                1. Crea en el repositorio el **ENUNCIADO.md** y la **solución** (carpeta Java, `.sql`, `SOLUCION.md`, …).
                2. Menú **Ejercicios → Añadir ejercicio…**
                3. Id único, título, módulo del índice y tipo de solución.
                4. **Elegir…** el enunciado (`.md`) y la solución (archivo o carpeta).
                5. **Añadir al índice** — aparece al instante; se guarda en `ejercicios-personalizados/`.

                | Tipo | Solución | Ejecutar en el visor |
                |------|----------|----------------------|
                | Java | Carpeta con `.java` | Sí (clase con `main`) |
                | SQL | Archivo `.sql` | No |
                | Markdown | `.md` (p. ej. SOLUCION.md) | No |
                | Ninguno | Sin solución | No |

                ### Eliminar del índice

                - **Ejercicios → Eliminar ejercicio…** — quita el ejercicio del árbol.
                - Añadidos por ti: se borra su `.properties`.
                - Del curso (POO, BBDD…): solo se **oculta**; los archivos no se eliminan.
                - **Restaurar ejercicios ocultos…** — recupera los del curso que ocultaste.

                ### Estructura de archivos

                - Enunciado: p. ej. `modulo-poo/09-nombre/ENUNCIADO.md`
                - Solución Java: `soluciones/modulo-poo/09-nombre/*.java`
                - Solución SQL: `soluciones/modulo-bbdd/…/archivo.sql`

                ### Catálogo fijo (avanzado)

                Para ejercicios permanentes en el repo, edita `ExerciseCatalog.java` y recompila con `./viewer/run.sh`.
                """;
    }
}
