-- Solución orientativa — JOINs y subconsultas (ejercicio 05)
USE academia_idiomas;

-- 1. Matrículas con datos completos
SELECT CONCAT(a.nombre, ' ', a.apellidos) AS alumno,
       c.nombre AS curso, c.idioma,
       CONCAT(p.nombre, ' ', p.apellidos) AS profesor
FROM matricula m
INNER JOIN alumno a ON m.alumno_dni = a.dni
INNER JOIN curso c ON m.curso_codigo = c.codigo
INNER JOIN profesor p ON c.profesor_dni = p.dni;

-- 2. Cursos con número de matriculados
SELECT c.codigo, c.nombre, COUNT(m.id) AS matriculas
FROM curso c
LEFT JOIN matricula m ON c.codigo = m.curso_codigo
GROUP BY c.codigo, c.nombre;

-- 3. Profesores y cursos (incluye sin cursos)
SELECT p.dni, p.nombre, c.codigo
FROM profesor p
LEFT JOIN curso c ON p.dni = c.profesor_dni;

-- 5. Alumnos en todos los cursos de Inglés
SELECT a.dni, a.nombre
FROM alumno a
WHERE (
    SELECT COUNT(DISTINCT m.curso_codigo)
    FROM matricula m
    JOIN curso c ON m.curso_codigo = c.codigo
    WHERE m.alumno_dni = a.dni AND c.idioma = 'Inglés'
) = (SELECT COUNT(*) FROM curso WHERE idioma = 'Inglés');

-- 9. Cursos sin matrículas
SELECT c.codigo, c.nombre
FROM curso c
WHERE NOT EXISTS (SELECT 1 FROM matricula m WHERE m.curso_codigo = c.codigo);

-- 10. Vista resumen
CREATE OR REPLACE VIEW v_resumen_cursos AS
SELECT c.codigo, c.nombre, c.idioma,
       CONCAT(p.nombre, ' ', p.apellidos) AS profesor,
       COUNT(m.id) AS total_matriculas,
       ROUND(AVG(m.nota), 2) AS nota_media
FROM curso c
JOIN profesor p ON c.profesor_dni = p.dni
LEFT JOIN matricula m ON c.codigo = m.curso_codigo
GROUP BY c.codigo, c.nombre, c.idioma, p.nombre, p.apellidos;

SELECT * FROM v_resumen_cursos ORDER BY nota_media DESC;
