--  Script de creación: academia_idiomas
-- Ejecutar: mysql -u root -p < sql/01_crear_base_datos.sql

DROP DATABASE IF EXISTS academia_idiomas;
CREATE DATABASE academia_idiomas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE academia_idiomas;

-- Profesores
CREATE TABLE profesor (
    dni         VARCHAR(9)  PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL,
    apellidos   VARCHAR(80) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    especialidad VARCHAR(50) NOT NULL
);

-- Alumnos
CREATE TABLE alumno (
    dni         VARCHAR(9)  PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL,
    apellidos   VARCHAR(80) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    telefono    VARCHAR(15)
);

-- Cursos
CREATE TABLE curso (
    codigo      VARCHAR(10) PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    idioma      VARCHAR(30) NOT NULL,
    nivel       ENUM('A1','A2','B1','B2','C1','C2') NOT NULL,
    plazas      INT NOT NULL CHECK (plazas > 0),
    profesor_dni VARCHAR(9) NOT NULL,
    CONSTRAINT fk_curso_profesor
        FOREIGN KEY (profesor_dni) REFERENCES profesor(dni)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX idx_curso_idioma ON curso(idioma);

-- Matrículas (tabla intermedia N:M con atributos)
CREATE TABLE matricula (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    alumno_dni      VARCHAR(9) NOT NULL,
    curso_codigo    VARCHAR(10) NOT NULL,
    fecha_inscripcion DATE NOT NULL DEFAULT (CURRENT_DATE),
    nota            DECIMAL(4,2) CHECK (nota IS NULL OR (nota >= 0 AND nota <= 10)),
    CONSTRAINT fk_matricula_alumno
        FOREIGN KEY (alumno_dni) REFERENCES alumno(dni)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_matricula_curso
        FOREIGN KEY (curso_codigo) REFERENCES curso(codigo)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_matricula UNIQUE (alumno_dni, curso_codigo)
);

-- Datos de prueba
INSERT INTO profesor (dni, nombre, apellidos, email, especialidad) VALUES
('11111111A', 'María',  'López Sánchez',  'maria.lopez@academia.es',  'Inglés'),
('22222222B', 'Carlos', 'Ruiz Fernández', 'carlos.ruiz@academia.es',  'Francés'),
('33333333C', 'Elena',  'García Muñoz',   'elena.garcia@academia.es', 'Alemán');

INSERT INTO alumno (dni, nombre, apellidos, email, telefono) VALUES
('12345678A', 'Ana',    'García López',   'ana.garcia@email.com',    '600111222'),
('87654321B', 'Luis',   'Martín Ruiz',    'luis.martin@email.com',   '600333444'),
('11223344C', 'Sofía',  'Pérez Díaz',     'sofia.perez@email.com',   '600555666'),
('44332211D', 'Pablo',  'Sánchez Torres', 'pablo.sanchez@email.com', '600777888');

INSERT INTO curso (codigo, nombre, idioma, nivel, plazas, profesor_dni) VALUES
('ING-A1', 'Inglés Elemental',     'Inglés',  'A1', 20, '11111111A'),
('ING-B1', 'Inglés Intermedio',    'Inglés',  'B1', 15, '11111111A'),
('FRA-A2', 'Francés Básico',       'Francés', 'A2', 18, '22222222B'),
('ALM-B1', 'Alemán Intermedio',    'Alemán',  'B1', 12, '33333333C');

INSERT INTO matricula (alumno_dni, curso_codigo, fecha_inscripcion, nota) VALUES
('12345678A', 'ING-A1', '2025-09-15', 8.50),
('12345678A', 'ING-B1', '2026-01-10', NULL),
('87654321B', 'ING-A1', '2025-09-15', 6.75),
('87654321B', 'FRA-A2', '2025-10-01', 7.00),
('11223344C', 'FRA-A2', '2025-10-01', 9.25),
('11223344C', 'ALM-B1', '2026-01-20', NULL),
('44332211D', 'ING-B1', '2026-01-10', NULL);

SELECT 'Base de datos academia_idiomas creada correctamente.' AS mensaje;
