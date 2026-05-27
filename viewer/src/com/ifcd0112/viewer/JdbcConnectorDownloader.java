package com.ifcd0112.viewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Si {@code lib/} no contiene un conector JDBC, lo descarga desde Maven Central
 * y lo guarda en esa carpeta.
 */
public final class JdbcConnectorDownloader {

    private static final Object LOCK = new Object();

    private static final String MYSQL_VERSION = "8.3.0";
    private static final String MYSQL_JAR = "mysql-connector-j-" + MYSQL_VERSION + ".jar";
    private static final String MYSQL_URL =
            "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/"
                    + MYSQL_VERSION + "/" + MYSQL_JAR;

    private static final String MARIADB_VERSION = "3.3.3";
    private static final String MARIADB_JAR = "mariadb-java-client-" + MARIADB_VERSION + ".jar";
    private static final String MARIADB_URL =
            "https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/"
                    + MARIADB_VERSION + "/" + MARIADB_JAR;

    private static final long MIN_JAR_BYTES = 400_000;

    private JdbcConnectorDownloader() {}

    /**
     * Garantiza que haya al menos un .jar en {@code lib/}; descarga el adecuado al driver si hace falta.
     *
     * @return mensaje informativo si hubo descarga, o {@code null} si ya existía
     */
    public static String ensureConnector(Path projectRoot, String driverClass) throws IOException {
        List<Path> existing = listJars(projectRoot);
        if (!existing.isEmpty()) {
            return null;
        }
        Artifact artifact = artifactForDriver(driverClass);
        synchronized (LOCK) {
            existing = listJars(projectRoot);
            if (!existing.isEmpty()) {
                return null;
            }
            Path dest = download(projectRoot, artifact);
            return "Conector JDBC descargado automáticamente:\n" + dest;
        }
    }

    private static Artifact artifactForDriver(String driverClass) {
        if (driverClass != null && driverClass.toLowerCase(Locale.ROOT).contains("mariadb")) {
            return new Artifact(MARIADB_JAR, MARIADB_URL);
        }
        return new Artifact(MYSQL_JAR, MYSQL_URL);
    }

    private static Path download(Path projectRoot, Artifact artifact) throws IOException {
        Path lib = projectRoot.resolve("lib");
        Files.createDirectories(lib);
        Path dest = lib.resolve(artifact.fileName());
        Path temp = Files.createTempFile(lib, "jdbc-download-", ".part");

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(artifact.url()))
                .timeout(Duration.ofMinutes(3))
                .GET()
                .build();

        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                throw new IOException(
                        "No se pudo descargar el conector (HTTP " + response.statusCode() + "):\n" + artifact.url()
                );
            }
            try (InputStream in = response.body(); OutputStream out = Files.newOutputStream(temp)) {
                in.transferTo(out);
            }
            long size = Files.size(temp);
            if (size < MIN_JAR_BYTES) {
                throw new IOException("Descarga incompleta o corrupta (" + size + " bytes). Comprueba tu conexión a Internet.");
            }
            Files.move(temp, dest, StandardCopyOption.REPLACE_EXISTING);
            return dest;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Descarga del conector JDBC interrumpida.", e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error al descargar el conector JDBC: " + e.getMessage(), e);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    static List<Path> listJars(Path projectRoot) throws IOException {
        Path lib = projectRoot.resolve("lib");
        if (!Files.isDirectory(lib)) {
            return List.of();
        }
        List<Path> jars = new ArrayList<>();
        try (var stream = Files.list(lib)) {
            stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".jar")).forEach(jars::add);
        }
        return jars;
    }

    private record Artifact(String fileName, String url) {}
}
