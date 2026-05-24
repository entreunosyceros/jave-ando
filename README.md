# Ejercicios — POO y Bases de datos con Java

Colección de ejercicios básicos para practicar la  
*programación con lenguajes orientados a objetos y bases de datos relacionales*.

Incluye el visor gráfico **JAVe-Ando** (Swing) para leer enunciados, practicar con tu propio código, consultar soluciones y ejecutar programas Java desde una misma ventana. Funciona en **Linux, Windows y macOS**.

## Estructura del repositorio

```
.
├── modulo-poo/          # Enunciados de programación orientada a objetos
├── modulo-bbdd/         # Enunciados de SQL, diseño ER, JDBC y proyecto integrador
├── soluciones/          # Soluciones oficiales del repositorio (consultar tras intentar)
│   ├── modulo-poo/
│   └── modulo-bbdd/
├── trabajo/             # Tu código personal (JAVe-Ando — botón «Mi código»)
│   ├── modulo-poo/      # Misma estructura que soluciones/
│   └── modulo-bbdd/
├── sql/                 # Scripts de creación de bases de datos
├── lib/                 # JAR del conector JDBC (opcional)
├── img/                 # Logo y recursos del visor
└── viewer/              # Código fuente de JAVe-Ando
    ├── run.sh           # Arranque en Linux / macOS
    └── run.bat          # Arranque en Windows
```

La carpeta `trabajo/` no sustituye a `soluciones/`: es donde el alumno guarda sus intentos sin modificar la solución publicada (ver `trabajo/README.md`).

## Categorías (módulos del índice)

En JAVe-Ando los ejercicios se agrupan en el árbol por el campo **módulo** del catálogo:

| Módulo en el visor | Carpeta de enunciados | Tipo habitual | Prefijo de id |
|--------------------|------------------------|---------------|---------------|
| Programación orientada a objetos | `modulo-poo/` | Java (`JAVA`) | `poo-` |
| Bases de datos | `modulo-bbdd/` (raíz) | SQL o teoría (`SQL`, `MARKDOWN`) | `bbdd-` |
| Acceso a datos con JDBC | `modulo-bbdd/jdbc/` | Java con BD (`JAVA`) | `jdbc-` |
| Proyecto integrador | `modulo-bbdd/proyecto-integrador/` | Sin solución publicada (`NONE`) | `proj-` |

El **prefijo del id** (`poo`, `bbdd`, `jdbc`, `proj`) determina el color del icono en el visor.

## Requisitos

- **Java JDK 17** o superior (`java -version`, `javac -version`)
- **MySQL 8** o **MariaDB 10** (para ejercicios de SQL y JDBC)
- Conector JDBC: [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) o MariaDB Java Client

### Compilar y ejecutar (ejemplo)

```bash
cd modulo-poo/01-clases-y-objetos
javac *.java
java Main
```

### Ejercicios JDBC

1. Importa el script `sql/01_crear_base_datos.sql` en tu servidor MySQL/MariaDB.
2. Copia `modulo-bbdd/jdbc/config/database.properties.example` → `database.properties`.
3. Edita `database.properties` con tus credenciales.
4. Añade el JAR del conector al classpath:

```bash
javac -cp ".:lib/mysql-connector-j-8.3.0.jar" *.java
java  -cp ".:lib/mysql-connector-j-8.3.0.jar" Main
```

En Windows sustituye `:` por `;` en el classpath.

## JAVe-Ando — Visor gráfico de ejercicios

### Arranque

| Sistema | Comando |
|---------|---------|
| Linux / macOS | `./viewer/run.sh` |
| Windows (CMD) | `viewer\run.bat` |

**Requisitos:** JDK 17+ en `PATH` o variable `JAVA_HOME` configurada (`%JAVA_HOME%\bin` en PATH en Windows).

Tras modificar el código del visor, vuelve a ejecutar el script de arranque. En Windows, si PowerShell no está disponible, `run.bat` compila el paquete directamente.

Recompilación manual desde `viewer/` (cualquier sistema):

```bash
java -cp out com.ifcd0112.viewer.ViewerBuild
```

### Interfaz principal

- **Índice en árbol** agrupado por módulo (POO, BBDD, JDBC, proyecto).
- **Enunciado** con formato Markdown (títulos, listas, tablas, código).
- **Consola de salida** al ejecutar programas Java.
- **Tema claro / oscuro** — menú Ver.
- La **raíz del repositorio** se detecta automáticamente (también al lanzar desde el IDE).

### Panel de acciones (por ejercicio)

| Botón | Función |
|-------|---------|
| **Mi código** | Abre un editor con resaltado de sintaxis sobre tu carpeta `trabajo/`. La primera vez puedes copiar la solución como punto de partida o crear un esqueleto `Main.java` (solo Java). **Guardar** escribe en `trabajo/`, no en `soluciones/`. |
| **Ejecutar mi código** | Compila y ejecuta los `.java` de `trabajo/` del ejercicio seleccionado (solo ejercicios Java). |
| **Ver solución** | Abre la solución oficial. Puedes editarla y **Guardar** en `soluciones/` (útil para docentes; el alumno debería usar «Mi código»). |
| **Ejecutar solución** | Compila y ejecuta la versión oficial en `soluciones/`. |

En ejercicios **SQL** o **teoría** (Markdown), «Mi código» permite editar tu `.sql` o `.md` en `trabajo/`; la ejecución SQL sigue haciéndose en MySQL/MariaDB.

### Menús

**Archivo**

- Cerrar la aplicación.

**Herramientas**

- Abrir carpeta del **enunciado** (explorador de archivos).
- Abrir carpeta **«mi código»** (`trabajo/…`, se crea si no existe).
- Abrir **terminal** en la carpeta del enunciado o en `trabajo/`.
  - Linux: gnome-terminal, konsole, xterm, etc.
  - Windows: Windows Terminal (`wt`), PowerShell o CMD.
  - macOS: Terminal.

**Ver**

- Alternar **modo claro** / **modo oscuro**.

**Ayuda**

- **Cómo añadir ejercicios…** — guía integrada (contenido del README).
- **Acerca de** — información de JAVe-Ando y enlace al repositorio.

### Carpeta `trabajo/` (mi código)

- Estructura paralela a `soluciones/`, por ejemplo:
  - `trabajo/modulo-poo/01-clases-y-objetos/Main.java`
  - `trabajo/modulo-bbdd/02-ddl/crear_academia.sql`
- Por defecto está en `.gitignore` (solo se versiona `trabajo/README.md`).
- En proyectos Java con varios archivos, el editor muestra bloques con marcas  
  `// ===== archivo.java =====` al guardar o leer varios `.java` a la vez.

### Editor de código (solución y mi código)

- Resaltado de sintaxis para **Java**, **SQL** y texto.
- **Guardar**, **Restaurar** (recarga desde disco), **Copiar**.
- Deshacer / rehacer con Ctrl+Z / Ctrl+Y (Cmd en macOS).
- Aviso al cerrar si hay cambios sin guardar.

### Compatibilidad multiplataforma

- Rutas con `java.nio.file` (válidas en Windows y Linux).
- Detección de `JAVA_HOME` y mensajes de error si falta el JDK.
- Botones e interfaz adaptados al aspecto del sistema (incl. GTK en Linux).
- Apertura de carpetas y terminales con fallbacks según el sistema operativo.

---

## Cómo añadir nuevos ejercicios

Cada ejercicio necesita **archivos en disco** y una **entrada en el catálogo** del visor. Sigue los pasos según la categoría.

### Paso común: enunciado

1. Crea una carpeta con nombre descriptivo y prefijo numérico, por ejemplo `09-interfaces-avanzadas`.
2. Dentro, añade `ENUNCIADO.md` en Markdown (títulos `#`, listas, tablas, bloques de código con ` ``` `).

Ejemplo de cabecera:

```markdown
# Ejercicio 09 — Interfaces avanzadas

**Área:** Programación orientada a objetos
**Nivel:** Intermedio
```

### Paso común: solución

Coloca la solución en `soluciones/`, manteniendo la misma estructura relativa que el enunciado:

| Tipo | Dónde guardar la solución |
|------|---------------------------|
| Java (POO o JDBC) | Carpeta con `.java` (p. ej. `soluciones/modulo-poo/09-interfaces-avanzadas/`) |
| SQL | Un archivo `.sql` (p. ej. `soluciones/modulo-bbdd/06-vistas/vistas.sql`) |
| Teoría (diseño ER, etc.) | `SOLUCION.md` en la carpeta correspondiente bajo `soluciones/modulo-bbdd/` |
| Proyecto sin solución publicada | No hace falta archivo; usa ruta vacía en el catálogo |

### Paso común: registrar en el visor

Edita `viewer/src/com/ifcd0112/viewer/ExerciseCatalog.java` y añade una línea `add(...)` en el constructor, **en el bloque de la categoría** que corresponda:

```java
add(
    "poo-09",                              // id único (prefijo de categoría + número)
    "09 — Interfaces avanzadas",           // título en el índice
    "Programación orientada a objetos",    // módulo (agrupa el árbol)
    mod.resolve("09-interfaces-avanzadas/ENUNCIADO.md"),   // enunciado
    sol.resolve("modulo-poo/09-interfaces-avanzadas"),     // solución
    Exercise.SolutionType.JAVA,            // tipo (ver tabla abajo)
    "Main"                                 // clase con main (solo JAVA); null si no aplica
);
```

**Tipos de solución** (`Exercise.SolutionType`):

| Tipo | Uso | `mainClass` | Botón ejecutar |
|------|-----|-------------|----------------|
| `JAVA` | Código Java compilable | Nombre de la clase con `main` (`Main`, `ListarAlumnosDemo`, …) | Sí |
| `SQL` | Archivo `.sql` | `null` | No (solo ver código) |
| `MARKDOWN` | Respuesta en Markdown (`SOLUCION.md`) | `null` | No |
| `NONE` | Enunciado sin solución en el visor | `null` | No |

Tras modificar el catálogo, **recompila el visor** con `./viewer/run.sh`, `viewer\run.bat` o, desde `viewer/`:

```bash
java -cp out com.ifcd0112.viewer.ViewerBuild
```

---

### Añadir ejercicio de POO

1. Carpeta del enunciado: `modulo-poo/NN-nombre-corto/ENUNCIADO.md`
2. Solución: `soluciones/modulo-poo/NN-nombre-corto/*.java`
3. En `ExerciseCatalog.java`:

```java
add("poo-09", "09 — Tu título", "Programación orientada a objetos",
        mod.resolve("09-tu-carpeta/ENUNCIADO.md"),
        sol.resolve("modulo-poo/09-tu-carpeta"),
        Exercise.SolutionType.JAVA, "Main");
```

- Usa id `poo-NN` (número correlativo).
- La clase indicada en el último parámetro debe tener `public static void main`.

---

### Añadir ejercicio de Bases de datos (SQL o teoría)

1. Carpeta del enunciado: `modulo-bbdd/NN-nombre/ENUNCIADO.md`
2. Solución:
   - **SQL:** `soluciones/modulo-bbdd/NN-nombre/archivo.sql`
   - **Teoría / E-R:** `soluciones/modulo-bbdd/NN-nombre/SOLUCION.md`

**Ejemplo SQL:**

```java
add("bbdd-06", "06 — Vistas", "Bases de datos",
        bbdd.resolve("06-vistas/ENUNCIADO.md"),
        sol.resolve("modulo-bbdd/06-vistas/vistas.sql"),
        Exercise.SolutionType.SQL, null);
```

**Ejemplo teoría (Markdown):**

```java
add("bbdd-06", "06 — Normalización", "Bases de datos",
        bbdd.resolve("06-normalizacion/ENUNCIADO.md"),
        sol.resolve("modulo-bbdd/06-normalizacion/SOLUCION.md"),
        Exercise.SolutionType.MARKDOWN, null);
```

- Usa id `bbdd-NN`.

---

### Añadir ejercicio JDBC

1. Carpeta del enunciado: `modulo-bbdd/jdbc/NN-nombre/ENUNCIADO.md`
2. Solución: `soluciones/modulo-bbdd/jdbc/NN-nombre/` (varios `.java` si hace falta)
3. En `ExerciseCatalog.java`:

```java
add("jdbc-04", "04 — Tu ejercicio JDBC", "Acceso a datos con JDBC",
        bbdd.resolve("jdbc/04-tu-carpeta/ENUNCIADO.md"),
        sol.resolve("modulo-bbdd/jdbc/04-tu-carpeta"),
        Exercise.SolutionType.JAVA, "Main");
```

- Usa id `jdbc-NN`.
- Si la clase principal no se llama `Main`, indica el nombre real (como en `ListarAlumnosDemo` del ejercicio 01).
- Recuerda configurar `database.properties` y el JAR en `lib/` para ejecutar desde el visor.

---

### Añadir proyecto integrador (u otra categoría nueva)

1. Enunciado en `modulo-bbdd/proyecto-integrador/ENUNCIADO.md` (o nueva carpeta).
2. Sin solución en el visor:

```java
add("proj-02", "Proyecto — Otro tema", "Proyecto integrador",
        bbdd.resolve("otro-proyecto/ENUNCIADO.md"),
        Path.of(""),
        Exercise.SolutionType.NONE, null);
```

Para una **categoría nueva** en el árbol, usa un texto de módulo distinto en el tercer parámetro de `add` (por ejemplo `"Prácticas extra"`). Todos los ejercicios con el mismo módulo aparecerán agrupados bajo ese nombre.

---

### Comprobación rápida

1. `./viewer/run.sh` o `viewer\run.bat` — el ejercicio aparece en el índice bajo su módulo.
2. Al seleccionarlo, se muestra el enunciado.
3. **Mi código** crea o abre `trabajo/…` y permite guardar tu versión.
4. En ejercicios `JAVA`, **Ejecutar mi código** y **Ejecutar solución** muestran la salida en la consola.
5. **Ver solución** abre la solución oficial.
6. **Herramientas → Abrir carpeta «mi código»** abre el directorio correcto.

Opcional: añade el enlace en la sección [Orden recomendado](#orden-recomendado) de este README.

---

## Orden recomendado

### Bloque 1 — POO

1. [01 — Clases y objetos](modulo-poo/01-clases-y-objetos/ENUNCIADO.md)
2. [02 — Encapsulación](modulo-poo/02-encapsulacion/ENUNCIADO.md)
3. [03 — Constructores y métodos estáticos](modulo-poo/03-constructores/ENUNCIADO.md)
4. [04 — Herencia](modulo-poo/04-herencia/ENUNCIADO.md)
5. [05 — Polimorfismo](modulo-poo/05-polimorfismo/ENUNCIADO.md)
6. [06 — Clases abstractas e interfaces](modulo-poo/06-abstractas-interfaces/ENUNCIADO.md)
7. [07 — Colecciones](modulo-poo/07-colecciones/ENUNCIADO.md)
8. [08 — Excepciones](modulo-poo/08-excepciones/ENUNCIADO.md)

### Bloque 2 — Bases de datos

9. [01 — Diseño entidad-relación](modulo-bbdd/01-diseno-er/ENUNCIADO.md)
10. [02 — DDL: crear tablas](modulo-bbdd/02-ddl/ENUNCIADO.md)
11. [03 — Consultas SELECT](modulo-bbdd/03-consultas-select/ENUNCIADO.md)
12. [04 — INSERT, UPDATE, DELETE](modulo-bbdd/04-manipulacion-datos/ENUNCIADO.md)
13. [05 — JOINs y subconsultas](modulo-bbdd/05-joins/ENUNCIADO.md)

### Bloque 3 — JDBC

14. [01 — Conexión básica](modulo-bbdd/jdbc/01-conexion-basica/ENUNCIADO.md)
15. [02 — CRUD con PreparedStatement](modulo-bbdd/jdbc/02-crud/ENUNCIADO.md)
16. [03 — Transacciones](modulo-bbdd/jdbc/03-transacciones/ENUNCIADO.md)
17. [Proyecto integrador — Gestión de biblioteca](modulo-bbdd/proyecto-integrador/ENUNCIADO.md)

## Licencia

Material educativo de uso libre.
