package com.ifcd0112.viewer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public record Exercise(
        String id,
        String title,
        String module,
        Path enunciadoPath,
        Path solutionPath,
        SolutionType solutionType,
        Optional<String> mainClassName,
        /** Script SQL ejecutable aunque la solución sea Markdown (p. ej. crear la base del curso). */
        Optional<Path> sqlRunPath
) {
    public enum SolutionType {
        JAVA,
        SQL,
        MARKDOWN,
        NONE
    }

    public boolean isJavaRunnable() {
        return solutionType == SolutionType.JAVA && mainClassName.isPresent();
    }

    public boolean isSqlRunnable() {
        if (solutionType != SolutionType.SQL || solutionPath == null) {
            return false;
        }
        String name = solutionPath.getFileName().toString().toLowerCase();
        return name.endsWith(".sql");
    }

    /** Hay un .sql asociado para ejecutar (solución SQL o script auxiliar del curso). */
    public boolean hasSqlRunScript() {
        if (isSqlRunnable()) {
            return true;
        }
        return sqlRunPath.filter(Files::isRegularFile).isPresent();
    }

    /** Ruta del script a ejecutar con JDBC (solución .sql o sqlRunPath). */
    public Optional<Path> sqlExecutionPath() {
        if (isSqlRunnable()) {
            return Optional.of(solutionPath);
        }
        return sqlRunPath.filter(Files::isRegularFile);
    }

    /** Java (compilar/ejecutar) o SQL (script JDBC en consola del visor). */
    public boolean isRunnable() {
        return isJavaRunnable() || hasSqlRunScript();
    }
}
