# Ejercicio JDBC 02 — CRUD con PreparedStatement

**Área:** Acceso a datos con JDBC
**Nivel:** Básico  
**Conceptos:** PreparedStatement, parámetros, ResultSet, POJO

## Enunciado

Implementa el acceso a datos de alumnos usando el patrón DAO (Data Access Object).

### Clase modelo `Alumno`

Campos: `dni`, `nombre`, `apellidos`, `email`, `telefono` + constructor, getters y `toString()`.

### Clase `AlumnoDAO`

| Método | SQL | Descripción |
|--------|-----|-------------|
| `List<Alumno> findAll()` | SELECT * ORDER BY apellidos | Todos los alumnos |
| `Alumno findByDni(String dni)` | SELECT … WHERE dni = ? | Un alumno o null |
| `boolean insert(Alumno a)` | INSERT INTO alumno … | Alta; false si DNI duplicado |
| `boolean update(Alumno a)` | UPDATE … WHERE dni = ? | Modifica email y teléfono |
| `boolean delete(String dni)` | DELETE … WHERE dni = ? | Borra si no tiene matrículas |

Usa **PreparedStatement** en todos los métodos. Mapea filas a objetos `Alumno` en un método privado `mapResultSetToAlumno(ResultSet rs)`.

### Clase `Main`

Menú por consola:

```
1. Listar alumnos
2. Buscar por DNI
3. Insertar alumno
4. Actualizar email/teléfono
5. Eliminar alumno
0. Salir
```

## Restricciones

- No uses concatenación de strings para SQL (`"SELECT ... WHERE dni = '" + dni + "'"` está **prohibido**).
- Captura `SQLException` y muestra mensajes útiles.

## Criterios de corrección

- [ ] PreparedStatement con parámetros `?`
- [ ] CRUD completo funcional
- [ ] Mapeo ResultSet → Alumno centralizado
- [ ] Menú interactivo en Main
