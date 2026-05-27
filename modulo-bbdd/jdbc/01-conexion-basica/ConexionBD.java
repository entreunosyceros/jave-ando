import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utilidad de conexión JDBC — Ejercicio JDBC 01
 * Lee la configuración desde config/database.properties
 */
public class ConexionBD {

    private static Properties props = new Properties();

    static {
        try (InputStream in = ConexionBD.class.getResourceAsStream("/config/database.properties")) {
            if (in == null) {
                throw new RuntimeException(
                    "No se encontró config/database.properties. " +
                    "Copia database.properties.example y configura tus credenciales.");
            }
            props.load(in);
            Class.forName(props.getProperty("jdbc.driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error al cargar configuración JDBC", e);
        }
    }

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(
            props.getProperty("jdbc.url"),
            props.getProperty("jdbc.user"),
            props.getProperty("jdbc.password")
        );
    }

    public static void cerrarConexion(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}
