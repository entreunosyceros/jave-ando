package com.ifcd0112.viewer;

import java.util.ArrayList;
import java.util.List;

/** Comprobaciones ligeras sin compilador (complementan {@link CodeDiagnosticsService}). */
final class SyntaxHints {

    private SyntaxHints() {}

    static List<CodeDiagnostic> scan(String content, boolean javaLike) {
        List<CodeDiagnostic> out = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return out;
        }
        String[] lines = content.split("\n", -1);
        int braces = 0;
        int parens = 0;
        int brackets = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNo = i + 1;
            String trimmed = line.trim();

            if (javaLike && trimmed.startsWith("// ===== ") && trimmed.endsWith(" =====")) {
                continue;
            }
            if (trimmed.startsWith("//") || trimmed.startsWith("--") || trimmed.startsWith("/*")) {
                continue;
            }

            boolean inString = false;
            char stringChar = 0;
            for (int c = 0; c < line.length(); c++) {
                char ch = line.charAt(c);
                if (inString) {
                    if (ch == stringChar && line.charAt(c - 1) != '\\') {
                        inString = false;
                    }
                    continue;
                }
                if (ch == '"' || ch == '\'') {
                    inString = true;
                    stringChar = ch;
                    continue;
                }
                switch (ch) {
                    case '{' -> braces++;
                    case '}' -> braces--;
                    case '(' -> parens++;
                    case ')' -> parens--;
                    case '[' -> brackets++;
                    case ']' -> brackets--;
                    default -> { }
                }
                if (braces < 0) {
                    out.add(new CodeDiagnostic(lineNo, "Llave de cierre '}' sin apertura correspondiente"));
                    braces = 0;
                }
                if (parens < 0) {
                    out.add(new CodeDiagnostic(lineNo, "Paréntesis de cierre ')' sin apertura correspondiente"));
                    parens = 0;
                }
                if (brackets < 0) {
                    out.add(new CodeDiagnostic(lineNo, "Corchete de cierre ']' sin apertura correspondiente"));
                    brackets = 0;
                }
            }
            if (inString) {
                out.add(new CodeDiagnostic(lineNo, "Cadena o carácter sin cerrar (comillas)"));
            }
        }

        int lastLine = lines.length;
        if (braces > 0) {
            out.add(new CodeDiagnostic(lastLine, "Falta una llave de cierre '}'"));
        }
        if (parens > 0) {
            out.add(new CodeDiagnostic(lastLine, "Falta un paréntesis de cierre ')'"));
        }
        if (brackets > 0) {
            out.add(new CodeDiagnostic(lastLine, "Falta un corchete de cierre ']'"));
        }
        return out;
    }
}
