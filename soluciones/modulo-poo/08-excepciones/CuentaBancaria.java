public class CuentaBancaria {

    private final String numeroCuenta;
    private String titular;
    private double saldo;

    public CuentaBancaria(String numeroCuenta, String titular, double saldo) {
        this.numeroCuenta = numeroCuenta;
        this.titular = titular;
        this.saldo = saldo;
    }

    public String getNumeroCuenta() { return numeroCuenta; }
    public double getSaldo() { return saldo; }

    public void ingresar(double cantidad) throws CantidadInvalidaException {
        if (cantidad <= 0) {
            throw new CantidadInvalidaException("Cantidad inválida: " + cantidad);
        }
        saldo += cantidad;
    }

    public void retirar(double cantidad) throws CantidadInvalidaException, SaldoInsuficienteException {
        if (cantidad <= 0) {
            throw new CantidadInvalidaException("Cantidad inválida: " + cantidad);
        }
        if (cantidad > saldo) {
            throw new SaldoInsuficienteException("Saldo insuficiente en " + numeroCuenta);
        }
        saldo -= cantidad;
    }

    public void transferir(CuentaBancaria destino, double cantidad)
            throws CantidadInvalidaException, SaldoInsuficienteException {
        retirar(cantidad);
        try {
            destino.ingresar(cantidad);
        } catch (CantidadInvalidaException e) {
            saldo += cantidad;
            throw e;
        }
    }

    @Override
    public String toString() {
        return numeroCuenta + " (" + titular + ") — " + String.format("%.2f €", saldo);
    }
}
