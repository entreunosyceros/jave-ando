#!/usr/bin/env bash
# Lanza JAVe-Ando (rutas relativas: portable entre equipos)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VIEWER="$ROOT/viewer"

cd "$VIEWER"
mkdir -p out

# Rutas relativas a viewer/ (forward slashes; válido también en Windows con javac)
find src -name "*.java" | sort > out/sources.txt

JAVAC="javac"
JAVA_BIN="java"
if [[ -n "${JAVA_HOME:-}" ]]; then
  [[ -x "${JAVA_HOME}/bin/javac" ]] && JAVAC="${JAVA_HOME}/bin/javac"
  [[ -x "${JAVA_HOME}/bin/java" ]] && JAVA_BIN="${JAVA_HOME}/bin/java"
fi
"$JAVAC" -encoding UTF-8 -d out @out/sources.txt

cd "$ROOT"
CLASSPATH="$VIEWER/out"
if compgen -G "$ROOT/lib/"'*.jar' > /dev/null 2>&1; then
  for jar in "$ROOT/lib/"*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
  done
fi
# ibus + Swing: las teclas muertas (´+a) exigen varias pulsaciones; sin esto va directo al teclado X11
if [[ "$(uname -s)" == "Linux" ]]; then
  export XMODIFIERS=
fi
exec "$JAVA_BIN" -cp "$CLASSPATH" com.ifcd0112.viewer.Launcher
