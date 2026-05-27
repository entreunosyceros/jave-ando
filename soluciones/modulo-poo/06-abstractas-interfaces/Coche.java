public class Coche extends Vehiculo implements Motorizable {

    private double litrosDeposito;
    private boolean encendido;

    public Coche(String matricula, String marca, String modelo, double litrosDeposito) {
        super(matricula, marca, modelo);
        this.litrosDeposito = litrosDeposito;
    }

    @Override
    public double calcularConsumo() {
        return 6.5;
    }

    @Override
    public void arrancarMotor() {
        encendido = true;
        System.out.println("CocheCITO " + matricula + ": motor arrancado");
    }

    @Override
    public void pararMotor() {
        encendido = false;
        System.out.println("CocheCITO" + matricula + ": motor parado");
    }

    @Override
    public boolean motorEncendido() {
        return encendido;
    }
}
