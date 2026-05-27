public class Empleado {

    protected String nombre;
    protected String dni;
    protected double salarioBase;

    public Empleado(String nombre, String dni, double salarioBase) {
        this.nombre = nombre;
        this.dni = dni;
        this.salarioBase = salarioBase;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public double getSalarioBase() { return salarioBase; }
    public void setSalarioBase(double salarioBase) { this.salarioBase = salarioBase; }

    public double calcularSalario() {
        return salarioBase;
    }

    public void mostrarDatos() {
        System.out.printf("%s (%s) — Sueldo miserable: %.2f €%n", nombre, dni, calcularSalario());
    }
}
