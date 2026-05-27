package com.ifcd0112.viewer;

public final class AppInfo {

    public static final String NAME = "JAVe-Ando";
    public static final String VERSION = "1.1";
    public static final String SUBTITLE = "Ejercicios de POO y bases de datos con Java";
    public static final String GITHUB_URL = "https://github.com/entreunoysceros/jave-ando";

    public static final String ABOUT_TEXT = """
            JAVe-Ando es el visor gráfico del repositorio de ejercicios IFCD0112: \
            programación orientada a objetos, diseño y consultas SQL, y JDBC con MySQL/MariaDB.

            Enunciados en Markdown, índice por módulos y carpeta trabajo/ para practicar \
            sin modificar las soluciones oficiales.

            Editor de código con resaltado de sintaxis, comprobación de errores (javac en Java) \
            y marcas en el margen.

            Ejecución integrada: compilar y ejecutar Java; ejecutar scripts .sql contra la base \
            configurada (conexión JDBC, descarga automática del conector, avisos si un script \
            falla a medias).

            Base de datos: elegir la base activa, consola SQL interactiva (Ctrl+Alt+S) y visor \
            de esquema con diagrama de tablas, claves foráneas y datos al seleccionar una tabla.

            Menú Ejercicios para añadir, ocultar o restaurar ejercicios; tema claro/oscuro; \
            bandeja del sistema (icono img/logo.png) con acceso rápido a las mismas funciones.

            Linux, Windows y macOS — JDK 17+.""";

    private AppInfo() {}
}
