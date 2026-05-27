# Ejercicio 02 — DDL: definición de datos

**Área:** Bases de datos
**Nivel:** Básico  
**Conceptos:** CREATE TABLE, PRIMARY KEY, FOREIGN KEY, CHECK, UNIQUE

## Enunciado

A partir del diseño de la academia de idiomas (ejercicio 01), escribe el script SQL completo para crear la base de datos.

### Requisitos del script

1. Crear la base de datos `academia_idiomas` (si no existe).
2. Usar `academia_idiomas` como base activa.
3. Crear las tablas en orden correcto (respetando dependencias de FK):

| Tabla | Restricciones destacadas |
|-------|--------------------------|
| `profesor` | PK: `dni`; `email` UNIQUE |
| `alumno` | PK: `dni`; `email` UNIQUE |
| `curso` | PK: `codigo`; FK a `profesor(dni)`; CHECK nivel IN ('A1','A2','B1','B2','C1','C2') |
| `matricula` | FK a `alumno` y `curso`; UNIQUE(alumno_dni, curso_codigo); CHECK nota BETWEEN 0 AND 10 o NULL |

4. Índice en `curso(idioma)` para acelerar búsquedas por idioma.
5. Al final, un `DESCRIBE` o consulta a `INFORMATION_SCHEMA` de cada tabla.

### Datos de prueba

Inserta al menos:

- 2 profesores
- 3 alumnos
- 4 cursos (distintos idiomas/niveles)
- 6 matrículas

Guarda el script como `crear_academia.sql`.

## Pistas

```sql
CREATE TABLE profesor (
    dni VARCHAR(9) PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
  ...
);

ALTER TABLE curso
    ADD CONSTRAINT fk_curso_profesor
    FOREIGN KEY (profesor_dni) REFERENCES profesor(dni)
    ON DELETE RESTRICT ON UPDATE CASCADE;
```

## Criterios de corrección

- [ ] Orden de creación correcto
- [ ] Restricciones CHECK y UNIQUE aplicadas
- [ ] FK con acciones definidas
- [ ] Script ejecutable sin errores en MySQL/MariaDB
