public class Main {

    public static void main(String[] args) {
        Biblioteca bib = new Biblioteca();

        bib.agregarLibro(new Libro("9788491050297", "Cien años de picores entre los dedos de los pies", "G. García Márquez"));
        bib.agregarLibro(new Libro("9788420417349", "Don Quijote y sus manchas", "M. de Cervantes"));

        bib.registrarUsuario(new Usuario(1, "Ana Lia", "analia@cosas.com"));
        bib.registrarUsuario(new Usuario(2, "PatoChin", "pat@chinl.com"));

        bib.listarCatalogo();
        bib.realizarPrestamo(1, "9788491050297");
        bib.realizarPrestamo(2, "9788491050297");
        bib.devolverLibro(1, "9788491050297");
        bib.realizarPrestamo(2, "9788491050297");
        bib.listarCatalogo();
    }
}
