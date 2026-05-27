import java.util.ArrayList;

public class GestorCuentas {

    private final ArrayList<CuentaBancaria> cuentas = new ArrayList<>();

    public void agregar(CuentaBancaria cuenta) {
        cuentas.add(cuenta);
    }

    public CuentaBancaria buscarCuenta(String numero) throws CuentaNoEncontradaException {
        return cuentas.stream()
                .filter(c -> c.getNumeroCuenta().equals(numero))
                .findFirst()
                .orElseThrow(() -> new CuentaNoEncontradaException("Cuenta no encontrada: " + numero));
    }

    public void transferirEntreCuentas(String origen, String destino, double cantidad) {
        try {
            CuentaBancaria cOrigen = buscarCuenta(origen);
            CuentaBancaria cDestino = buscarCuenta(destino);
            cOrigen.transferir(cDestino, cantidad);
            System.out.println("Transferencia OK: " + cantidad + " € de " + origen + " a " + destino);
        } catch (CuentaNoEncontradaException | CantidadInvalidaException | SaldoInsuficienteException e) {
            System.out.println("Error en transferencia: " + e.getMessage());
        }
    }
}
