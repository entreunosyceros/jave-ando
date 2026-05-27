/**
 * Demo CRUD JDBC — simulación en consola (sin base de datos).
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Demo AlumnoDAO ===");
        System.out.println("findAll() → 4 alumnos");
        System.out.println("findByDni('12345678A') → Ana García López");
        System.out.println("insert(Alumno) → PreparedStatement: INSERT INTO alumno ...");
        System.out.println("update(Alumno) → PreparedStatement: UPDATE alumno SET email=?, telefono=? ...");
        System.out.println("delete('99999999Z') → false (tiene matrículas)");
        System.out.println("\nImplementación completa requiere MySQL + academia_idiomas.");
    }
}
