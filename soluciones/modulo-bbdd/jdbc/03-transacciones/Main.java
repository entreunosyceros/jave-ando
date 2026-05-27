/**
 * Demo transacciones JDBC — simulación en consola.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Demo MatriculaService ===");
        System.out.println("1. matricular('12345678A', 'ING-B2', hoy)");
        System.out.println("   → setAutoCommit(false); INSERT; commit(); → true");
        System.out.println("2. matricular duplicada → validación previa → false (sin INSERT)");
        System.out.println("3. curso lleno → COUNT >= plazas → rollback()");
        System.out.println("4. cancelarMatricula → DELETE WHERE nota IS NULL");
        System.out.println("\nImplementación completa requiere MySQL + academia_idiomas.");
    }
}
