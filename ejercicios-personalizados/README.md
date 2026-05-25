# Ejercicios personalizados (JAVe-Ando)

Aquí se guardan los ejercicios que añadas desde el menú **Ejercicios → Añadir ejercicio…** del visor.

Cada ejercicio es un archivo `.properties` con:

- Ruta al enunciado (`.md`)
- Ruta a la solución (carpeta Java, `.sql`, `.md`, etc.)
- Título, módulo del índice y clase principal (si aplica)

Los archivos de enunciado y solución pueden estar en cualquier carpeta del repositorio; el visor solo guarda las rutas relativas.

Puedes versionar esta carpeta en git si quieres compartir ejercicios extra con el grupo.

## Ejercicios ocultos del curso

Si eliminas del índice un ejercicio que ya venía en el repositorio (POO, BBDD, etc.), su id se guarda en `ocultos.properties`. Restáuralo con **Ejercicios → Restaurar ejercicios ocultos…**.
