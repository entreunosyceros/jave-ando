package com.ifcd0112.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JavacOutputParser {

    private static final Pattern LINE_MESSAGE = Pattern.compile(
            "^(?:\\.\\/)?([^:]+\\.java):(\\d+)(?::(\\d+))?\\s*:\\s*(error|warning)\\s*:\\s*(.+)$",
            Pattern.CASE_INSENSITIVE
    );

    private JavacOutputParser() {}

    static List<ParsedMessage> parse(String compilerOutput) {
        List<ParsedMessage> messages = new ArrayList<>();
        if (compilerOutput == null || compilerOutput.isBlank()) {
            return messages;
        }
        for (String raw : compilerOutput.split("\n")) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("Note:") || line.startsWith("warning: [options]")) {
                continue;
            }
            Matcher m = LINE_MESSAGE.matcher(line);
            if (m.matches()) {
                messages.add(new ParsedMessage(
                        m.group(1),
                        Integer.parseInt(m.group(2)),
                        m.group(4).equalsIgnoreCase("warning")
                                ? CodeDiagnostic.Severity.WARNING
                                : CodeDiagnostic.Severity.ERROR,
                        m.group(5).trim()
                ));
                continue;
            }
            if (line.contains("error:") || line.contains("error ")) {
                messages.add(new ParsedMessage(null, 1, CodeDiagnostic.Severity.ERROR, line));
            }
        }
        return messages;
    }

    record ParsedMessage(
            String fileName,
            int fileLine,
            CodeDiagnostic.Severity severity,
            String message
    ) {}
}
