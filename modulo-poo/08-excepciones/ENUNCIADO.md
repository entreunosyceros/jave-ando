# Ejercicio 08 — Excepciones personalizadas

**Área:** Programación orientada a objetos
**Nivel:** Básico  
**Conceptos:** try-catch, throws, excepciones checked/unchecked

## Enunciado

Amplía el concepto de cuenta bancaria con manejo robusto de errores.

### Excepciones personalizadas

```java
public class SaldoInsuficienteException extends Exception {
    public SaldoInsuficienteException(String mensaje) { super(mensaje); }
}

public class CantidadInvalidaException extends Exception {
    public CantidadInvalidaException(String mensaje) { super(mensaje); }
}
```

### Clase `CuentaBancaria`

Reescribe los métodos de movimiento:

```java
public void ingresar(double cantidad) throws CantidadInvalidaException
public void retirar(double cantidad) throws CantidadInvalidaException, SaldoInsuficienteException
public void transferir(CuentaBancaria destino, double cantidad)
    throws CantidadInvalidaException, SaldoInsuficienteException
```

`transferir` debe retirar de la cuenta origen e ingresar en la destino. Si falla el ingreso, revierte el retiro (usa try-catch interno o transacción lógica).

### Clase `GestorCuentas`

- `ArrayList<CuentaBancaria> cuentas`
- `buscarCuenta(String numero)`: lanza `CuentaNoEncontradaException` si no existe
- `transferirEntreCuentas(String origen, String destino, double cantidad)`: captura excepciones y muestra mensajes amigables sin detener el programa

### Main

Prueba al menos estos casos:

1. Retiro con saldo insuficiente (capturado).
2. Ingreso con cantidad negativa (capturado).
3. Transferencia exitosa.
4. Transferencia a cuenta inexistente.

## Criterios de corrección

- [ ] Excepciones personalizadas checked
- [ ] Declaración `throws` correcta
- [ ] Captura en `Main` o `GestorCuentas` sin `System.exit`
- [ ] Transferencia con rollback lógico en caso de error
