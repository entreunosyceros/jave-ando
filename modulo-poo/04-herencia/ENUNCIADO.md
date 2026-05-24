# Ejercicio 04 — Herencia

**Área:** Programación orientada a objetos
**Nivel:** Básico  
**Conceptos:** extends, super, relación "es-un"

## Enunciado

Una empresa necesita gestionar distintos tipos de empleados.

### Clase base `Empleado`

| Atributo | Tipo |
|----------|------|
| `nombre` | `String` |
| `dni` | `String` |
| `salarioBase` | `double` |

Métodos:

- Constructor con los tres parámetros.
- Getters y setters.
- `calcularSalario()`: devuelve `salarioBase` (será redefinido en subclases).
- `mostrarDatos()`: imprime nombre, DNI y salario calculado.

### Subclase `EmpleadoFijo`

Atributo adicional: `plusAntiguedad` (`double`).

- `calcularSalario()`: `salarioBase + plusAntiguedad`.

### Subclase `EmpleadoTemporal`

Atributos adicionales: `mesesContrato` (`int`), `importeMes` (`double`).

- `calcularSalario()`: `mesesContrato * importeMes`.

### Main

Crea un array de tipo `Empleado[]` con al menos un empleado de cada tipo. Recorre el array y muestra los datos de cada uno usando `mostrarDatos()`.

## Preguntas de reflexión (responder en comentarios)

1. ¿Por qué el array es de tipo `Empleado` y no de `EmpleadoFijo`?
2. ¿Qué método se ejecuta en `calcularSalario()` cuando el elemento es `EmpleadoTemporal`?

## Criterios de corrección

- [ ] Herencia correcta con `extends`
- [ ] Uso de `super()` en constructores de subclases
- [ ] Redefinición de `calcularSalario()` en cada subclase
- [ ] Array polimórfico `Empleado[]`
