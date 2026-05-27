public abstract class Figura {

    protected String color;

    public Figura(String color) {
        this.color = color;
    }

    public abstract double calcularArea();
    public abstract double calcularPerimetro();

    public void describir() {
        System.out.printf("Figura %s — Área: %.2f — Perímetro: %.2f%n",
                color, calcularArea(), calcularPerimetro());
    }
}
