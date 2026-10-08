#!/system/bin/sh
# BLASTER Windows runtime launcher template.
# This is a template only: it must be paired with a tested Wine/Box64 build.
set -eu

RUNTIME_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
EXE="$1"

if [ -z "${EXE:-}" ] || [ ! -f "$EXE" ]; then
  echo "BLASTER: EXE no encontrado: $EXE"
  exit 2
fi

export BLASTER_RUNTIME="$RUNTIME_DIR"
export PATH="$RUNTIME_DIR/bin:$RUNTIME_DIR/wine/bin:$PATH"
export LD_LIBRARY_PATH="$RUNTIME_DIR/lib:$RUNTIME_DIR/bin:$RUNTIME_DIR/wine/lib:${LD_LIBRARY_PATH:-}"
export WINEPREFIX="${BLASTER_WINEPREFIX:-$RUNTIME_DIR/prefix}"

BOX64="$RUNTIME_DIR/bin/box64"
WINE="$RUNTIME_DIR/wine/bin/wine64"

[ -x "$BOX64" ] || { echo "BLASTER: Box64 no encontrado o no ejecutable"; exit 3; }
[ -x "$WINE" ] || { echo "BLASTER: Wine64 no encontrado o no ejecutable"; exit 4; }

mkdir -p "$WINEPREFIX"
exec "$BOX64" "$WINE" "$EXE"
