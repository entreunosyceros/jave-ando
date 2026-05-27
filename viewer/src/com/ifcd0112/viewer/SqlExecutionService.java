package com.ifcd0112.viewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Ejecuta scripts .sql contra la base configurada en database.properties. */
public final class SqlExecutionService {

    private static final int TIMEOUT_SECONDS = 15;

    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "sql-exec");
        t.setDaemon(true);
        return t;
    });

    public void executeSqlFile(Path sqlFile, Path projectRoot, StringBuilder output) throws Exception {
        if (!Files.isRegularFile(sqlFile)) {
            throw new IOException("No existe el archivo SQL: " + sqlFile);
        }
        String sql = Files.readString(sqlFile, StandardCharsets.UTF_8);
        executeScript(sql, sqlFile.getFileName().toString(), projectRoot, output);
    }

    public void executeScript(String sql, String label, Path projectRoot, StringBuilder output)
            throws Exception {
        Properties props = DatabaseConfig.load(projectRoot);
        DatabaseConfig.registerDriver(projectRoot, props);
        List<String> statements = splitStatements(sql);
        if (statements.isEmpty()) {
            output.append("No hay sentencias SQL en ").append(label).append(".\n");
            return;
        }

        Callable<Void> task = () -> {
            try (Connection conn = DatabaseConfig.openConnectionForScript(props, statements);
                    Statement stmt = conn.createStatement()) {
                conn.setAutoCommit(true);
                stmt.setQueryTimeout(TIMEOUT_SECONDS);
                if (DatabaseConfig.usesServerConnectionForScript(props, statements)) {
                    output.append("Conexión al servidor (sin base en la URL) para permitir CREATE/USE.\n");
                }
                output.append("Modo: autocommit activo — cada sentencia se confirma al ejecutarse.\n");
                output.append("Si el script falla a medias, lo ya aplicado NO se revierte automáticamente.\n\n");

                int total = statements.size();
                int index = 0;
                for (String statement : statements) {
                    index++;
                    String preview = preview(statement);
                    output.append("--- Sentencia ").append(index).append(" de ").append(total).append(" ---\n");
                    output.append(preview).append("\n");
                    try {
                        runStatement(stmt, statement, output);
                        output.append("\n");
                    } catch (SQLException ex) {
                        output.append("ERROR: ").append(ex.getMessage()).append("\n\n");
                        appendPartialWarning(output, index, total);
                        throw new SqlScriptException(
                                "Error en la sentencia " + index + " de " + total,
                                output.toString(),
                                index - 1,
                                total
                        );
                    }
                }
                output.append("--- Fin del script (").append(total).append(" sentencias) ---\n");
            }
            return null;
        };

        Future<Void> future = executor.submit(task);
        try {
            future.get(TIMEOUT_SECONDS + 5L, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new IOException("Tiempo de espera agotado ejecutando SQL (" + TIMEOUT_SECONDS + " s).");
        } catch (java.util.concurrent.ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof Exception ex) {
                throw ex;
            }
            throw new IOException(cause.getMessage(), cause);
        }
    }

    /** Ejecuta sentencias en una conexión ya abierta (consola SQL interactiva). */
    public static void executeOnConnection(
            Connection conn, List<String> statements, StringBuilder output, int timeoutSeconds)
            throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(timeoutSeconds);
            int index = 0;
            for (String statement : statements) {
                index++;
                output.append("--- ").append(index).append(" ---\n");
                output.append(preview(statement)).append("\n");
                runStatement(stmt, statement, output);
                output.append("\n");
            }
        }
    }

    static void runStatement(Statement stmt, String sql, StringBuilder output)
            throws SQLException {
        boolean hasResult = stmt.execute(sql);
        int updateCount = stmt.getUpdateCount();
        while (true) {
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    formatResultSet(rs, output);
                }
            } else if (updateCount >= 0) {
                output.append("OK — filas afectadas: ").append(updateCount).append("\n");
            } else {
                output.append("OK\n");
            }
            hasResult = stmt.getMoreResults();
            updateCount = stmt.getUpdateCount();
            if (!hasResult && updateCount == -1) {
                break;
            }
        }
    }

    private static void formatResultSet(ResultSet rs, StringBuilder output) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        if (cols == 0) {
            output.append("(sin columnas)\n");
            return;
        }
        List<String> headers = new ArrayList<>();
        int[] widths = new int[cols];
        for (int c = 1; c <= cols; c++) {
            String name = meta.getColumnLabel(c);
            headers.add(name);
            widths[c - 1] = name.length();
        }
        List<List<String>> rows = new ArrayList<>();
        while (rs.next()) {
            List<String> row = new ArrayList<>();
            for (int c = 1; c <= cols; c++) {
                String val = rs.getString(c);
                if (val == null) {
                    val = "NULL";
                }
                row.add(val);
                widths[c - 1] = Math.max(widths[c - 1], val.length());
            }
            rows.add(row);
        }
        if (rows.isEmpty()) {
            output.append("(0 filas)\n");
            return;
        }
        appendRow(output, headers, widths);
        for (int i = 0; i < cols; i++) {
            for (int w = 0; w < widths[i]; w++) {
                output.append('-');
            }
            if (i < cols - 1) {
                output.append("  ");
            }
        }
        output.append("\n");
        for (List<String> row : rows) {
            appendRow(output, row, widths);
        }
        output.append(rows.size()).append(" fila(s)\n");
    }

    private static void appendRow(StringBuilder output, List<String> cells, int[] widths) {
        for (int i = 0; i < cells.size(); i++) {
            String cell = cells.get(i);
            output.append(cell);
            for (int p = cell.length(); p < widths[i]; p++) {
                output.append(' ');
            }
            if (i < cells.size() - 1) {
                output.append("  ");
            }
        }
        output.append("\n");
    }

    static List<String> splitStatements(String sql) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if (!inSingle && !inDouble && ch == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                while (i < sql.length() && sql.charAt(i) != '\n') {
                    i++;
                }
                current.append('\n');
                continue;
            }
            if (ch == '\'' && !inDouble) {
                inSingle = !inSingle;
            } else if (ch == '"' && !inSingle) {
                inDouble = !inDouble;
            }
            if (!inSingle && !inDouble && ch == ';') {
                String stmt = current.toString().trim();
                if (!stmt.isEmpty()) {
                    result.add(stmt);
                }
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        String last = current.toString().trim();
        if (!last.isEmpty()) {
            result.add(last);
        }
        return result;
    }

    private static void appendPartialWarning(StringBuilder output, int failedAt, int total) {
        int applied = failedAt - 1;
        output.append("*** AVISO: script detenido a medias ***\n");
        if (applied <= 0) {
            output.append("Ninguna sentencia llegó a completarse correctamente antes del error.\n");
        } else if (applied == 1) {
            output.append("La sentencia 1 se aplicó correctamente; la ").append(failedAt)
                    .append(" falló.\n");
        } else {
            output.append("Las sentencias 1 a ").append(applied)
                    .append(" se aplicaron correctamente; la ").append(failedAt).append(" falló.\n");
        }
        output.append("Con autocommit no hay rollback automático del visor: revisa la base o ejecuta\n");
        output.append("ROLLBACK/COMMIT manual solo si tu script abrió una transacción explícita (START TRANSACTION…).\n");
        output.append("Sentencias pendientes: ").append(total - failedAt + 1).append(" de ").append(total).append(".\n");
    }

    private static String preview(String sql) {
        String oneLine = sql.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        if (oneLine.length() > 120) {
            return oneLine.substring(0, 117) + "...";
        }
        return oneLine;
    }
}
