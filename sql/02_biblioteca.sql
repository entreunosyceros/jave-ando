--  Script de creación: biblioteca (proyecto integrador)
-- Ejecutar: mysql -u root -p < sql/02_biblioteca.sql

DROP DATABASE IF EXISTS biblioteca;
CREATE DATABASE biblioteca CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biblioteca;

CREATE TABLE usuario (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL,
    email   VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(15)
);

CREATE TABLE libro (
    isbn                  VARCHAR(13) PRIMARY KEY,
    titulo                VARCHAR(200) NOT NULL,
    autor                 VARCHAR(100) NOT NULL,
    anio                  INT CHECK (anio > 0),
    ejemplares_disponibles INT NOT NULL DEFAULT 1 CHECK (ejemplares_disponibles >= 0)
);

CREATE TABLE prestamo (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id       INT NOT NULL,
    isbn             VARCHAR(13) NOT NULL,
    fecha_prestamo   DATE NOT NULL DEFAULT (CURRENT_DATE),
    fecha_devolucion DATE NULL,
    CONSTRAINT fk_prestamo_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_prestamo_libro
        FOREIGN KEY (isbn) REFERENCES libro(isbn)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

INSERT INTO usuario (nombre, email, telefono) VALUES
('Pedro Gómez',   'pedro@email.com',  '611000111'),
('Laura Fernández','laura@email.com', '611000222');

INSERT INTO libro (isbn, titulo, autor, anio, ejemplares_disponibles) VALUES
('9788491050297', 'Cien años de soledad',     'Gabriel García Márquez', 1967, 3),
('9788420417349', 'Don Quijote de la Mancha', 'Miguel de Cervantes',    1605, 2),
('9780141439518', 'Pride and Prejudice',      'Jane Austen',            1813, 1);

SELECT 'Base de datos biblioteca creada correctamente.' AS mensaje;
