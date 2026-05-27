public abstract class Vehiculo {

    protected String matricula;
    protected String marca;
    protected String modelo;

    public Vehiculo(String matricula, String marca, String modelo) {
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
    }

    public abstract double calcularConsumo();

    public void mostrarFicha() {
        System.out.printf("%s %s (%s) — Consumismo: %.1f%n",
                marca, modelo, matricula, calcularConsumo());
    }
}
