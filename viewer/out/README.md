# Carpeta de compilación

Archivos generados al ejecutar `../run.sh` (Linux/macOS) o `../run.bat` (Windows):

- `sources.txt` — lista de `.java` con **rutas relativas** a `viewer/` (portable)
- `*.class` — bytecode compilado

No edites `sources.txt` a mano; se regenera en cada compilación.

Para compilar manualmente desde `viewer/`:

```bash
javac -encoding UTF-8 -d out @out/sources.txt
java -cp out com.ifcd0112.viewer.Launcher
```

En Windows, `run.bat` regenera `sources.txt` y lanza la app. Sin PowerShell, compila con `src\com\ifcd0112\viewer\*.java`.
Tras la primera compilación: `java -cp out com.ifcd0112.viewer.ViewerBuild` (multiplataforma).
