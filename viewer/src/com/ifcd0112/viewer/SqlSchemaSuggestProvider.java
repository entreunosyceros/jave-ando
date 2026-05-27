package com.ifcd0112.viewer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingWorker;

/** Carga tablas y columnas de la base activa para sugerencias SQL. */
public final class SqlSchemaSuggestProvider {

    private final Path projectRoot;
    private volatile List<String> schemaSuggestions = List.of();
    private SwingWorker<List<String>, Void> worker;

    public SqlSchemaSuggestProvider(Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    public List<String> suggestions() {
        return schemaSuggestions;
    }

    public void refresh() {
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }
        worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                if (!DatabaseConfig.configExists(projectRoot)) {
                    return List.of();
                }
                try {
                    String db = DatabaseConfig.activeDatabaseName(DatabaseConfig.load(projectRoot));
                    if (db == null || db.isBlank()) {
                        return List.of();
                    }
                    DatabaseSchemaService.DatabaseSchema schema =
                            DatabaseSchemaService.load(projectRoot, db);
                    return flatten(schema);
                } catch (Exception e) {
                    return List.of();
                }
            }

            @Override
            protected void done() {
                if (!isCancelled()) {
                    try {
                        schemaSuggestions = get();
                    } catch (Exception ignored) {
                        schemaSuggestions = List.of();
                    }
                }
            }
        };
        worker.execute();
    }

    private static List<String> flatten(DatabaseSchemaService.DatabaseSchema schema) {
        Set<String> out = new LinkedHashSet<>();
        for (DatabaseSchemaService.TableSchema table : schema.tables()) {
            String tableName = table.name();
            out.add(tableName);
            for (DatabaseSchemaService.ColumnSchema col : table.columns()) {
                out.add(tableName + "." + col.name());
                out.add(col.name());
            }
        }
        return new ArrayList<>(out);
    }

    public void cancel() {
        if (worker != null) {
            worker.cancel(true);
        }
    }
}
