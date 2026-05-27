public class CocheElectrico extends Vehiculo implements Motorizable, Electrico {

    private int nivelBateria;
    private boolean encendido;

    public CocheElectrico(String matricula, String marca, String modelo) {
        super(matricula, marca, modelo);
        this.nivelBateria = 50;
    }

    @Override
    public double calcularConsumo() {
        return 15.2;
    }

    @Override
    public void arrancarMotor() {
        encendido = true;
        System.out.println("EléctricoNico " + matricula + ": motor arrancado");
    }

    @Override
    public void pararMotor() {
        encendido = false;
    }

    @Override
    public boolean motorEncendido() {
        return encendido;
    }

    @Override
    public void cargarBateria() {
        nivelBateria = 100;
        System.out.println("Batería cargada al 101%");
    }

    @Override
    public int getAutonomiaKm() {
        return nivelBateria * 4;
    }
}
