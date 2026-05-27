package com.ifcd0112.viewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Analiza el código del editor: compilación Java con {@code javac} y avisos de sintaxis básicos.
 */
public final class CodeDiagnosticsService {

    private static final int TIMEOUT_SECONDS = 20;

    private CodeDiagnosticsService() {}

    public record AnalysisResult(List<CodeDiagnostic> diagnostics, String log) {}

    public static AnalysisResult analyze(
            String editorContent,
            String languageHint,
            Path projectRoot,
            String defaultJavaFile
    ) {
        String lang = languageHint != null ? languageHint.toLowerCase() : "";
        boolean javaLike = lang.contains("java");
        boolean sqlLike = lang.contains("sql");

        List<CodeDiagnostic> quick = SyntaxHints.scan(editorContent, javaLike || sqlLike);
        if (!javaLike) {
            return new AnalysisResult(dedupe(quick), "");
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("jave-diagnostics-");
            SolutionWriter.saveToDirectory(tempDir, editorContent);
            EditorBufferIndex index = EditorBufferIndex.build(editorContent, defaultJavaFile);
            String compileLog = runJavac(tempDir, projectRoot);
            List<CodeDiagnostic> fromCompiler = mapCompilerMessages(compileLog, index);
            List<CodeDiagnostic> merged = new ArrayList<>(fromCompiler);
            merged.addAll(quick);
            return new AnalysisResult(dedupe(merged), compileLog);
        } catch (IOException e) {
            List<CodeDiagnostic> err = new ArrayList<>(quick);
            err.add(new CodeDiagnostic(1, "No se pudo analizar: " + e.getMessage()));
            return new AnalysisResult(dedupe(err), e.getMessage());
        } finally {
            if (tempDir != null) {
                deleteRecursive(tempDir);
            }
        }
    }

    private static List<CodeDiagnostic> mapCompilerMessages(String log, EditorBufferIndex index) {
        List<CodeDiagnostic> out = new ArrayList<>();
        for (JavacOutputParser.ParsedMessage pm : JavacOutputParser.parse(log)) {
            int editorLine = pm.fileName() != null
                    ? index.toEditorLine(pm.fileName(), pm.fileLine())
                    : pm.fileLine();
            out.add(new CodeDiagnostic(
                    editorLine,
                    pm.message(),
                    pm.severity(),
                    pm.fileName()
            ));
        }
        return out;
    }

    private static String runJavac(Path sourceDir, Path projectRoot) throws IOException {
        List<Path> javaFiles;
        try (Stream<Path> walk = Files.walk(sourceDir)) {
            javaFiles = walk.filter(p -> p.toString().endsWith(".java")).toList();
        }
        if (javaFiles.isEmpty()) {
            return "No hay archivos .java para compilar.";
        }

        Path libDir = projectRoot != null ? projectRoot.resolve("lib") : null;
        String classpath = buildClasspath(sourceDir, libDir);

        List<String> cmd = new ArrayList<>();
        cmd.add(PlatformSupport.resolveJdkTool("javac"));
        cmd.add("-encoding");
        cmd.add("UTF-8");
        if (!classpath.isEmpty()) {
            cmd.add("-cp");
            cmd.add(classpath);
        }
        javaFiles.forEach(p -> cmd.add(p.toString()));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(sourceDir.toFile());
        pb.redirectErrorStream(true);

        StringBuilder output = new StringBuilder();
        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        }
        try {
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                output.append("\n[Análisis cancelado: tiempo agotado]");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            output.append("\n[Análisis interrumpido]");
        }
        return output.toString();
    }

    private static String buildClasspath(Path sourceDir, Path libDir) {
        List<String> parts = new ArrayList<>();
        parts.add(sourceDir.toString());
        if (libDir != null && Files.isDirectory(libDir)) {
            try (Stream<Path> jars = Files.list(libDir)) {
                jars.filter(p -> p.toString().endsWith(".jar"))
                        .map(Path::toString)
                        .forEach(parts::add);
            } catch (IOException ignored) {
                // sin JARs
            }
        }
        return String.join(System.getProperty("path.separator"), parts);
    }

    private static List<CodeDiagnostic> dedupe(List<CodeDiagnostic> items) {
        Map<String, CodeDiagnostic> byKey = new LinkedHashMap<>();
        for (CodeDiagnostic d : items) {
            String key = d.line() + "|" + d.message();
            byKey.putIfAbsent(key, d);
        }
        return byKey.values().stream()
                .sorted(Comparator.comparingInt(CodeDiagnostic::line))
                .toList();
    }

    private static void deleteRecursive(Path root) {
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }
}
