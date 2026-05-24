-- Solución orientativa — Consultas SELECT (ejercicio 03)
-- Base de datos: academia_idiomas

-- 1. Alumnos ordenados
SELECT dni, nombre, apellidos, email
FROM alumno
ORDER BY apellidos, nombre;

-- 2. Cursos de Inglés con más de 15 plazas
SELECT codigo, nombre, plazas
FROM curso
WHERE idioma = 'Inglés' AND plazas > 15;

-- 3. Profesores con especialidad relacionada con Francés
SELECT dni, nombre, apellidos, especialidad
FROM profesor
WHERE especialidad LIKE '%Francés%';

-- 4. Alumnos matriculados por curso
SELECT c.codigo, c.nombre, COUNT(m.id) AS total_alumnos
FROM curso c
LEFT JOIN matricula m ON c.codigo = m.curso_codigo
GROUP BY c.codigo, c.nombre;

-- 5. Nota media por curso (mínimo 2 notas)
SELECT c.codigo, c.nombre, ROUND(AVG(m.nota), 2) AS nota_media
FROM curso c
JOIN matricula m ON c.codigo = m.curso_codigo
WHERE m.nota IS NOT NULL
GROUP BY c.codigo, c.nombre
HAVING COUNT(m.nota) >= 2;

-- 6. Idiomas con número de cursos
SELECT idioma, COUNT(*) AS num_cursos
FROM curso
GROUP BY idioma;

-- 7. Alumnos sin matrícula en C1/C2
SELECT a.dni, a.nombre, a.apellidos
FROM alumno a
WHERE a.dni NOT IN (
    SELECT m.alumno_dni
    FROM matricula m
    JOIN curso c ON m.curso_codigo = c.codigo
    WHERE c.nivel IN ('C1', 'C2')
);

-- 8. Cursos con plazas agotadas
SELECT c.codigo, c.nombre, c.plazas, COUNT(m.id) AS matriculas
FROM curso c
JOIN matricula m ON c.codigo = m.curso_codigo
GROUP BY c.codigo, c.nombre, c.plazas
HAVING COUNT(m.id) >= c.plazas;

-- 9. Top 3 alumnos por nota media
SELECT a.dni, a.nombre, a.apellidos, ROUND(AVG(m.nota), 2) AS nota_media
FROM alumno a
JOIN matricula m ON a.dni = m.alumno_dni
WHERE m.nota IS NOT NULL
GROUP BY a.dni, a.nombre, a.apellidos
HAVING COUNT(m.nota) >= 2
ORDER BY nota_media DESC
LIMIT 3;

-- 10. Matrículas con datos completos
SELECT
    CONCAT(a.nombre, ' ', a.apellidos) AS alumno,
    c.nombre AS curso,
    CONCAT(p.nombre, ' ', p.apellidos) AS profesor,
    m.nota
FROM matricula m
JOIN alumno a ON m.alumno_dni = a.dni
JOIN curso c ON m.curso_codigo = c.codigo
JOIN profesor p ON c.profesor_dni = p.dni;
