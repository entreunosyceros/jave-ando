package com.ifcd0112.viewer;

import java.nio.file.Path;
import java.util.Optional;

public record Exercise(
        String id,
        String title,
        String module,
        Path enunciadoPath,
        Path solutionPath,
        SolutionType solutionType,
        Optional<String> mainClassName
) {
    public enum SolutionType {
        JAVA,
        SQL,
        MARKDOWN,
        NONE
    }

    public boolean isRunnable() {
        return solutionType == SolutionType.JAVA && mainClassName.isPresent();
    }
}
