#!/system/bin/sh
set -eu

RUNTIME="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
EXE="${1:-}"

if [ -z "$EXE" ]; then
  echo "BLASTER: selecciona un archivo .exe"
  exit 2
fi
shift

BOX64="$RUNTIME/bin/box64"
WINE64="$RUNTIME/wine/usr/local/bin/wine64"
[ -x "$WINE64" ] || WINE64="$RUNTIME/wine/usr/local/bin/wine"

[ -x "$BOX64" ] || { echo "BLASTER: falta Box64 ARM64 en $BOX64"; exit 10; }
[ -x "$WINE64" ] || { echo "BLASTER: falta Wine64 en $WINE64"; exit 11; }
[ -f "$EXE" ] || { echo "BLASTER: no existe el archivo $EXE"; exit 12; }

mkdir -p "$RUNTIME/home" "$RUNTIME/tmp" "$RUNTIME/prefix"

export BLASTER_RUNTIME="$RUNTIME"
export BOX64_LD_LIBRARY_PATH="$RUNTIME/rootfs/lib/x86_64-linux-gnu:$RUNTIME/rootfs/usr/lib/x86_64-linux-gnu:$RUNTIME/rootfs/lib64:$RUNTIME/rootfs/usr/lib64:$RUNTIME/wine/usr/local/lib"
export BOX64_DYNAREC=1
export BOX64_MMAP32=1
export WINEPREFIX="$RUNTIME/prefix"
export WINEDLLPATH="$RUNTIME/wine/usr/local/lib/wine/x86_64-windows:$RUNTIME/wine/usr/local/lib/wine/i386-windows"
export PATH="$RUNTIME/wine/usr/local/bin:${PATH:-}"
export HOME="$RUNTIME/home"
export TMPDIR="$RUNTIME/tmp"
export WINEARCH=win64

LOG="$RUNTIME/last-launch.log"
{
  echo "BLASTER Windows Runtime"
  echo "Runtime: $RUNTIME"
  echo "EXE: $EXE"
  echo "Fecha: $(date 2>/dev/null || echo desconocida)"
} >> "$LOG"

if [ ! -f "$WINEPREFIX/system.reg" ]; then
  echo "BLASTER: preparando Wine por primera vez..."
  "$BOX64" "$WINE64" wineboot -u >> "$LOG" 2>&1 || {
    echo "BLASTER: Wine no pudo inicializarse. Revisa last-launch.log."
    exit 20
  }
fi

echo "BLASTER: iniciando $EXE"
exec "$BOX64" "$WINE64" "$EXE" "$@" >> "$LOG" 2>&1
