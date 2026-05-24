public class EmpleadoTemporal extends Empleado {

    private int mesesContrato;
    private double importeMes;

    public EmpleadoTemporal(String nombre, String dni, int mesesContrato, double importeMes) {
        super(nombre, dni, 0);
        this.mesesContrato = mesesContrato;
        this.importeMes = importeMes;
    }

    @Override
    public double calcularSalario() {
        return mesesContrato * importeMes;
    }
}
