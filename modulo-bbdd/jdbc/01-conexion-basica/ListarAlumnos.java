import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ejemplo de listado con JDBC — completa según ENUNCIADO.md
 */
public class ListarAlumnos {

    public static void main(String[] args) {
        String sql = "SELECT dni, nombre, apellidos, email FROM alumno ORDER BY apellidos, nombre";

        // TODO: usar try-with-resources con Connection, Statement y ResultSet
        // TODO: imprimir cada fila con formato legible
        // TODO: manejar SQLException con mensaje claro
    }
}
