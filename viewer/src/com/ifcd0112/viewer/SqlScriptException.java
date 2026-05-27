package com.ifcd0112.viewer;

import java.io.IOException;

/** El script SQL falló tras ejecutar una o más sentencias (autocommit: sin rollback del bloque). */
public final class SqlScriptException extends IOException {

    private final String consoleOutput;
    private final int completedStatements;
    private final int totalStatements;

    public SqlScriptException(
            String message, String consoleOutput, int completedStatements, int totalStatements) {
        super(message);
        this.consoleOutput = consoleOutput;
        this.completedStatements = completedStatements;
        this.totalStatements = totalStatements;
    }

    public String consoleOutput() {
        return consoleOutput;
    }

    public int completedStatements() {
        return completedStatements;
    }

    public int totalStatements() {
        return totalStatements;
    }

    public boolean isPartial() {
        return completedStatements > 0 && completedStatements < totalStatements;
    }
}
