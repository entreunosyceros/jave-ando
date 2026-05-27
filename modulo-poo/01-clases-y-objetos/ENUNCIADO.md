# Ejercicio 01 — Clases y objetos

**Área:** Programación orientada a objetos
**Nivel:** Básico  
**Conceptos:** clase, objeto, atributos, métodos, instanciación

## Enunciado

Una librería quiere registrar información básica de sus libros. Debes modelar esta situación en Java.

### Parte A — Clase `Libro`

Crea la clase `Libro` con:

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| `titulo` | `String` | Título del libro |
| `autor` | `String` | Nombre del autor |
| `isbn` | `String` | Código ISBN |
| `anioPublicacion` | `int` | Año de publicación |

Métodos:

- `mostrarInformacion()`: imprime todos los datos del libro en consola con formato legible.
- `esAntiguo()`: devuelve `true` si el libro tiene más de 20 años (respecto al año actual).

### Parte B — Clase `Main`

En `Main`, crea **al menos tres objetos** `Libro` distintos y:

1. Muestra la información de cada uno.
2. Indica cuáles son antiguos.

## Ejemplo de salida esperada

```
--- Libro ---
Título: Don Quijote de la Mancha
Autor: Miguel de Cervantes
ISBN: 978-84-376-0494-7
Año: 1605
¿Es antiguo?: true
```

## Pistas

- Usa `LocalDate.now().getYear()` para obtener el año actual.
- Los atributos pueden ser `public` en este ejercicio inicial; en el ejercicio 02 los harás privados.

## Criterios de corrección

- [ ] Existe la clase `Libro` con los cuatro atributos indicados
- [ ] Los métodos `mostrarInformacion()` y `esAntiguo()` funcionan correctamente
- [ ] Se instancian al menos 3 objetos en `Main`
- [ ] El código compila sin errores
