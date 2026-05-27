public class Triangulo extends Figura {

    private double ladoA;
    private double ladoB;
    private double ladoC;

    public Triangulo(String color, double ladoA, double ladoB, double ladoC) {
        super(color);
        this.ladoA = ladoA;
        this.ladoB = ladoB;
        this.ladoC = ladoC;
    }

    @Override
    public double calcularArea() {
        double s = calcularPerimetro() / 2;
        return Math.sqrt(s * (s - ladoA) * (s - ladoB) * (s - ladoC));
    }

    @Override
    public double calcularPerimetro() {
        return ladoA + ladoB + ladoC;
    }
}
