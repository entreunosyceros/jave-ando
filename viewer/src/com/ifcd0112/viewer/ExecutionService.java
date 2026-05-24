package com.ifcd0112.viewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class ExecutionService {

    private static final int TIMEOUT_SECONDS = 15;

    public record ExecutionResult(boolean success, String output) {}

    public ExecutionResult compileAndRun(Path solutionDir, String mainClass, Path projectRoot) {
        if (!Files.isDirectory(solutionDir)) {
            return new ExecutionResult(false, "No se encontró el directorio de solución: " + solutionDir);
        }

        List<Path> javaFiles;
        try (Stream<Path> walk = Files.walk(solutionDir)) {
            javaFiles = walk
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList();
        } catch (IOException e) {
            return new ExecutionResult(false, "Error al leer archivos: " + e.getMessage());
        }

        if (javaFiles.isEmpty()) {
            return new ExecutionResult(false, "No hay archivos .java en la solución.");
        }

        Path libDir = projectRoot.resolve("lib");
        String classpath = buildClasspath(solutionDir, libDir);

        List<String> compileCmd = new ArrayList<>();
        compileCmd.add(PlatformSupport.resolveJdkTool("javac"));
        compileCmd.add("-encoding");
        compileCmd.add("UTF-8");
        if (!classpath.isEmpty()) {
            compileCmd.add("-cp");
            compileCmd.add(classpath);
        }
        javaFiles.forEach(p -> compileCmd.add(p.toString()));

        ProcessResult compile = runProcess(compileCmd, solutionDir);
        if (!compile.success()) {
            return new ExecutionResult(false, "Error de compilación:\n" + compile.output());
        }

        List<String> runCmd = new ArrayList<>();
        runCmd.add(PlatformSupport.resolveJdkTool("java"));
        if (!classpath.isEmpty()) {
            runCmd.add("-cp");
            runCmd.add(classpath);
        }
        runCmd.add(mainClass);

        ProcessResult run = runProcess(runCmd, solutionDir);
        String output = run.output();
        if (!run.success()) {
            output = "La ejecución terminó con errores:\n" + output;
        }
        return new ExecutionResult(run.success(), output.isBlank() ? "(Sin salida en consola)" : output);
    }

    private static String buildClasspath(Path solutionDir, Path libDir) {
        List<String> parts = new ArrayList<>();
        parts.add(solutionDir.toString());

        if (Files.isDirectory(libDir)) {
            try (Stream<Path> jars = Files.list(libDir)) {
                jars.filter(p -> p.toString().endsWith(".jar"))
                        .map(Path::toString)
                        .forEach(parts::add);
            } catch (IOException ignored) {
                // sin JARs opcionales
            }
        }
        return String.join(System.getProperty("path.separator"), parts);
    }

    private record ProcessResult(boolean success, String output) {}

    private static ProcessResult runProcess(List<String> command, Path workDir) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);

        StringBuilder output = new StringBuilder();
        try {
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                output.append("\n[Ejecución cancelada: superó ").append(TIMEOUT_SECONDS).append(" segundos]");
                return new ProcessResult(false, output.toString());
            }
            return new ProcessResult(process.exitValue() == 0, output.toString());
        } catch (IOException e) {
            if (PlatformSupport.looksLikeJdkMissing(e)) {
                return new ProcessResult(false, PlatformSupport.jdkNotFoundHint());
            }
            return new ProcessResult(false, "Error al ejecutar: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ProcessResult(false, "Error al ejecutar: " + e.getMessage());
        }
    }
}
