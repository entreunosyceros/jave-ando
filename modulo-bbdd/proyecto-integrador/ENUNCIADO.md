# Proyecto integrador — Gestión de biblioteca

**Área:** Proyecto integrador
**Nivel:** Integrador  
**Duración estimada:** 8–12 horas

## Descripción

Desarrolla una aplicación de consola para gestionar una biblioteca municipal, combinando POO y JDBC.

## Base de datos

Importa `sql/02_biblioteca.sql` o diseña y crea tu propio esquema con:

- `libro` (isbn PK, titulo, autor, anio, ejemplares_disponibles)
- `usuario` (id PK, nombre, email, telefono)
- `prestamo` (id PK, usuario_id FK, isbn FK, fecha_prestamo, fecha_devolucion NULL)

## Capa de dominio (POO)

| Clase | Responsabilidad |
|-------|-----------------|
| `Libro` | Entidad con lógica de disponibilidad |
| `Usuario` | Entidad usuario |
| `Prestamo` | Representa un préstamo activo o devuelto |
| `BibliotecaException` | Excepción de negocio |

## Capa de acceso a datos (JDBC)

| DAO | Operaciones mínimas |
|-----|---------------------|
| `LibroDAO` | CRUD + buscarPorAutor + actualizarDisponibles |
| `UsuarioDAO` | CRUD |
| `PrestamoDAO` | crearPrestamo, devolverPrestamo, listarActivosPorUsuario |

## Capa de servicio

`BibliotecaService`:

- `prestarLibro(int usuarioId, String isbn)`: transacción (insert prestamo + decrementar ejemplares)
- `devolverLibro(int prestamoId)`: transacción inversa
- `buscarLibros(String criterio)`: por título o autor

## Interfaz de usuario

Menú principal con las operaciones anteriores más listados de libros, usuarios y préstamos activos.

## Requisitos de calidad

1. Separación clara: modelo / DAO / servicio / UI.
2. PreparedStatement en todas las consultas.
3. Transacciones en préstamo y devolución.
4. Excepciones de negocio capturadas en la UI.
5. Al menos una consulta con JOIN (prestamos con datos de libro y usuario).

## Entregables

```
proyecto-integrador/
├── src/
│   ├── modelo/
│   ├── dao/
│   ├── servicio/
│   ├── ui/
│   └── Main.java
├── config/database.properties
└── README.md (instrucciones de ejecución)
```

## Rúbrica de evaluación

| Criterio | Puntos |
|----------|--------|
| Modelo POO coherente | 20% |
| DAO con JDBC correcto | 25% |
| Transacciones | 20% |
| Menú funcional | 20% |
| Código documentado y limpio | 15% |
