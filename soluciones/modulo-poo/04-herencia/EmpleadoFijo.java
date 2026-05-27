public class EmpleadoFijo extends Empleado {

    private double plusAntiguedad;

    public EmpleadoFijo(String nombre, String dni, double salarioBase, double plusAntiguedad) {
        super(nombre, dni, salarioBase);
        this.plusAntiguedad = plusAntiguedad;
    }

    @Override
    public double calcularSalario() {
        return salarioBase + plusAntiguedad;
    }
}
