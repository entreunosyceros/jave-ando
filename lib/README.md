# Conector JDBC

Coloca **un** archivo `.jar` en esta carpeta, o deja la carpeta vacía: **JAVe-Ando descargará el conector** la primera vez que pruebes la conexión SQL o ejecutes un script (MySQL Connector/J u MariaDB Client según el driver configurado).

## Descarga manual (MySQL Connector/J 8.3)

**Linux / macOS:**

```bash
wget -O lib/mysql-connector-j-8.3.0.jar \
  https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar
```

**Windows (PowerShell), desde la raíz del repo:**

```powershell
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar" -OutFile "lib\mysql-connector-j-8.3.0.jar"
```

## Alternativas

- **MySQL**: [Connector/J](https://dev.mysql.com/downloads/connector/j/) → `mysql-connector-j-8.x.x.jar`
- **MariaDB**: [MariaDB Java Client](https://mariadb.com/kb/en/mariadb-connector-j/) → `mariadb-java-client-3.x.x.jar`

Compilar y ejecutar a mano:

```bash
javac -cp ".:lib/mysql-connector-j-8.3.0.jar" *.java
java  -cp ".:lib/mysql-connector-j-8.3.0.jar" ListarAlumnos
```

En Windows, usa `;` en lugar de `:` en el classpath.
