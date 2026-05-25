package com.ifcd0112.viewer;

/**
 * Error o aviso asociado a una línea del buffer del editor (numeración desde 1).
 */
public record CodeDiagnostic(
        int line,
        String message,
        Severity severity,
        String sourceFile
) {
    public enum Severity {
        ERROR,
        WARNING
    }

    public CodeDiagnostic(int line, String message) {
        this(line, message, Severity.ERROR, null);
    }
}
