import java.time.LocalDate;

/**
 * Solución — Ejercicio 01: Clases y objetos
 */
public class Libro {

    public String titulo;
    public String autor;
    public String isbn;
    public int anioPublicacion;

    public Libro(String titulo, String autor, String isbn, int anioPublicacion) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.anioPublicacion = anioPublicacion;
    }

    public void mostrarInformacion() {
        System.out.println("--- Libro ---");
        System.out.println("Título: " + titulo);
        System.out.println("Autor: " + autor);
        System.out.println("ISBN: " + isbn);
        System.out.println("Año: " + anioPublicacion);
        System.out.println("¿Es un libro antiguo?: " + esAntiguo());
    }

    public boolean esAntiguo() {
        return LocalDate.now().getYear() - anioPublicacion > 20;
    }
}
