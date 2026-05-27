# Ejercicio JDBC 01 — Conexión básica

**Área:** Acceso a datos con JDBC
**Nivel:** Básico  
**Conceptos:** DriverManager, Connection, Statement, ResultSet

## Enunciado

Crea una aplicación Java que se conecte a la base de datos `academia_idiomas` y liste los alumnos.

### Configuración

1. Copia `config/database.properties.example` → `config/database.properties`.
2. Edita usuario, contraseña y URL JDBC.
3. Descarga el conector MySQL/MariaDB en `lib/`.

### Clase `ConexionBD` (utilidad)

Métodos estáticos:

- `getConexion()`: lee `database.properties` y devuelve un `Connection`.
- `cerrarConexion(Connection conn)`: cierra la conexión de forma segura.

### Clase `ListarAlumnos`

En `main`:

1. Obtener conexión.
2. Ejecutar `SELECT dni, nombre, apellidos, email FROM alumno ORDER BY apellidos`.
3. Recorrer el `ResultSet` e imprimir cada fila.
4. Cerrar `ResultSet`, `Statement` y `Connection` en bloque `finally` o try-with-resources.

### Salida esperada

```
=== Alumnos de la academia ===
12345678A | Ana García López | ana.garcia@email.com
87654321B | Luis Martín Ruiz | luis.martin@email.com
...
Conexión cerrada correctamente.
```

## Estructura de archivos sugerida

```
01-conexion-basica/
├── config/
│   └── database.properties
├── ConexionBD.java
└── ListarAlumnos.java
```

## Criterios de corrección

- [ ] Conexión mediante `DriverManager` y properties externo
- [ ] try-with-resources o finally para cerrar recursos
- [ ] Consulta SELECT ejecutada y ResultSet recorrido
- [ ] Mensaje de error claro si falla la conexión
