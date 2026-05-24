public class Main {

    public static void main(String[] args) {
        Empleado[] plantilla = {
            new EmpleadoFijo("María Cigarros", "11111111A", 1800, 300),
            new EmpleadoTemporal("Carlos Illo", "22222222B", 6, 950),
            new EmpleadoFijo("Elena Nito", "33333333C", 2100, 450)
        };

        System.out.println("=== Nómina Miserable ===");
        for (Empleado e : plantilla) {
            e.mostrarDatos();
        }
    }
}
