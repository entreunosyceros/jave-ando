/**
 * Demo de conexión JDBC — funciona sin MySQL (modo simulación).
 * Con MySQL configurado, intenta conexión real.
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ListarAlumnosDemo {

    public static void main(String[] args) {
        Path propsPath = findDatabaseProperties();
        if (propsPath != null) {
            listarDesdeBaseDatos(propsPath);
        } else {
            listarSimulado();
        }
    }

    private static Path findDatabaseProperties() {
        Path dir = Path.of("").toAbsolutePath();
        for (int i = 0; i < 6 && dir != null; i++) {
            Path candidate = dir.resolve("modulo-bbdd/jdbc/config/database.properties");
            if (Files.exists(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        return null;
    }

    private static void listarDesdeBaseDatos(Path propsPath) {
        Properties props = new Properties();
        try {
            props.load(Files.newInputStream(propsPath));
            Class.forName(props.getProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver"));
            String sql = "SELECT dni, nombre, apellidos, email FROM alumno ORDER BY apellidos, nombre";
            try (Connection conn = DriverManager.getConnection(
                    props.getProperty("jdbc.url"),
                    props.getProperty("jdbc.user"),
                    props.getProperty("jdbc.password"));
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                System.out.println("=== Alumnos (JDBC real) ===");
                while (rs.next()) {
                    System.out.printf("%s | %s %s | %s%n",
                            rs.getString("dni"),
                            rs.getString("nombre"),
                            rs.getString("apellidos"),
                            rs.getString("email"));
                }
            }
        } catch (Exception e) {
            System.out.println("No se pudo conectar a MySQL: " + e.getMessage());
            System.out.println("Mostrando datos simulados...\n");
            listarSimulado();
        }
    }

    private static void listarSimulado() {
        System.out.println("=== Alumnos de la academia (simulación) ===");
        System.out.println("12345678A | Ana García López | ana.garcia@email.com");
        System.out.println("44332211D | Pablo Sánchez Torres | pablo.sanchez@email.com");
        System.out.println("87654321B | Luis Martín Ruiz | luis.martin@email.com");
        System.out.println("11223344C | Sofía Pérez Díaz | sofia.perez@email.com");
        System.out.println("\nPara JDBC real: importa sql/01_crear_base_datos.sql y configura database.properties");
    }
}
