# Ejercicio 04 — Manipulación de datos (DML)

**Área:** Bases de datos
**Nivel:** Básico  
**Conceptos:** INSERT, UPDATE, DELETE, transacciones

## Enunciado

Operaciones de mantenimiento sobre `academia_idiomas`. Escribe cada operación en `manipulacion_datos.sql`.

### INSERT

1. Matricular al alumno con DNI `12345678A` en el curso `ING-B1` con fecha actual.
2. Insertar un curso nuevo `ALM-A2` (Alemán A2, 20 plazas) del profesor existente más idóneo (elige uno con especialidad relacionada).

### UPDATE

3. Subir un 10% las plazas de todos los cursos de nivel A1 y A2.
4. Asignar nota **5.0** a matrículas con más de 6 meses de antigüedad que aún tienen `nota IS NULL`.
5. Cambiar el email del alumno `12345678A` a `nuevo.email@academia.es`.

### DELETE

6. Eliminar matrículas de cursos que ya no existen (si las hubiera tras pruebas).
7. Eliminar alumnos que no tienen ninguna matrícula activa **solo si** no tienen notas registradas.

### Transacciones

8. Realiza una transferencia de matrícula: el alumno `12345678A` deja el curso `ING-B1` y se matricula en `ING-B2` en una sola transacción (`START TRANSACTION` … `COMMIT`). Incluye `ROLLBACK` de prueba comentado.

## Preguntas (responder en comentarios SQL)

- ¿Qué ocurre si intentas borrar un profesor que tiene cursos asignados?
- ¿Por qué conviene usar transacciones en la operación 8?

## Criterios de corrección

- [ ] INSERT con subconsultas donde sea necesario
- [ ] UPDATE con condiciones en WHERE
- [ ] DELETE seguro (no borra datos con integridad referencial)
- [ ] Transacción 8 con COMMIT y ROLLBACK documentado
