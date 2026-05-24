# Ejercicio 01 — Diseño entidad-relación

**Área:** Bases de datos
**Nivel:** Básico  
**Conceptos:** modelo E-R, cardinalidad, claves, normalización

## Enunciado

Una academia de idiomas necesita una base de datos para gestionar cursos, profesores y alumnos.

### Requisitos funcionales

1. Cada **alumno** tiene: DNI (único), nombre, apellidos, email y teléfono.
2. Cada **profesor** tiene: DNI (único), nombre, apellidos, email y especialidad (ej. "Inglés", "Francés").
3. Cada **curso** tiene: código (único), nombre, idioma, nivel (A1–C2) y número de plazas.
4. Un profesor imparte **varios cursos**, pero cada curso tiene **un solo profesor**.
5. Un alumno puede matricularse en **varios cursos** y un curso tiene **varios alumnos**.
6. En cada matrícula se registra la **fecha de inscripción** y la **nota final** (puede ser NULL si el curso no ha terminado).

### Tareas

#### A) Diagrama E-R

Dibuja el diagrama entidad-relación (papel, draw.io, dbdiagram.io…) indicando:

- Entidades y atributos
- Relaciones con cardinalidad (1:1, 1:N, N:M)
- Claves primarias de cada entidad

#### B) Modelo relacional

Pasa el diagrama a tablas relacionales. Para cada tabla indica:

- Nombre de columnas y tipos de datos SQL aproximados
- Clave primaria (PK)
- Claves foráneas (FK) con acción ON DELETE / ON UPDATE

#### C) Normalización

1. ¿Están las tablas en **3FN**? Justifica.
2. ¿Sería necesaria alguna **tabla intermedia** para la relación N:M? ¿Qué atributos tendría?

## Entrega

Documento (PDF o Markdown) con diagrama, tablas y respuestas a las preguntas de normalización.

## Solución orientativa

Consulta `soluciones/modulo-bbdd/01-diseno-er/SOLUCION.md` tras completar el ejercicio.

## Criterios de corrección

- [ ] Entidades correctamente identificadas
- [ ] Cardinalidades coherentes con los requisitos
- [ ] Tabla intermedia `matricula` con PK compuesta o surrogate key
- [ ] Justificación de 3FN
