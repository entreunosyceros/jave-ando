# Conector JDBC

Descarga el driver e colócalo en esta carpeta:

- **MySQL**: [Connector/J](https://dev.mysql.com/downloads/connector/j/) → `mysql-connector-j-8.x.x.jar`
- **MariaDB**: [MariaDB Java Client](https://mariadb.com/kb/en/mariadb-connector-j/) → `mariadb-java-client-3.x.x.jar`

Compilar y ejecutar:

```bash
javac -cp ".:lib/mysql-connector-j-8.3.0.jar" *.java
java  -cp ".:lib/mysql-connector-j-8.3.0.jar" ListarAlumnos
```

En Windows, usa `;` en lugar de `:` en el classpath.
