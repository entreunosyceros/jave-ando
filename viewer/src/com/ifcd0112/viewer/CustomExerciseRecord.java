package com.ifcd0112.viewer;

import java.util.Optional;

/** Datos persistidos de un ejercicio añadido por el usuario. */
public record CustomExerciseRecord(
        String id,
        String title,
        String module,
        String enunciadoPath,
        String solutionPath,
        Exercise.SolutionType solutionType,
        String mainClassName
) {
    public Optional<String> mainClassOptional() {
        return Optional.ofNullable(mainClassName).filter(s -> !s.isBlank());
    }
}
