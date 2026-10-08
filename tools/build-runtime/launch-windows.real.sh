#!/system/bin/sh
set -eu

RUNTIME="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
EXE="${1:-}"

if [ -z "${EXE}" ]; then
  echo "Usage: launch-windows /path/to/program.exe"
  exit 2
fi

BOX64="${RUNTIME}/bin/box64"
WINE64="${RUNTIME}/wine/usr/local/bin/wine64"

if [ ! -x "${BOX64}" ]; then
  echo "BLASTER: Box64 missing"
  exit 10
fi
if [ ! -x "${WINE64}" ]; then
  echo "BLASTER: Wine64 missing"
  exit 11
fi

export BLASTER_RUNTIME="${RUNTIME}"
export BOX64_LD_LIBRARY_PATH="${RUNTIME}/rootfs/lib/x86_64-linux-gnu:${RUNTIME}/rootfs/usr/lib/x86_64-linux-gnu:${RUNTIME}/rootfs/lib64"
export WINEPREFIX="${RUNTIME}/prefix"
export WINEDLLPATH="${RUNTIME}/wine/usr/local/lib/wine/x86_64-windows:${RUNTIME}/wine/usr/local/lib/wine/i386-windows"
export PATH="${RUNTIME}/wine/usr/local/bin:${PATH:-}"

mkdir -p "${WINEPREFIX}"

exec "${BOX64}" "${WINE64}" "${EXE}" "${@:2}"
