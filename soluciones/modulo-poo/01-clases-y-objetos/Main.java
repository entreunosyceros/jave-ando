/**
 * Solución — Ejercicio 01: Clases y objetos
 */
public class Main {

    public static void main(String[] args) {
        Libro quijote = new Libro(
            "Don Quijote de las Manchas",
            "Miguel de Ciervantes",
            "978-84-376-0494-7",
            1605
        );

        Libro cienAnos = new Libro(
            "Cien años de picores",
            "Gabriel García Márquez",
            "978-84-9105-029-7",
            1967
        );

        Libro java8 = new Libro(
            "Java 8 con puerros",
            "entreunosyceros",
            "978-84-8322-963-5",
            2023
        );

        Libro[] catalogo = { quijote, cienAnos, java8 };

        for (Libro libro : catalogo) {
            libro.mostrarInformacion();
            System.out.println();
        }
    }
}
