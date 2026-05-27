public class Moto extends Vehiculo implements Motorizable {

    private boolean encendido;

    public Moto(String matricula, String marca, String modelo) {
        super(matricula, marca, modelo);
    }

    @Override
    public double calcularConsumo() {
        return 3.8;
    }

    @Override
    public void arrancarMotor() {
        encendido = true;
        System.out.println("Moto " + matricula + ": motor arrancado");
    }

    @Override
    public void pararMotor() {
        encendido = false;
        System.out.println("Moto " + matricula + ": motor parado");
    }

    @Override
    public boolean motorEncendido() {
        return encendido;
    }
}
