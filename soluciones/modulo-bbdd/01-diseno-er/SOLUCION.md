# Solución orientativa — Diseño E-R (Academia de idiomas)

> Consultar solo después de intentar el ejercicio.

---

## A) Diagrama entidad-relación

### Entidades y atributos

| Entidad   | Atributos | Clave primaria |
|-----------|-----------|----------------|
| **alumno** | dni, nombre, apellidos, email, telefono | dni |
| **profesor** | dni, nombre, apellidos, email, especialidad | dni |
| **curso** | codigo, nombre, idioma, nivel, plazas | codigo |

### Relaciones y cardinalidades

```
                    ┌─────────────┐
                    │  profesor  │
                    │  PK: dni   │
                    └──────┬──────┘
                           │ 1
                           │ imparte
                           │ N
                    ┌──────▼──────┐
                    │    curso    │
                    │ PK: codigo  │
                    └──────┬──────┘
                           │ N
                           │ matricula
                           │ N
                    ┌──────▼──────┐
                    │   alumno    │
                    │  PK: dni    │
                    └─────────────┘
```

- **profesor — curso** (`imparte`): **1:N**  
  Un profesor imparte varios cursos; cada curso tiene un solo profesor.

- **alumno — curso** (`matricula`): **N:M**  
  Un alumno puede matricularse en varios cursos y un curso tiene varios alumnos.  
  Atributos de la relación: `fecha_inscripcion`, `nota` (NULL si el curso no ha terminado).

### Representación textual (Chen)

```
profesor (dni, nombre, apellidos, email, especialidad)
    |
    |<1:N> imparte
    v
curso (codigo, nombre, idioma, nivel, plazas)

alumno (dni, nombre, apellidos, email, telefono)
    |
    |<N:M> matricula (fecha_inscripcion, nota)
    v
curso
```

---

## B) Modelo relacional

### Tabla `profesor`

| Columna | Tipo SQL | Restricciones |
|---------|----------|---------------|
| dni | VARCHAR(9) | PRIMARY KEY |
| nombre | VARCHAR(50) | NOT NULL |
| apellidos | VARCHAR(80) | NOT NULL |
| email | VARCHAR(100) | NOT NULL, UNIQUE |
| especialidad | VARCHAR(50) | NOT NULL |

### Tabla `alumno`

| Columna | Tipo SQL | Restricciones |
|---------|----------|---------------|
| dni | VARCHAR(9) | PRIMARY KEY |
| nombre | VARCHAR(50) | NOT NULL |
| apellidos | VARCHAR(80) | NOT NULL |
| email | VARCHAR(100) | NOT NULL, UNIQUE |
| telefono | VARCHAR(15) | NULL permitido |

### Tabla `curso`

| Columna | Tipo SQL | Restricciones |
|---------|----------|---------------|
| codigo | VARCHAR(10) | PRIMARY KEY |
| nombre | VARCHAR(100) | NOT NULL |
| idioma | VARCHAR(30) | NOT NULL |
| nivel | ENUM('A1','A2','B1','B2','C1','C2') | NOT NULL |
| plazas | INT | NOT NULL, CHECK (plazas > 0) |
| profesor_dni | VARCHAR(9) | NOT NULL, FK → profesor(dni) |

**FK `curso.profesor_dni`:**  
`ON DELETE RESTRICT` (no borrar profesor con cursos activos) · `ON UPDATE CASCADE`

### Tabla `matricula` (intermedia N:M)

| Columna | Tipo SQL | Restricciones |
|---------|----------|---------------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT |
| alumno_dni | VARCHAR(9) | NOT NULL, FK → alumno(dni) |
| curso_codigo | VARCHAR(10) | NOT NULL, FK → curso(codigo) |
| fecha_inscripcion | DATE | NOT NULL |
| nota | DECIMAL(4,2) | NULL, CHECK (0–10 si no es NULL) |

**Restricción adicional:** `UNIQUE (alumno_dni, curso_codigo)` — evita matricular dos veces al mismo alumno en el mismo curso.

**FK `matricula.alumno_dni`:** `ON DELETE CASCADE` · `ON UPDATE CASCADE`  
**FK `matricula.curso_codigo`:** `ON DELETE CASCADE` · `ON UPDATE CASCADE`

### Script SQL (implementación)

Puedes ejecutar este DDL en MySQL/MariaDB (o compararlo con el ejercicio 02):

```sql
-- Base de datos academia de idiomas (modelo relacional del E-R)

CREATE DATABASE IF NOT EXISTS academia_idiomas
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE academia_idiomas;

CREATE TABLE profesor (
    dni           VARCHAR(9)  PRIMARY KEY,
    nombre        VARCHAR(50) NOT NULL,
    apellidos     VARCHAR(80) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    especialidad  VARCHAR(50) NOT NULL
);

CREATE TABLE alumno (
    dni         VARCHAR(9)  PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL,
    apellidos   VARCHAR(80) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    telefono    VARCHAR(15)
);

CREATE TABLE curso (
    codigo        VARCHAR(10) PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    idioma        VARCHAR(30) NOT NULL,
    nivel         ENUM('A1','A2','B1','B2','C1','C2') NOT NULL,
    plazas        INT NOT NULL CHECK (plazas > 0),
    profesor_dni  VARCHAR(9) NOT NULL,
    CONSTRAINT fk_curso_profesor
        FOREIGN KEY (profesor_dni) REFERENCES profesor(dni)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE matricula (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    alumno_dni        VARCHAR(9)  NOT NULL,
    curso_codigo      VARCHAR(10) NOT NULL,
    fecha_inscripcion DATE        NOT NULL DEFAULT (CURRENT_DATE),
    nota              DECIMAL(4,2) CHECK (nota IS NULL OR (nota >= 0 AND nota <= 10)),
    CONSTRAINT fk_matricula_alumno
        FOREIGN KEY (alumno_dni) REFERENCES alumno(dni)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_matricula_curso
        FOREIGN KEY (curso_codigo) REFERENCES curso(codigo)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_matricula UNIQUE (alumno_dni, curso_codigo)
);
```

### Datos de ejemplo (opcional)

```sql
INSERT INTO profesor (dni, nombre, apellidos, email, especialidad) VALUES
('11111111A', 'María',  'Uno Cargado',  'maria.un@academia.es',  'Inglés'),
('22222222B', 'Pandereto', 'Adelways Pechofriada', 'pandereto.adelways@academia.es',  'Francés');

INSERT INTO alumno (dni, nombre, apellidos, email, telefono) VALUES
('33333333C', 'Ana',    'Lia Gorda',   'ana.li@email.com',   '600111222'),
('44444444D', 'Zinganillo',  'Salamandro Díaz',    'zinganillo.salamandro@email.com', '600333444');

INSERT INTO curso (codigo, nombre, idioma, nivel, plazas, profesor_dni) VALUES
('ING-B1', 'Inglés intermedio B1', 'Inglés',  'B1', 20, '11111111A'),
('FRA-A2', 'Francés elemental A2', 'Francés', 'A2', 15, '22222222B');

INSERT INTO matricula (alumno_dni, curso_codigo, fecha_inscripcion, nota) VALUES
('33333333C', 'ING-B1', '2025-09-15', NULL),
('33333333C', 'FRA-A2', '2025-09-20', 7.50),
('44444444D', 'ING-B1', '2025-09-16', 8.25);
```

---

## C) Normalización

### 1. ¿Están las tablas en 3FN?

**Sí**, el diseño propuesto cumple la **tercera forma normal (3FN)**:

| Forma normal | Comprobación |
|-------------|--------------|
| **1FN** | Todos los atributos son atómicos (un solo valor por celda). No hay listas ni grupos repetidos en una fila. |
| **2FN** | En `matricula`, con PK `id` (surrogate), ningún atributo no clave depende de una parte de la clave. Con PK compuesta `(alumno_dni, curso_codigo)`, `fecha_inscripcion` y `nota` dependen de **toda** la clave (son propios de esa matrícula concreta). |
| **3FN** | No hay dependencias transitivas: en `curso` no se guardan `nombre` ni `email` del profesor (solo `profesor_dni`); los datos del profesor están únicamente en `profesor`. Igual para alumno y matrícula. |

### 2. ¿Tabla intermedia para N:M?

**Sí.** La relación alumno–curso es **N:M**, por lo que hace falta la tabla **`matricula`**.

Atributos:

- `alumno_dni` (FK)
- `curso_codigo` (FK)
- `fecha_inscripcion` — atributo de la relación (requisito 6)
- `nota` — atributo de la relación, nullable (requisito 6)

**Clave primaria:** `id` autoincremental (surrogate) **o** PK compuesta `(alumno_dni, curso_codigo)` más `UNIQUE` para evitar duplicados. En esta solución se usa `id` + `UNIQUE(alumno_dni, curso_codigo)` por claridad y facilidad de referencia desde otras tablas.

---

## Resumen de cardinalidades (criterios de corrección)

| Requisito | Diseño |
|-----------|--------|
| Alumno con DNI único | Tabla `alumno`, PK `dni` |
| Profesor con DNI único | Tabla `profesor`, PK `dni` |
| Curso con código único | Tabla `curso`, PK `codigo` |
| Profesor 1:N curso | FK `curso.profesor_dni` |
| Alumno N:M curso | Tabla `matricula` |
| Fecha y nota en matrícula | Columnas en `matricula` |

Este mismo modelo se desarrolla en el **ejercicio 02 (DDL)** con el script `soluciones/modulo-bbdd/02-ddl/crear_academia.sql`.
