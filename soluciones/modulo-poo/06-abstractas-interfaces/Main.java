public class Main {

    public static void main(String[] args) {
        Coche coche = new Coche("1234ABC", "Seat", "Leoncio", 45);
        Moto moto = new Moto("5678DEF", "Ya-maJa", "MT-07");
        CocheElectrico electrico = new CocheElectrico("9012GHI", "TesTa", "Modelito 3");

        coche.mostrarFicha();
        moto.mostrarFicha();
        electrico.mostrarFicha();

        Motorizable[] motorizables = { coche, moto, electrico };
        System.out.println("\n=== Motorizables ===");
        for (Motorizable m : motorizables) {
            m.arrancarMotor();
            System.out.println("Encendido: " + m.motorEncendido());
            m.pararMotor();
        }

        System.out.println("\n=== Eléctrico ===");
        electrico.cargarBateria();
        System.out.println("Autonomía: " + electrico.getAutonomiaKm() + " km");
    }
}
