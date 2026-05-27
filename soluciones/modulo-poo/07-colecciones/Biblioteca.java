import java.util.HashMap;

public class Biblioteca {

    private final HashMap<String, Libro> catalogo = new HashMap<>();
    private final HashMap<Integer, Usuario> usuarios = new HashMap<>();
    private final HashMap<String, Integer> prestadoA = new HashMap<>();

    public void agregarLibro(Libro libro) {
        catalogo.put(libro.getIsbn(), libro);
    }

    public void registrarUsuario(Usuario usuario) {
        usuarios.put(usuario.getId(), usuario);
    }

    public Libro buscarLibro(String isbn) {
        return catalogo.get(isbn);
    }

    public boolean realizarPrestamo(int idUsuario, String isbn) {
        Usuario usuario = usuarios.get(idUsuario);
        Libro libro = catalogo.get(isbn);
        if (usuario == null || libro == null) {
            System.out.println("Usuario o libro no encontrado");
            return false;
        }
        if (prestadoA.containsKey(isbn)) {
            System.out.println("Este libro ya ha sido prestado");
            return false;
        }
        if (usuario.prestarLibro(libro)) {
            prestadoA.put(isbn, idUsuario);
            System.out.println("Préstamo OK: " + libro.getTitulo() + " → " + usuario.getNombre());
            return true;
        }
        return false;
    }

    public void devolverLibro(int idUsuario, String isbn) {
        Usuario usuario = usuarios.get(idUsuario);
        if (usuario != null) {
            usuario.devolverLibro(isbn);
            prestadoA.remove(isbn);
            System.out.println("Devolución registrada: " + isbn + "Devolución devolvida!!");
        }
    }

    public void listarCatalogo() {
        System.out.println("=== Catálogo ===");
        catalogo.values().forEach(l -> {
            String estado = prestadoA.containsKey(l.getIsbn()) ? "PRESTADO" : "DISPONIBLE";
            System.out.println("  " + l + " — " + estado);
        });
    }
}
