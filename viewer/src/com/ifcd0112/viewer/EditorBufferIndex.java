package com.ifcd0112.viewer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Traduce líneas de un archivo (p. ej. {@code Main.java:10}) a la línea global del editor
 * cuando varios ficheros se muestran con {@code // ===== archivo.java =====}.
 */
public final class EditorBufferIndex {

    private static final Pattern FILE_MARKER =
            Pattern.compile("// ===== ([^\\n]+) =====\\s*");

    private final Map<String, Integer> fileToStartLine = new LinkedHashMap<>();

    private EditorBufferIndex() {}

    public static EditorBufferIndex build(String editorContent, String defaultFileName) {
        EditorBufferIndex index = new EditorBufferIndex();
        String defaultFile = defaultFileName != null && !defaultFileName.isBlank()
                ? defaultFileName
                : "Main.java";
        index.fileToStartLine.put(defaultFile, 1);

        if (editorContent == null || editorContent.isEmpty()) {
            return index;
        }

        Matcher matcher = FILE_MARKER.matcher(editorContent);
        int globalLine = 1;
        int scanFrom = 0;

        while (matcher.find(scanFrom)) {
            int markerLine = countLines(editorContent, 0, matcher.start()) + 1;
            String file = matcher.group(1).trim();
            index.fileToStartLine.put(file, markerLine + 1);
            scanFrom = matcher.end();
        }
        return index;
    }

    private static int countLines(String text, int from, int to) {
        int count = 0;
        for (int i = from; i < to && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    public int toEditorLine(String reportedFile, int fileLine) {
        if (fileLine < 1) {
            return 1;
        }
        String name = reportedFile == null ? "" : reportedFile.replace('\\', '/');
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf('/') + 1);
        }

        Integer start = fileToStartLine.get(name);
        if (start == null) {
            for (Map.Entry<String, Integer> e : fileToStartLine.entrySet()) {
                if (name.equals(e.getKey()) || name.endsWith("/" + e.getKey())) {
                    start = e.getValue();
                    break;
                }
            }
        }
        if (start == null && !fileToStartLine.isEmpty()) {
            start = fileToStartLine.values().iterator().next();
        }
        if (start == null) {
            return fileLine;
        }
        return start + fileLine - 1;
    }
}
