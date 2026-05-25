# Ejercicios — POO y Bases de datos con Java

<img width="1536" height="1024" alt="logo" src="https://github.com/user-attachments/assets/747c3176-0838-4bbe-aaa5-15dfd8da2133" />

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
├── ejercicios-personalizados/  # Índice de ejercicios añadidos desde el menú Ejercicios
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

**Teclado español en Linux:** usa `./viewer/run.sh` para arrancar. El script desactiva `ibus` para esta aplicación (`XMODIFIERS=`), de modo que las tecclas muertas funcionen con una sola pulsación (`´` + `a` → `á`). Si lanzas desde el IDE, define la variable de entorno `XMODIFIERS` vacía en la configuración de ejecución.

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
| **Mi código** | Abre un editor sobre tu carpeta `trabajo/` (resaltado de sintaxis al salir del cuadro de texto; **Comprobar errores** con `javac` en Java y avisos de sintaxis; líneas marcadas en rojo). **Guardar** escribe en `trabajo/`, no en `soluciones/`. |
| **Ejecutar mi código** | Compila y ejecuta los `.java` de `trabajo/` del ejercicio seleccionado (solo ejercicios Java). |
| **Ver solución** | Abre la solución oficial con **Comprobar errores** y resaltado. Puedes editarla y **Guardar** en `soluciones/` (útil para docentes; el alumno debería usar «Mi código»). |
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

**Ejercicios**

- **Añadir ejercicio…** — elige el **enunciado** (`.md`) y la **solución** (carpeta Java, `.sql`, `.md`, etc.) dentro del repositorio. El ejercicio aparece al instante en el índice.
- **Eliminar ejercicio…** — quita cualquier ejercicio del índice. Los **añadidos por ti** se borran de `ejercicios-personalizados/`; los del **curso** solo se **ocultan** (no se borran archivos).
- **Restaurar ejercicios ocultos…** — vuelve a mostrar en el índice un ejercicio del curso que habías ocultado.

Los ejercicios personalizados se guardan en `ejercicios-personalizados/` (un `.properties` por ejercicio). Los ocultos del curso, en `ejercicios-personalizados/ocultos.properties`.

**Ver**

- Alternar **modo claro** / **modo oscuro**.

**Ayuda**

- **Cómo añadir y gestionar ejercicios…** — guía del menú Ejercicios, archivos y catálogo (contenido del README).
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
- **Windows:** `viewer\run.bat` (genera `sources.txt` sin BOM; pausa si falla compilación o arranque).
- **WSL (Linux en Windows):** carpetas y terminales se abren en el **Explorador / Windows Terminal** del host (`explorer.exe`, `wt.exe`, `cmd.exe` vía `wslpath -w`).
- **Linux con escritorio:** gnome-terminal, xdg-open, etc.
- Botones e interfaz adaptados al aspecto del sistema (Nimbus en Linux nativo; LAF del sistema en Windows).

---

## Cómo añadir nuevos ejercicios

Cada ejercicio necesita un **enunciado** (`ENUNCIADO.md`) y, normalmente, una **solución** en el repositorio. Puedes registrarlo en el índice del visor **sin programar** desde el menú **Ejercicios**.

### Forma recomendada: menú Ejercicios (JAVe-Ando)

1. Prepara en disco el **enunciado** (archivo `.md`) y la **solución** (carpeta con `.java`, archivo `.sql`, `SOLUCION.md`, etc.) dentro de este repositorio.
2. En el visor: **Ejercicios → Añadir ejercicio…**
3. Rellena el formulario:
   - **Id** — identificador único (p. ej. `extra-01`, `poo-09`)
   - **Título** — texto que aparece en el índice
   - **Módulo** — grupo del árbol (p. ej. `Programación orientada a objetos`, `Ejercicios añadidos`)
   - **Tipo de solución** — Java, SQL, Markdown o ninguno
   - **Clase principal** — solo para Java (`Main`, `ListarAlumnosDemo`, …)
4. Pulsa **Elegir…** junto a **Enunciado** y selecciona el `.md`.
5. Pulsa **Elegir…** junto a **Solución** y selecciona el archivo o carpeta.
6. Confirma con **Añadir al índice** — el ejercicio aparece al instante en el árbol.

Los datos se guardan en `ejercicios-personalizados/` (un archivo `.properties` por ejercicio). No hace falta recompilar el visor.

**Tipos de solución en el formulario:**

| Tipo | Qué elegir como solución | Ejecutar desde el visor |
|------|--------------------------|-------------------------|
| **Java** | Carpeta con `.java` | Sí (indica la clase con `main`) |
| **SQL** | Archivo `.sql` | No |
| **Markdown** | `SOLUCION.md` u otro `.md` | No |
| **Ninguno** | Dejar sin solución o ruta vacía | No |

El tipo se deduce en parte al elegir la solución (carpeta → Java, `.sql` → SQL, `.md` → Markdown).

### Eliminar o ocultar ejercicios del índice

| Menú | Efecto |
|------|--------|
| **Eliminar ejercicio…** | Quita el ejercicio del índice. Los **añadidos por ti** borran su `.properties`. Los del **curso** solo se **ocultan** (los archivos del repo no se eliminan). |
| **Restaurar ejercicios ocultos…** | Vuelve a mostrar ejercicios del curso que habías ocultado (`ejercicios-personalizados/ocultos.properties`). |

---

### Paso 1: crear el enunciado

1. Crea una carpeta con nombre descriptivo, por ejemplo `modulo-poo/09-interfaces-avanzadas/`.
2. Dentro, añade `ENUNCIADO.md` en Markdown.

Ejemplo de cabecera:

```markdown
# Ejercicio 09 — Interfaces avanzadas

**Área:** Programación orientada a objetos
**Nivel:** Intermedio
```

### Paso 2: crear la solución

Coloca la solución en `soluciones/`, con la misma estructura relativa que el enunciado:

| Tipo | Dónde guardar |
|------|----------------|
| Java (POO o JDBC) | `soluciones/modulo-poo/09-interfaces-avanzadas/` (varios `.java` si hace falta) |
| SQL | `soluciones/modulo-bbdd/06-vistas/vistas.sql` |
| Teoría (E-R, etc.) | `soluciones/modulo-bbdd/…/SOLUCION.md` |

En proyectos Java con varios archivos, el editor del visor usa marcas `// ===== archivo.java =====` al leer o guardar varios `.java` a la vez.

### Ejemplos por categoría

**POO (Java)** — enunciado: `modulo-poo/09-tu-carpeta/ENUNCIADO.md`, solución: `soluciones/modulo-poo/09-tu-carpeta/`, tipo **Java**, clase `Main`.

**Bases de datos (SQL)** — enunciado: `modulo-bbdd/06-vistas/ENUNCIADO.md`, solución: `soluciones/modulo-bbdd/06-vistas/vistas.sql`, tipo **SQL**.

**Teoría (Markdown)** — solución: `SOLUCION.md`, tipo **Markdown**.

**JDBC** — enunciado: `modulo-bbdd/jdbc/04-tu-carpeta/ENUNCIADO.md`, solución: carpeta Java en `soluciones/modulo-bbdd/jdbc/…`, tipo **Java**, clase principal según tu `main` (no tiene que llamarse `Main`).

---

### Forma avanzada: catálogo fijo en el código

Si quieres que el ejercicio forme parte del repositorio para todos los usuarios (sin depender de `ejercicios-personalizados/`), edita `viewer/src/com/ifcd0112/viewer/ExerciseCatalog.java` y añade una línea en el constructor:

```java
add("poo-09", "09 — Interfaces avanzadas", "Programación orientada a objetos",
        mod.resolve("09-interfaces-avanzadas/ENUNCIADO.md"),
        sol.resolve("modulo-poo/09-interfaces-avanzadas"),
        Exercise.SolutionType.JAVA, "Main");
```

Tras modificar el catálogo, **recompila** con `./viewer/run.sh` o `viewer\run.bat`.

---

### Comprobación rápida

1. El ejercicio aparece en el **índice** bajo su módulo.
2. Al seleccionarlo, se muestra el **enunciado**.
3. **Mi código** abre o crea tu versión en `trabajo/`.
4. En Java: **Ejecutar mi código** y **Ejecutar solución** muestran la consola.
5. **Ver solución** abre la solución oficial; **Comprobar errores** analiza el código (Java con `javac`).

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
