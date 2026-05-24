/**
 * Solución — Ejercicio 02: Encapsulación
 */
public class Main {

    public static void main(String[] args) {
        CuentaBancaria cuenta = new CuentaBancaria("ES001", "Ana la Jroña", 500);
        cuenta.mostrarResumen();

        cuenta.ingresar(200);
        cuenta.retirar(150);
        cuenta.retirar(1000);
        cuenta.ingresar(-50);

        cuenta.mostrarResumen();
    }
}
