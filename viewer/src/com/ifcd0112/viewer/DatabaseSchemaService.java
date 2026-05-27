package com.ifcd0112.viewer;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Lee metadatos de tablas y claves foráneas para el visor de esquema. */
public final class DatabaseSchemaService {

    private static final int TABLE_PREVIEW_MAX_ROWS = 500;

    private DatabaseSchemaService() {}

    public static DatabaseSchema load(Path projectRoot, String databaseName) throws Exception {
        Properties props = DatabaseConfig.load(projectRoot);
        DatabaseConfig.registerDriver(projectRoot, props);
        String url = DatabaseConfig.withDatabaseInUrl(props.getProperty("jdbc.url"), databaseName);
        try (Connection conn = java.sql.DriverManager.getConnection(
                url, props.getProperty("jdbc.user"), props.getProperty("jdbc.password"))) {
            return loadFromConnection(conn, databaseName);
        }
    }

    public static DatabaseSchema loadFromConnection(Connection conn, String databaseName) throws SQLException {
        Map<String, TableSchema> tables = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                """
                SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, COLUMN_KEY
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = ?
                ORDER BY TABLE_NAME, ORDINAL_POSITION
                """)) {
            ps.setString(1, databaseName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String table = rs.getString("TABLE_NAME");
                    String column = rs.getString("COLUMN_NAME");
                    String type = rs.getString("COLUMN_TYPE");
                    boolean pk = "PRI".equalsIgnoreCase(rs.getString("COLUMN_KEY"));
                    tables.computeIfAbsent(table, TableSchema::new)
                            .columns.add(new ColumnSchema(column, type, pk));
                }
            }
        }

        List<ForeignKeySchema> foreignKeys = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                """
                SELECT TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
                FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = ? AND REFERENCED_TABLE_NAME IS NOT NULL
                ORDER BY TABLE_NAME, COLUMN_NAME
                """)) {
            ps.setString(1, databaseName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    foreignKeys.add(new ForeignKeySchema(
                            rs.getString("TABLE_NAME"),
                            rs.getString("COLUMN_NAME"),
                            rs.getString("REFERENCED_TABLE_NAME"),
                            rs.getString("REFERENCED_COLUMN_NAME")
                    ));
                }
            }
        }

        return new DatabaseSchema(databaseName, new ArrayList<>(tables.values()), foreignKeys);
    }

    public static TablePreview loadTablePreview(Path projectRoot, String databaseName, String tableName)
            throws Exception {
        validateTableName(tableName);
        Properties props = DatabaseConfig.load(projectRoot);
        DatabaseConfig.registerDriver(projectRoot, props);
        String url = DatabaseConfig.withDatabaseInUrl(props.getProperty("jdbc.url"), databaseName);
        try (Connection conn = java.sql.DriverManager.getConnection(
                url, props.getProperty("jdbc.user"), props.getProperty("jdbc.password"))) {
            return loadTablePreviewFromConnection(conn, tableName, TABLE_PREVIEW_MAX_ROWS);
        }
    }

    static TablePreview loadTablePreviewFromConnection(Connection conn, String tableName, int maxRows)
            throws SQLException {
        String sql = "SELECT * FROM `" + tableName.replace("`", "``") + "` LIMIT " + maxRows;
        try (var stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            var meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            List<String> headers = new ArrayList<>();
            for (int c = 1; c <= cols; c++) {
                headers.add(meta.getColumnLabel(c));
            }
            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int c = 1; c <= cols; c++) {
                    Object val = rs.getObject(c);
                    row[c - 1] = val == null ? "NULL" : val;
                }
                rows.add(row);
            }
            return new TablePreview(tableName, headers, rows, rows.size(), maxRows);
        }
    }

    private static void validateTableName(String tableName) throws SQLException {
        if (tableName == null || !tableName.matches("[A-Za-z0-9_]+")) {
            throw new SQLException("Nombre de tabla no válido: " + tableName);
        }
    }

    public record TablePreview(
            String tableName,
            List<String> columnNames,
            List<Object[]> rows,
            int rowCount,
            int maxRows
    ) {}

    public record DatabaseSchema(String databaseName, List<TableSchema> tables, List<ForeignKeySchema> foreignKeys) {}

    public record TableSchema(String name, List<ColumnSchema> columns) {
        TableSchema(String name) {
            this(name, new ArrayList<>());
        }
    }

    public record ColumnSchema(String name, String sqlType, boolean primaryKey) {}

    public record ForeignKeySchema(String fromTable, String fromColumn, String toTable, String toColumn) {}
}
