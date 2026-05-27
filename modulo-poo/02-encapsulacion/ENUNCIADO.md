# Ejercicio 02 — Encapsulación

**Área:** Programación orientada a objetos
**Nivel:** Básico  
**Conceptos:** private, getters, setters, validación

## Enunciado

Modela una **cuenta bancaria** aplicando encapsulación correcta.

### Clase `CuentaBancaria`

| Atributo | Tipo | Visibilidad | Descripción |
|----------|------|-------------|-------------|
| `numeroCuenta` | `String` | private | Identificador único (solo lectura tras creación) |
| `titular` | `String` | private | Nombre del titular |
| `saldo` | `double` | private | Saldo actual (no puede ser negativo) |

**Constructores:**

- Uno que reciba `numeroCuenta`, `titular` y saldo inicial (≥ 0).
- Otro que reciba solo `numeroCuenta` y `titular`, con saldo inicial 0.

**Métodos:**

| Método | Descripción |
|--------|-------------|
| `getNumeroCuenta()` | Devuelve el número de cuenta |
| `getTitular()` / `setTitular(String)` | Acceso al titular |
| `getSaldo()` | Devuelve el saldo (solo lectura) |
| `ingresar(double cantidad)` | Suma al saldo si `cantidad > 0`; si no, muestra error |
| `retirar(double cantidad)` | Resta del saldo si hay fondos suficientes; si no, muestra error |
| `mostrarResumen()` | Imprime número, titular y saldo formateado |

### Clase `Main`

Prueba ingresos, retiros válidos, retiros que dejen saldo insuficiente e ingresos con cantidad negativa.

## Restricciones

- Ningún atributo debe ser `public`.
- No permitas modificar el saldo directamente desde fuera de la clase.

## Criterios de corrección

- [ ] Atributos privados con getters/setters apropiados
- [ ] El saldo no puede quedar negativo
- [ ] `numeroCuenta` no tiene setter
- [ ] Validación en `ingresar()` y `retirar()`
