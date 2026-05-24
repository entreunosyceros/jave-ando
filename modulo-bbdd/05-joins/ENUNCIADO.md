# Ejercicio 05 — JOINs y subconsultas

**Área:** Bases de datos
**Nivel:** Intermedio  
**Conceptos:** INNER JOIN, LEFT JOIN, subconsultas correlacionadas, EXISTS

## Enunciado

Resuelve las siguientes consultas sobre `academia_idiomas`. Archivo: `joins_subconsultas.sql`.

### JOINs

1. **INNER JOIN**: Listado de matrículas con nombre del alumno, curso, idioma y nombre del profesor.
2. **LEFT JOIN**: Todos los cursos con el número de alumnos matriculados (0 si no hay ninguno).
3. **LEFT JOIN**: Todos los profesores y sus cursos; mostrar profesores sin cursos asignados.
4. **Self-join / múltiples tablas**: Alumnos matriculados en cursos del mismo idioma que imparte su profesor (nombre alumno, curso, profesor).

### Subconsultas

5. Alumnos matriculados en **todos** los cursos de Inglés ( división relacional simplificada: alumnos cuyo número de cursos de inglés = total de cursos de inglés).
6. Cursos cuyo número de matrículas supera la media de matrículas por curso.
7. Profesores que imparten algún curso con nota media superior a 7.

### EXISTS / NOT EXISTS

8. Alumnos que tienen al menos una matrícula sin nota (curso en curso).
9. Cursos que **no** tienen ninguna matrícula.

### Vista (DDL + consulta)

10. Crea una vista `v_resumen_cursos` con: código, nombre, idioma, profesor, total_matriculas, nota_media. Consulta la vista ordenada por nota_media DESC.

## Criterios de corrección

- [ ] Uso explícito de JOIN (no solo WHERE implícito)
- [ ] Subconsulta 5 correcta (todos los cursos de un idioma)
- [ ] Vista creada y consultada
- [ ] Consultas legibles con alias de tabla
