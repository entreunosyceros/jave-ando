package com.ifcd0112.viewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/** Carga y prueba {@code modulo-bbdd/jdbc/config/database.properties}. */
public final class DatabaseConfig {

    public static final String RELATIVE_PATH = "modulo-bbdd/jdbc/config/database.properties";
    public static final String EXAMPLE_RELATIVE_PATH = "modulo-bbdd/jdbc/config/database.properties.example";

    /** Mantiene vivo el classloader que carga los JAR de lib/ (no cerrar tras registrar el driver). */
    private static volatile java.net.URLClassLoader jdbcClassLoader;

    private static final Set<String> SYSTEM_DATABASES = Set.of(
            "information_schema", "performance_schema", "mysql", "sys"
    );

    private DatabaseConfig() {}

    public static Path configFile(Path projectRoot) {
        return projectRoot.resolve(RELATIVE_PATH);
    }

    public static Path exampleFile(Path projectRoot) {
        return projectRoot.resolve(EXAMPLE_RELATIVE_PATH);
    }

    public static boolean configExists(Path projectRoot) {
        return Files.isRegularFile(configFile(projectRoot));
    }

    public static Properties load(Path projectRoot) throws IOException {
        Path file = configFile(projectRoot);
        if (!Files.isRegularFile(file)) {
            throw new IOException(
                    "No existe database.properties.\n"
                            + "Usa Herramientas → Configurar conexión SQL…\n"
                            + "o copia:\n  " + exampleFile(projectRoot)
            );
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        }
        validate(props);
        return props;
    }

    public static Properties loadOrExample(Path projectRoot) throws IOException {
        if (configExists(projectRoot)) {
            return load(projectRoot);
        }
        Path example = exampleFile(projectRoot);
        if (!Files.isRegularFile(example)) {
            throw new IOException("No se encontró ni database.properties ni el archivo de ejemplo.");
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(example)) {
            props.load(in);
        }
        return props;
    }

    public static void save(Path projectRoot, Properties props) throws IOException {
        validate(props);
        Path file = configFile(projectRoot);
        Files.createDirectories(file.getParent());
        try (OutputStream out = Files.newOutputStream(file)) {
            props.store(out, "JAVe-Ando — conexión SQL/JDBC");
        }
    }

    public static String testConnection(Path projectRoot) {
        try {
            Properties props = load(projectRoot);
            String downloaded = registerDriver(projectRoot, props);
            String jdbcUrl = props.getProperty("jdbc.url", "");
            String user = props.getProperty("jdbc.user");
            String password = props.getProperty("jdbc.password");
            String serverUrl = serverJdbcUrl(jdbcUrl);

            try (Connection conn = DriverManager.getConnection(serverUrl, user, password)) {
                if (!conn.isValid(3)) {
                    return prefixDownloaded(downloaded,
                            "Conexión al servidor establecida pero isValid() devolvió false.");
                }
                StringBuilder msg = new StringBuilder();
                msg.append("Conexión al servidor MySQL/MariaDB correcta");
                if (!serverUrl.equals(jdbcUrl)) {
                    msg.append(" (sin exigir que exista la base aún)");
                }
                msg.append(".");

                String db = extractDatabaseName(jdbcUrl);
                if (!db.isEmpty()) {
                    if (databaseExists(conn, db)) {
                        msg.append("\nBase de datos «").append(db).append("» encontrada.");
                    } else {
                        msg.append("\nLa base «").append(db).append("» aún no existe (es normal la primera vez).");
                        msg.append("\nCréala con el ejercicio 01 → Ejecutar solución");
                        msg.append(" o Herramientas → Inicializar base de datos (script 01)…");
                    }
                }
                return prefixDownloaded(downloaded, msg.toString());
            }
        } catch (ClassNotFoundException e) {
            return e.getMessage()
                    + "\n\nNo se pudo cargar el driver. Comprueba la conexión a Internet o descarga manualmente (lib/README.md).";
        } catch (IOException e) {
            return "No se pudo obtener el conector JDBC:\n" + e.getMessage();
        } catch (SQLException e) {
            return "Error de conexión: " + e.getMessage()
                    + "\n\nComprueba que el servicio esté en marcha y que usuario/contraseña en database.properties sean correctos.";
        } catch (Exception e) {
            return "Error de conexión: " + e.getMessage()
                    + "\n\nComprueba que MySQL/MariaDB esté en marcha y las credenciales en database.properties.";
        }
    }

    private static String prefixDownloaded(String downloaded, String message) {
        return downloaded == null ? message : downloaded + "\n\n" + message;
    }

    /** URL JDBC al servidor sin nombre de base (para probar usuario/red sin crear la BD antes). */
    static String serverJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return jdbcUrl;
        }
        int proto = jdbcUrl.indexOf("://");
        if (proto < 0) {
            return jdbcUrl;
        }
        String afterProto = jdbcUrl.substring(proto + 3);
        int dbSlash = afterProto.indexOf('/');
        if (dbSlash < 0) {
            return jdbcUrl;
        }
        String hostPort = afterProto.substring(0, dbSlash);
        String prefix = jdbcUrl.substring(0, proto + 3);
        int q = jdbcUrl.indexOf('?', proto);
        String query = q >= 0 ? jdbcUrl.substring(q) : "";
        return prefix + hostPort + "/" + query;
    }

    private static boolean databaseExists(Connection conn, String dbName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?")) {
            ps.setString(1, dbName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static Connection openConnection(Properties props) throws Exception {
        validate(props);
        return DriverManager.getConnection(
                props.getProperty("jdbc.url"),
                props.getProperty("jdbc.user"),
                props.getProperty("jdbc.password")
        );
    }

    /**
     * Abre conexión adecuada para ejecutar un script: al servidor si el script crea la base
     * o si la base configurada en la URL aún no existe (p. ej. ejercicio 01).
     */
    public static Connection openConnectionForScript(Properties props, List<String> statements) throws Exception {
        validate(props);
        String jdbcUrl = props.getProperty("jdbc.url");
        String user = props.getProperty("jdbc.user");
        String password = props.getProperty("jdbc.password");
        String executionUrl = resolveExecutionJdbcUrl(jdbcUrl, user, password, statements);
        return DriverManager.getConnection(executionUrl, user, password);
    }

    public static boolean usesServerConnectionForScript(
            Properties props, List<String> statements) throws Exception {
        validate(props);
        String jdbcUrl = props.getProperty("jdbc.url");
        return resolveExecutionJdbcUrl(
                jdbcUrl,
                props.getProperty("jdbc.user"),
                props.getProperty("jdbc.password"),
                statements
        ).equals(serverJdbcUrl(jdbcUrl));
    }

    private static String resolveExecutionJdbcUrl(
            String jdbcUrl, String user, String password, List<String> statements) throws SQLException {
        if (scriptCreatesOrRecreatesDatabase(statements)) {
            return serverJdbcUrl(jdbcUrl);
        }
        String db = extractDatabaseName(jdbcUrl);
        if (db.isEmpty()) {
            return jdbcUrl;
        }
        try (Connection probe = DriverManager.getConnection(serverJdbcUrl(jdbcUrl), user, password)) {
            if (!databaseExists(probe, db)) {
                return serverJdbcUrl(jdbcUrl);
            }
        }
        return jdbcUrl;
    }

    static boolean scriptCreatesOrRecreatesDatabase(List<String> statements) {
        for (String statement : statements) {
            String norm = statement.stripLeading().toUpperCase(Locale.ROOT);
            if (norm.startsWith("CREATE DATABASE") || norm.startsWith("DROP DATABASE")) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return mensaje si se descargó el conector, o {@code null} si ya estaba disponible
     */
    public static String registerDriver(Path projectRoot, Properties props) throws Exception {
        String driverClass = props.getProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver");
        try {
            Class.forName(driverClass);
            return null;
        } catch (ClassNotFoundException ignored) {
            // intentar desde lib/
        }
        String downloaded = JdbcConnectorDownloader.ensureConnector(projectRoot, driverClass);
        List<Path> jars = listJdbcJars(projectRoot);
        if (jars.isEmpty()) {
            throw new ClassNotFoundException(
                    driverClass + " — no hay conector en lib/ y la descarga automática no tuvo éxito"
            );
        }
        if (jdbcClassLoader != null) {
            try {
                Class.forName(driverClass, true, jdbcClassLoader);
                return downloaded;
            } catch (ClassNotFoundException ignored) {
                jdbcClassLoader = null;
            }
        }
        List<URL> urls = new ArrayList<>();
        for (Path jar : jars) {
            urls.add(jar.toUri().toURL());
        }
        jdbcClassLoader = new java.net.URLClassLoader(
                urls.toArray(new URL[0]), DatabaseConfig.class.getClassLoader());
        Class<?> clazz = Class.forName(driverClass, true, jdbcClassLoader);
        Driver driver = (Driver) clazz.getDeclaredConstructor().newInstance();
        DriverManager.registerDriver(new DriverShim(driver));
        return downloaded;
    }

    private static List<Path> listJdbcJars(Path projectRoot) throws IOException {
        return JdbcConnectorDownloader.listJars(projectRoot);
    }

    private static void validate(Properties props) throws IOException {
        if (blank(props.getProperty("jdbc.url"))) {
            throw new IOException("Falta jdbc.url en database.properties");
        }
        if (blank(props.getProperty("jdbc.user"))) {
            throw new IOException("Falta jdbc.user en database.properties");
        }
        if (props.getProperty("jdbc.password") == null) {
            throw new IOException("Falta jdbc.password en database.properties (puede estar vacía)");
        }
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    public static String activeDatabaseName(Properties props) {
        return extractDatabaseName(props.getProperty("jdbc.url", ""));
    }

    /** Bases visibles para el usuario (sin esquemas de sistema). */
    public static List<String> listUserDatabases(Path projectRoot) throws Exception {
        Properties props = load(projectRoot);
        registerDriver(projectRoot, props);
        List<String> all = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(
                serverJdbcUrl(props.getProperty("jdbc.url")),
                props.getProperty("jdbc.user"),
                props.getProperty("jdbc.password"));
                var stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
            while (rs.next()) {
                String name = rs.getString(1);
                if (name != null && !SYSTEM_DATABASES.contains(name.toLowerCase(Locale.ROOT))) {
                    all.add(name);
                }
            }
        }
        all.sort(String.CASE_INSENSITIVE_ORDER);
        return all;
    }

    public static void setActiveDatabase(Path projectRoot, String databaseName) throws IOException {
        Properties props = load(projectRoot);
        String url = props.getProperty("jdbc.url");
        props.setProperty("jdbc.url", withDatabaseInUrl(url, databaseName));
        save(projectRoot, props);
    }

    /** Reescribe {@code jdbc.url} con el nombre de base bien parseado (arregla URLs dañadas por el bug Europe/Madrid). */
    public static void normalizeJdbcUrl(Path projectRoot) throws IOException {
        if (!configExists(projectRoot)) {
            return;
        }
        Properties props = load(projectRoot);
        String url = props.getProperty("jdbc.url");
        String db = extractDatabaseName(url);
        // Bug antiguo: confundía Europe/Madrid del timezone con nombre de base
        if ("Madrid".equalsIgnoreCase(db) && url.contains("Europe/Madrid")) {
            db = "academia_idiomas";
        }
        String normalized = db.isEmpty() ? serverJdbcUrl(url) : withDatabaseInUrl(url, db);
        if (!normalized.equals(url)) {
            props.setProperty("jdbc.url", normalized);
            save(projectRoot, props);
        }
    }

    /** Conexión a la base indicada en {@code jdbc.url} (debe existir). */
    public static Connection openConnectionToActiveDatabase(Path projectRoot) throws Exception {
        Properties props = load(projectRoot);
        registerDriver(projectRoot, props);
        String jdbcUrl = props.getProperty("jdbc.url");
        String db = extractDatabaseName(jdbcUrl);
        if (!db.isEmpty()) {
            try (Connection probe = DriverManager.getConnection(
                    serverJdbcUrl(jdbcUrl),
                    props.getProperty("jdbc.user"),
                    props.getProperty("jdbc.password"))) {
                if (!databaseExists(probe, db)) {
                    throw new IOException("La base «" + db + "» no existe. Créala con el ejercicio 01 o selecciona otra base.");
                }
            }
        }
        return openConnection(props);
    }

    static String withDatabaseInUrl(String jdbcUrl, String databaseName) {
        String server = serverJdbcUrl(jdbcUrl);
        int q = server.indexOf('?');
        String base = q >= 0 ? server.substring(0, q) : server;
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        String params = q >= 0 ? server.substring(q) : "";
        return base + databaseName + params;
    }

    /**
     * Nombre de base en la ruta JDBC (primer segmento tras host:puerto).
     * No usar {@code lastIndexOf('/')} — falla con {@code serverTimezone=Europe/Madrid}.
     */
    static String extractDatabaseName(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        int proto = url.indexOf("://");
        if (proto < 0) {
            return "";
        }
        String afterAuthority = url.substring(proto + 3);
        int pathSlash = afterAuthority.indexOf('/');
        if (pathSlash < 0 || pathSlash >= afterAuthority.length() - 1) {
            return "";
        }
        String path = afterAuthority.substring(pathSlash + 1);
        int q = path.indexOf('?');
        if (q >= 0) {
            path = path.substring(0, q);
        }
        int extra = path.indexOf('/');
        if (extra >= 0) {
            path = path.substring(0, extra);
        }
        return path.trim();
    }

    /** Permite registrar el driver cargado con URLClassLoader en DriverManager. */
    private static final class DriverShim implements Driver {
        private final Driver delegate;

        DriverShim(Driver delegate) {
            this.delegate = delegate;
        }

        @Override
        public Connection connect(String url, java.util.Properties info) throws java.sql.SQLException {
            return delegate.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url) throws java.sql.SQLException {
            return delegate.acceptsURL(url);
        }

        @Override
        public java.sql.DriverPropertyInfo[] getPropertyInfo(String url, java.util.Properties info)
                throws java.sql.SQLException {
            return delegate.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return delegate.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return delegate.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return delegate.jdbcCompliant();
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws java.sql.SQLFeatureNotSupportedException {
            return delegate.getParentLogger();
        }
    }
}
