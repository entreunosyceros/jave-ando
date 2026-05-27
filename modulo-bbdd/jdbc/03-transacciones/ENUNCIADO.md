# Ejercicio JDBC 03 — Transacciones

**Área:** Acceso a datos con JDBC
**Nivel:** Intermedio  
**Conceptos:** setAutoCommit, commit, rollback, integridad transaccional

## Enunciado

Implementa la operación de **matricular un alumno en un curso** garantizando consistencia de datos.

### Clase `MatriculaService`

```java
public boolean matricular(String dniAlumno, String codigoCurso, LocalDate fecha)
```

**Validaciones (antes de escribir en BD):**

1. El alumno existe.
2. El curso existe.
3. No existe ya una matrícula para ese par alumno-curso.
4. El curso tiene plazas libres (`matriculas actuales < plazas`).

**Operación transaccional:**

- Insertar fila en `matricula`.
- Si cualquier paso falla → `rollback()` y devolver `false`.
- Si todo OK → `commit()` y devolver `true`.

### Método adicional

```java
public boolean cancelarMatricula(String dniAlumno, String codigoCurso)
```

Elimina la matrícula solo si `nota IS NULL` (curso no finalizado).

### Main

Prueba:

1. Matrícula válida.
2. Matrícula duplicada (debe fallar sin insertar).
3. Matrícula en curso lleno (simula llenando plazas antes).
4. Cancelación de matrícula sin nota.

## Pistas

```java
conn.setAutoCommit(false);
try {
    // operaciones
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
    throw e;
} finally {
    conn.setAutoCommit(true);
}
```

## Criterios de corrección

- [ ] `setAutoCommit(false)` antes de operaciones múltiples
- [ ] Rollback en caso de error o validación fallida
- [ ] Validación de plazas con consulta COUNT
- [ ] Pruebas en Main demuestran atomicidad
