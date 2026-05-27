# Ejercicio 03 — Constructores y métodos estáticos

**Área:** Programación orientada a objetos
**Nivel:** Básico  
**Conceptos:** sobrecarga de constructores, `this`, métodos estáticos

## Enunciado

Crea una clase `Rectangulo` para representar figuras geométricas.

### Atributos (private)

- `base` (`double`)
- `altura` (`double`)

### Constructores (sobrecarga)

1. Sin parámetros: crea un cuadrado de 1×1.
2. Un parámetro (`double lado`): crea un cuadrado del lado indicado.
3. Dos parámetros (`double base`, `double altura`): rectángulo general.

Usa `this()` para evitar duplicar código entre constructores.

### Métodos de instancia

- `calcularArea()`: devuelve base × altura.
- `calcularPerimetro()`: devuelve 2 × (base + altura).
- `esCuadrado()`: devuelve `true` si base == altura.

### Métodos estáticos

- `compararAreas(Rectangulo r1, Rectangulo r2)`: devuelve el rectángulo de mayor área (si empatan, devuelve `r1`).
- `crearDesdePerimetro(double perimetro, double base)`: calcula la altura a partir del perímetro y la base, y devuelve un nuevo `Rectangulo`.

### Main

Demuestra los tres constructores, los métodos de instancia y al menos una llamada a cada método estático.

## Criterios de corrección

- [ ] Tres constructores encadenados con `this()`
- [ ] Métodos estáticos invocados sin instancia (`Rectangulo.compararAreas(...)`)
- [ ] Cálculos correctos de área y perímetro
