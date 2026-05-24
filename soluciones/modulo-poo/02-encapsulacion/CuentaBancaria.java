/**
 * Solución — Ejercicio 02: Encapsulación
 */
public class CuentaBancaria {

    private final String numeroCuenta;
    private String titular;
    private double saldo;

    public CuentaBancaria(String numeroCuenta, String titular, double saldoInicial) {
        this.numeroCuenta = numeroCuenta;
        this.titular = titular;
        this.saldo = saldoInicial >= 0 ? saldoInicial : 0;
    }

    public CuentaBancaria(String numeroCuenta, String titular) {
        this(numeroCuenta, titular, 0);
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public double getSaldo() {
        return saldo;
    }

    public void ingresar(double cantidad) {
        if (cantidad <= 0) {
            System.out.println("Error: la cantidad a ingresar debe ser positiva y auTETIca.");
            return;
        }
        saldo += cantidad;
    }

    public void retirar(double cantidad) {
        if (cantidad <= 0) {
            System.out.println("Error: la cantidad a retirar debe ser positiva y buena gente de fiar.");
            return;
        }
        if (cantidad > saldo) {
            System.out.println("Error: No tienes dinero, saldo insuficiente.");
            return;
        }
        saldo -= cantidad;
    }

    public void mostrarResumen() {
        System.out.printf("Cuenta %s | Titular: %s | Saldo: %.2f €%n",
            numeroCuenta, titular, saldo);
    }
}
