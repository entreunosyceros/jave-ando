# Ejercicio 07 — Colecciones y relaciones entre clases

**Área:** Programación orientada a objetos
**Nivel:** Básico  
**Conceptos:** ArrayList, HashMap, agregación, asociación

## Enunciado

Implementa un sistema simple de gestión de una **biblioteca universitaria**.

### Clase `Libro`

- `isbn` (String), `titulo` (String), `autor` (String)
- Constructor, getters, `equals()` y `hashCode()` basados en `isbn`
- `toString()` legible

### Clase `Usuario`

- `id` (int), `nombre` (String), `email` (String)
- Lista de libros prestados: `ArrayList<Libro>`

Métodos:

- `prestarLibro(Libro libro)`: añade a la lista (máximo 3 libros).
- `devolverLibro(String isbn)`: elimina el libro por ISBN.
- `listarPrestamos()`: muestra los libros prestados.

### Clase `Biblioteca`

Atributos:

- `catalogo`: `HashMap<String, Libro>` (clave = ISBN)
- `usuarios`: `HashMap<Integer, Usuario>` (clave = id)

Métodos:

- `agregarLibro(Libro libro)`
- `registrarUsuario(Usuario usuario)`
- `buscarLibro(String isbn)`: devuelve el libro o `null`
- `realizarPrestamo(int idUsuario, String isbn)`: valida existencia y disponibilidad
- `listarCatalogo()`: muestra todos los libros

> Un libro prestado no puede prestarse a otro usuario hasta que se devuelva.

### Main

Simula: alta de libros, registro de usuarios, préstamos, intento de préstamo duplicado y devolución.

## Criterios de corrección

- [ ] Uso correcto de `HashMap` y `ArrayList`
- [ ] Relación de agregación Biblioteca–Libro/Usuario
- [ ] Validación de límite de préstamos y disponibilidad
- [ ] `equals`/`hashCode` en `Libro`
