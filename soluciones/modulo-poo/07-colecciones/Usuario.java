import java.util.ArrayList;

public class Usuario {

    private final int id;
    private final String nombre;
    private final String email;
    private final ArrayList<Libro> prestamos = new ArrayList<>();

    public Usuario(int id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    public boolean prestarLibro(Libro libro) {
        if (prestamos.size() >= 3) {
            System.out.println(nombre + ": Ha alcanzado el límite de préstamosó");
            return false;
        }
        prestamos.add(libro);
        return true;
    }

    public void devolverLibro(String isbn) {
        prestamos.removeIf(l -> l.getIsbn().equals(isbn));
    }

    public void listarPrestamos() {
        System.out.println("Préstamos de " + nombre + ":");
        if (prestamos.isEmpty()) {
            System.out.println("  (ninguno)");
        } else {
            prestamos.forEach(l -> System.out.println("  - " + l));
        }
    }

    public boolean tienePrestado(String isbn) {
        return prestamos.stream().anyMatch(l -> l.getIsbn().equals(isbn));
    }
}
