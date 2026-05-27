-- Solución orientativa — Manipulación DML (ejercicio 04)
USE academia_idiomas;

-- 1. Matricular alumno
INSERT INTO matricula (alumno_dni, curso_codigo, fecha_inscripcion)
VALUES ('12345678A', 'ING-B1', CURRENT_DATE)
ON DUPLICATE KEY UPDATE fecha_inscripcion = VALUES(fecha_inscripcion);

-- 2. Nuevo curso alemán
INSERT INTO curso (codigo, nombre, idioma, nivel, plazas, profesor_dni)
VALUES ('ALM-A2', 'Alemán Básico', 'Alemán', 'A2', 20, '33333333C');

-- 3. Subir plazas A1/A2 un 10%
UPDATE curso SET plazas = plazas + ROUND(plazas * 0.10) WHERE nivel IN ('A1', 'A2');

-- 4. Nota 5.0 a matrículas antiguas sin nota
UPDATE matricula
SET nota = 5.0
WHERE nota IS NULL
  AND fecha_inscripcion < DATE_SUB(CURRENT_DATE, INTERVAL 6 MONTH);

-- 5. Cambiar email
UPDATE alumno SET email = 'nuevo.email@academia.es' WHERE dni = '12345678A';

-- 8. Transferencia de matrícula en transacción
START TRANSACTION;
DELETE FROM matricula WHERE alumno_dni = '12345678A' AND curso_codigo = 'ING-B1';
INSERT INTO matricula (alumno_dni, curso_codigo, fecha_inscripcion)
VALUES ('12345678A', 'ING-B2', CURRENT_DATE);
-- ROLLBACK; -- descomentar para probar rollback
COMMIT;
