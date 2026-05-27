package com.ifcd0112.viewer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Formateo ligero de sangría para Java y SQL (orientado a principiantes). */
public final class CodeFormatter {

    private static final int INDENT_SPACES = 4;
    private static final Pattern JAVA_FILE_MARKER =
            Pattern.compile("(?m)^// ===== [^\\n]+ =====\\s*$");

    private CodeFormatter() {}

    public static String formatJava(String source) {
        if (source == null || source.isBlank()) {
            return source != null ? source : "";
        }
        Matcher marker = JAVA_FILE_MARKER.matcher(source);
        if (!marker.find()) {
            return formatJavaBlock(source);
        }
        StringBuilder out = new StringBuilder();
        int last = 0;
        marker.reset();
        while (marker.find()) {
            if (marker.start() > last) {
                out.append(formatJavaBlock(source.substring(last, marker.start())));
            }
            out.append(source, marker.start(), marker.end()).append('\n');
            last = marker.end();
        }
        if (last < source.length()) {
            out.append(formatJavaBlock(source.substring(last)));
        }
        return ensureTrailingNewline(out.toString().stripTrailing());
    }

    public static String formatSql(String source) {
        if (source == null || source.isBlank()) {
            return source != null ? source : "";
        }
        String[] lines = source.split("\n", -1);
        StringBuilder out = new StringBuilder();
        int depth = 0;
        boolean prevBlank = false;
        for (String raw : lines) {
            String trimmed = raw.strip();
            if (trimmed.isEmpty()) {
                if (!prevBlank) {
                    out.append('\n');
                    prevBlank = true;
                }
                continue;
            }
            prevBlank = false;
            if (trimmed.startsWith("--")) {
                out.append(" ".repeat(Math.max(0, depth) * INDENT_SPACES)).append(trimmed).append('\n');
                continue;
            }
            int lineDepth = depth;
            if (trimmed.startsWith(")")) {
                lineDepth = Math.max(0, depth - 1);
            }
            BraceBalance balance = countDelimiters(trimmed, '(', ')', LineCommentStyle.SQL);
            out.append(" ".repeat(lineDepth * INDENT_SPACES)).append(trimmed).append('\n');
            depth = Math.max(0, depth + balance.net());
        }
        return ensureTrailingNewline(out.toString().stripTrailing());
    }

    private static String formatJavaBlock(String source) {
        String[] lines = source.split("\n", -1);
        StringBuilder out = new StringBuilder();
        int level = 0;
        boolean prevBlank = false;
        for (String raw : lines) {
            String trimmed = raw.strip();
            if (trimmed.isEmpty()) {
                if (!prevBlank) {
                    out.append('\n');
                    prevBlank = true;
                }
                continue;
            }
            prevBlank = false;
            if (trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                out.append(" ".repeat(Math.max(0, level) * INDENT_SPACES)).append(trimmed).append('\n');
                continue;
            }
            int lineLevel = level;
            if (startsWithClosingBrace(trimmed)) {
                lineLevel = Math.max(0, level - 1);
            }
            BraceBalance balance = countDelimiters(trimmed, '{', '}', LineCommentStyle.JAVA);
            out.append(" ".repeat(lineLevel * INDENT_SPACES)).append(trimmed).append('\n');
            level = Math.max(0, level + balance.net());
        }
        return ensureTrailingNewline(out.toString().stripTrailing());
    }

    private static boolean startsWithClosingBrace(String trimmed) {
        return trimmed.startsWith("}")
                || trimmed.startsWith("};")
                || trimmed.startsWith("},")
                || trimmed.startsWith("})");
    }

    private enum LineCommentStyle {
        /** Comentario de línea {@code //} (Java). */
        JAVA,
        /** Comentario de línea {@code --} o {@code //} (SQL). */
        SQL
    }

    /**
     * Cuenta delimitadores fuera de cadenas y comentarios de línea según el lenguaje.
     */
    private static BraceBalance countDelimiters(
            String line, char open, char close, LineCommentStyle commentStyle) {
        int openCount = 0;
        int closeCount = 0;
        boolean inString = false;
        boolean inChar = false;
        char stringQuote = 0;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (!inString && !inChar && isLineCommentAt(line, i, commentStyle)) {
                break;
            }
            if (inString) {
                if (ch == stringQuote && !isEscapedAt(line, i)) {
                    inString = false;
                }
                continue;
            }
            if (inChar) {
                if (ch == '\'' && !isEscapedAt(line, i)) {
                    inChar = false;
                }
                continue;
            }
            if (ch == '"') {
                inString = true;
                stringQuote = '"';
                continue;
            }
            if (ch == '\'') {
                inChar = true;
                continue;
            }
            if (ch == open) {
                openCount++;
            } else if (ch == close) {
                closeCount++;
            }
        }
        return new BraceBalance(openCount, closeCount);
    }

    private static boolean isLineCommentAt(String line, int i, LineCommentStyle style) {
        if (i + 1 >= line.length()) {
            return false;
        }
        if (line.charAt(i) == '/' && line.charAt(i + 1) == '/') {
            return true;
        }
        if (style == LineCommentStyle.SQL && line.charAt(i) == '-' && line.charAt(i + 1) == '-') {
            return true;
        }
        return false;
    }

    /**
     * {@code true} si el carácter en {@code index} está escapado por barras invertidas consecutivas
     * (número impar de {@code \} justo antes).
     */
    private static boolean isEscapedAt(String line, int index) {
        if (index <= 0) {
            return false;
        }
        int backslashes = 0;
        for (int j = index - 1; j >= 0 && line.charAt(j) == '\\'; j--) {
            backslashes++;
        }
        return (backslashes % 2) == 1;
    }

    private static String ensureTrailingNewline(String text) {
        if (text.isEmpty()) {
            return text;
        }
        return text + "\n";
    }

    private record BraceBalance(int open, int close) {
        int net() {
            return open - close;
        }
    }
}
