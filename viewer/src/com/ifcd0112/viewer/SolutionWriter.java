package com.ifcd0112.viewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class SolutionWriter {

  /** Separador usado al leer varios .java de una carpeta (ver {@link SolutionReader}). */
  private static final Pattern FILE_MARKER =
      Pattern.compile("// ===== ([^\\n]+) =====\\s*\\n");

  private SolutionWriter() {}

  public static void save(Exercise exercise, String content) throws IOException {
    Path path = exercise.solutionPath();
    if (path == null || path.toString().isEmpty()) {
      throw new IOException("Este ejercicio no tiene ruta de solución configurada.");
    }
    if (!Files.exists(path)) {
      throw new IOException("No existe la ruta de solución: " + path);
    }
    if (Files.isDirectory(path)) {
      saveToDirectory(path, content);
    } else {
      Files.writeString(path, ensureTrailingNewline(content), StandardCharsets.UTF_8);
    }
  }

  public static void saveToDirectory(Path dir, String content) throws IOException {
    writeJavaDirectory(dir, content);
  }

  public static String ensureTrailingNewline(String text) {
    if (text.isEmpty()) {
      return "\n";
    }
    return text.endsWith("\n") ? text : text + "\n";
  }

  private static void writeJavaDirectory(Path dir, String content) throws IOException {
    Matcher matcher = FILE_MARKER.matcher(content);
    if (!matcher.find()) {
      Path target = resolvePrimaryJavaFile(dir);
      Files.writeString(target, ensureTrailingNewline(content), StandardCharsets.UTF_8);
      return;
    }

    matcher.reset();
    String currentFile = null;
    int bodyStart = 0;

    while (matcher.find()) {
      if (currentFile != null) {
        String body = content.substring(bodyStart, matcher.start()).trim();
        writeJavaFile(dir, currentFile, body);
      }
      currentFile = matcher.group(1).trim();
      bodyStart = matcher.end();
    }

    if (currentFile != null) {
      String body = content.substring(bodyStart).trim();
      writeJavaFile(dir, currentFile, body);
    }
  }

  private static void writeJavaFile(Path dir, String relativePath, String body) throws IOException {
    Path file = dir.resolve(relativePath).normalize();
    if (!PlatformSupport.isSubPath(file, dir)) {
      throw new IOException("Ruta de archivo no válida: " + relativePath);
    }
    Files.createDirectories(file.getParent() != null ? file.getParent() : dir);
    Files.writeString(file, ensureTrailingNewline(body), StandardCharsets.UTF_8);
  }

  private static Path resolvePrimaryJavaFile(Path dir) throws IOException {
    Path main = dir.resolve("Main.java");
    if (Files.isRegularFile(main)) {
      return main;
    }
    try (Stream<Path> files = Files.list(dir)) {
      return files
          .filter(p -> p.toString().endsWith(".java"))
          .min(Comparator.comparing(p -> p.getFileName().toString()))
          .orElse(main);
    }
  }

}
