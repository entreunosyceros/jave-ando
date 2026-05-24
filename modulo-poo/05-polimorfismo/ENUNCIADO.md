# Ejercicio 05 — Polimorfismo

**Área:** Programación orientada a objetos
**Nivel:** Básico  
**Conceptos:** ligadura dinámica, `@Override`, colecciones polimórficas

## Enunciado

Diseña un sistema de figuras geométricas con comportamiento polimórfico.

### Clase abstracta `Figura`

```java
public abstract class Figura {
    protected String color;

    public Figura(String color) { ... }

    public abstract double calcularArea();
    public abstract double calcularPerimetro();

    public void describir() {
        System.out.printf("Figura %s - Área: %.2f - Perímetro: %.2f%n",
            color, calcularArea(), calcularPerimetro());
    }
}
```

### Subclases

Implementa al menos:

| Clase | Atributos | Fórmulas |
|-------|-----------|----------|
| `Circulo` | `radio` | Área = π·r², Perímetro = 2·π·r |
| `Rectangulo` | `base`, `altura` | Área = b·h, Perímetro = 2·(b+h) |
| `Triangulo` | `ladoA`, `ladoB`, `ladoC` | Perímetro = suma lados; Área con fórmula de Herón |

### Main

1. Crea una `ArrayList<Figura>` con al menos 4 figuras de tipos distintos.
2. Recorre la lista llamando a `describir()` en cada elemento.
3. Calcula y muestra el **área total** de todas las figuras.

## Extra (opcional)

Añade un método estático `Figura crearFigura(String tipo, double... params)` que devuelva la figura correspondiente según el tipo indicado (`"circulo"`, `"rectangulo"`, `"triangulo"`).

## Criterios de corrección

- [ ] Clase abstracta con métodos abstractos
- [ ] Al menos 3 subclases con `@Override`
- [ ] Uso de `ArrayList<Figura>` (polimorfismo)
- [ ] Cálculo correcto del área total
