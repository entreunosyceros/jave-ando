# Ejercicio 06 — Clases abstractas e interfaces

**Área:** Programación orientada a objetos
**Nivel:** Básico  
**Conceptos:** interface, implements, múltiple herencia de interfaz

## Enunciado

Modela un parque de vehículos con interfaces que definen contratos de comportamiento.

### Interface `Motorizable`

```java
public interface Motorizable {
    void arrancarMotor();
    void pararMotor();
    boolean motorEncendido();
}
```

### Interface `Electrico`

```java
public interface Electrico {
    void cargarBateria();
    int getAutonomiaKm();
}
```

### Clase abstracta `Vehiculo`

Atributos: `matricula` (String), `marca` (String), `modelo` (String).

Métodos concretos: constructor, getters, `mostrarFicha()`.

Método abstracto: `double calcularConsumo()` (litros o kWh por 100 km).

### Implementaciones

| Clase | Implementa | Detalle |
|-------|------------|---------|
| `Coche` | `Motorizable` | Atributo `litrosDeposito`; consumo en L/100km |
| `Moto` | `Motorizable` | Igual que coche, menor consumo |
| `CocheElectrico` | `Motorizable`, `Electrico` | Atributo `nivelBateria`; autonomía según carga |

### Main

1. Crea objetos de cada tipo.
2. Guarda los `Motorizable` en un array y arranca/para el motor de cada uno.
3. Para los `Electrico`, muestra la autonomía tras cargar.

## Preguntas de reflexión

- ¿Por qué `CocheElectrico` puede implementar dos interfaces pero solo extender una clase?
- ¿Qué ocurre si una clase implementa `Motorizable` pero no define `arrancarMotor()`?

## Criterios de corrección

- [ ] Dos interfaces con métodos bien definidos
- [ ] Clase abstracta con al menos un método abstracto
- [ ] `CocheElectrico` implementa ambas interfaces
- [ ] Uso polimórfico del array `Motorizable[]`
