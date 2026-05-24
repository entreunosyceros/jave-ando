/**
 * Solución — Ejercicio 03: Constructores y métodos estáticos
 */
public class Rectangulo {

    private double base;
    private double altura;

    public Rectangulo() {
        this(1);
    }

    public Rectangulo(double lado) {
        this(lado, lado);
    }

    public Rectangulo(double base, double altura) {
        this.base = base;
        this.altura = altura;
    }

    public double calcularArea() {
        return base * altura;
    }

    public double calcularPerimetro() {
        return 2 * (base + altura);
    }

    public boolean esCuadrado() {
        return base == altura;
    }

    public static Rectangulo compararAreas(Rectangulo r1, Rectangulo r2) {
        return r1.calcularArea() >= r2.calcularArea() ? r1 : r2;
    }

    public static Rectangulo crearDesdePerimetro(double perimetro, double base) {
        double altura = (perimetro / 2) - base;
        return new Rectangulo(base, altura);
    }

    @Override
    public String toString() {
        return String.format("Rectángulo %.1f x %.1f (área %.2f)", base, altura, calcularArea());
    }
}
