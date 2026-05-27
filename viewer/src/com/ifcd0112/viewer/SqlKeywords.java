package com.ifcd0112.viewer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Palabras reservadas y plantillas SQL para resaltado y autocompletado. */
public final class SqlKeywords {

    public static final Set<String> RESERVED = Set.of(
            "SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "CREATE", "TABLE",
            "DROP", "ALTER", "JOIN", "INNER", "LEFT", "RIGHT", "OUTER", "FULL", "CROSS", "ON",
            "GROUP", "BY", "ORDER", "HAVING", "AS", "AND", "OR", "NOT", "NULL", "VALUES", "SET",
            "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "CONSTRAINT", "INDEX", "VIEW", "UNION",
            "ALL", "DISTINCT", "COUNT", "AVG", "SUM", "MIN", "MAX", "LIMIT", "OFFSET", "USE",
            "DATABASE", "IF", "EXISTS", "COMMIT", "ROLLBACK", "START", "TRANSACTION", "BEGIN",
            "CURRENT_DATE", "DATE_SUB", "INTERVAL", "DUPLICATE", "LIKE", "IN", "BETWEEN", "IS",
            "CASE", "WHEN", "THEN", "ELSE", "END", "DESCRIBE", "SHOW", "GRANT", "REVOKE",
            "AUTO_INCREMENT", "DEFAULT", "ENGINE", "CHARSET", "UNSIGNED", "ZEROFILL", "MODIFY",
            "COLUMN", "ADD", "CHANGE", "RENAME", "TRUNCATE", "REPLACE", "EXPLAIN", "WITH"
    );

    private static final List<String> SNIPPETS = List.of(
            "SELECT * FROM ",
            "SELECT COUNT(*) FROM ",
            "INSERT INTO ",
            "UPDATE ",
            "DELETE FROM ",
            "CREATE TABLE ",
            "ALTER TABLE ",
            "LEFT JOIN ",
            "INNER JOIN ",
            "ORDER BY ",
            "GROUP BY ",
            "WHERE ",
            "AND ",
            "OR NOT "
    );

    private SqlKeywords() {}

    public static List<String> allForCompletion() {
        LinkedHashSet<String> out = new LinkedHashSet<>(RESERVED);
        out.addAll(SNIPPETS);
        return List.copyOf(out);
    }

    public static boolean isKeyword(String word) {
        return word != null && RESERVED.contains(word.toUpperCase(Locale.ROOT));
    }
}
