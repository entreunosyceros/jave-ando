public class Main {

    public static void main(String[] args) {
        GestorCuentas gestor = new GestorCuentas();
        gestor.agregar(new CuentaBancaria("ES001", "Edelmira", 500));
        gestor.agregar(new CuentaBancaria("ES002", "Segismundo", 200));

        System.out.println("=== Prueba retiro sin saldo ===");
        try {
            gestor.buscarCuenta("ES002").retirar(1000);
        } catch (CuentaNoEncontradaException | CantidadInvalidaException | SaldoInsuficienteException e) {
            System.out.println("Capturado: " + e.getMessage());
        }

        System.out.println("\n=== Prueba ingreso negativo ===");
        try {
            gestor.buscarCuenta("ES001").ingresar(-10);
        } catch (CuentaNoEncontradaException | CantidadInvalidaException e) {
            System.out.println("Capturado: " + e.getMessage());
        }

        System.out.println("\n=== Transferencia exitosa ===");
        gestor.transferirEntreCuentas("ES001", "ES002", 100);

        System.out.println("\n=== Transferencia a cuenta inexistente ===");
        gestor.transferirEntreCuentas("ES001", "ES999", 50);

        System.out.println("\n=== Estado final ===");
        try {
            System.out.println(gestor.buscarCuenta("ES001"));
            System.out.println(gestor.buscarCuenta("ES002"));
        } catch (CuentaNoEncontradaException e) {
            System.out.println(e.getMessage());
        }
    }
}
