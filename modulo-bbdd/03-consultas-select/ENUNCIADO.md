# Ejercicio 03 — Consultas SELECT

**Área:** Bases de datos
**Nivel:** Básico  
**Conceptos:** SELECT, WHERE, ORDER BY, GROUP BY, HAVING, funciones agregadas

## Enunciado

Usa la base de datos `academia_idiomas` (script en `sql/01_crear_base_datos.sql`).

Escribe una consulta SQL para cada apartado. Guarda todas en `consultas_select.sql`.

### Consultas básicas

1. Listar todos los alumnos ordenados por apellidos y nombre.
2. Cursos de idioma **Inglés** con más de 15 plazas.
3. Profesores cuya especialidad contenga la palabra "Francés".

### Agregaciones

4. Número total de alumnos matriculados por curso (código y nombre del curso).
5. Nota media por curso, solo cursos con al menos 2 notas registradas (no NULL).
6. Idiomas distintos ofrecidos, con el número de cursos de cada uno.

### Filtros avanzados

7. Alumnos que **no** están matriculados en ningún curso de nivel C1 o C2.
8. Cursos con **todas** las plazas ocupadas (matrículas ≥ plazas).
9. Top 3 alumnos con mejor nota media (solo alumnos con al menos 2 notas).

### Extra

10. Listado de matrículas con: nombre completo del alumno, nombre del curso, profesor y nota (use alias legibles).

## Formato de entrega

```sql
-- Consulta 1
SELECT ...;

-- Consulta 2
SELECT ...;
```

## Criterios de corrección

- [ ] 10 consultas funcionales
- [ ] Uso correcto de GROUP BY / HAVING donde corresponda
- [ ] Alias descriptivos en columnas calculadas
- [ ] Consulta 8 usa subconsulta o JOIN con HAVING COUNT
