public class Main {

    public static void main(String[] args) {
        Rectangulo defecto = new Rectangulo();
        Rectangulo cuadrado = new Rectangulo(5);
        Rectangulo rect = new Rectangulo(4, 6);

        System.out.println(defecto + " | cuadrado: " + defecto.esCuadrado());
        System.out.println(cuadrado + " | cuadrado: " + cuadrado.esCuadrado());
        System.out.println(rect + " | perímetro: " + rect.calcularPerimetro());

        Rectangulo mayor = Rectangulo.compararAreas(cuadrado, rect);
        System.out.println("Mayor área: " + mayor);

        Rectangulo desdePerimetro = Rectangulo.crearDesdePerimetro(20, 4);
        System.out.println("Desde perímetro 20 y base 4: " + desdePerimetro);
    }
}
