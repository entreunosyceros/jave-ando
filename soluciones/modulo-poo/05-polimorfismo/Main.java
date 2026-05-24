import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        ArrayList<Figura> figuras = new ArrayList<>();
        figuras.add(new Circulo("rojo", 3));
        figuras.add(new Rectangulo("azul", 4, 6));
        figuras.add(new Triangulo("verde", 3, 4, 5));
        figuras.add(new Circulo("amarillo", 1.5));

        System.out.println("=== Figuras ===");
        double areaTotal = 0;
        for (Figura f : figuras) {
            f.describir();
            areaTotal += f.calcularArea();
        }
        System.out.printf("%nÁrea total: %.2f%n", areaTotal);
    }
}
