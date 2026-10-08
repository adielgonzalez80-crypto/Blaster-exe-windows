#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT="${ROOT}/dist/blaster-runtime"
SRC="${ROOT}/.runtime-src"
JOBS="${JOBS:-$(nproc)}"
NDK="${ANDROID_NDK_HOME:-}"
ANDROID_API="${ANDROID_API:-26}"
ABIs="arm64-v8a"

rm -rf "${OUT}" "${SRC}"
mkdir -p "${OUT}/bin" "${OUT}/wine" "${OUT}/rootfs" "${SRC}"

if [ -z "${NDK}" ] || [ ! -d "${NDK}" ]; then
  echo "ERROR: ANDROID_NDK_HOME no está configurado."
  exit 20
fi

echo "[1/5] Clonar y compilar Box64 para Android ARM64"
git clone --depth 1 https://github.com/ptitSeb/box64.git "${SRC}/box64"

# The optional Steam helper uses glob()/globfree(); make the declaration explicit
# for the Android NDK toolchain before configuring and building.
if [ -f "${SRC}/box64/src/steam.c" ] && ! grep -Eq '^#include[[:space:]]*[<"]glob\.h[>"]' "${SRC}/box64/src/steam.c"; then
  sed -i '1i#include <glob.h>' "${SRC}/box64/src/steam.c"
fi
echo "Box64 source prepared; glob declaration:"
grep -n 'glob\.h' "${SRC}/box64/src/steam.c" || true

cmake -S "${SRC}/box64" -B "${SRC}/box64/build" \
  -DCMAKE_BUILD_TYPE=Release \
  -DANDROID=ON \
  -DTERMUX=ON \
  -DARM_DYNAREC=ON \
  -DBOX32=ON \
  -DWOW64=ON \
  -DCMAKE_TOOLCHAIN_FILE="${NDK}/build/cmake/android.toolchain.cmake" \
  -DANDROID_ABI="${ABIs}" \
  -DANDROID_PLATFORM="android-${ANDROID_API}" \
  -DCMAKE_INSTALL_PREFIX=/usr

cmake --build "${SRC}/box64/build" -j"${JOBS}"
cmake --install "${SRC}/box64/build" --prefix "${OUT}"

if [ ! -x "${OUT}/bin/box64" ]; then
  echo "ERROR: Box64 ARM64 no fue generado."
  find "${OUT}" -maxdepth 3 -type f -print || true
  exit 21
fi

echo "[2/5] Compilar Wine x86_64 WOW64 como guest"
git clone --depth 1 https://gitlab.winehq.org/wine/wine.git "${SRC}/wine"
cd "${SRC}/wine"
./configure --enable-win64 --with-xattr
make -j"${JOBS}"
make install DESTDIR="${OUT}/wine"

if [ ! -x "${OUT}/wine/usr/local/bin/wine64" ]; then
  echo "ERROR: Wine64 WOW64 no fue generado."
  exit 22
fi

echo "[3/5] Crear rootfs amd64 mínimo para el guest"
cd "${ROOT}"
sudo apt-get update
sudo apt-get install -y --no-install-recommends debootstrap ca-certificates
sudo debootstrap --arch=amd64 --variant=minbase bookworm "${OUT}/rootfs" http://deb.debian.org/debian

echo "[4/5] Instalar launcher"
install -m 0755 "${ROOT}/tools/build-runtime/launch-windows.real.sh" "${OUT}/launch-windows"

cat > "${OUT}/runtime.json" <<EOF
{
  "name": "BLASTER Windows Runtime",
  "format": 2,
  "architecture": "arm64-host/x86_64-guest",
  "engine": "Box64 Android ARM64 + Wine x86_64 WOW64",
  "rootfs": "Debian Bookworm amd64",
  "android": {
    "minSdk": ${ANDROID_API},
    "abi": "arm64-v8a"
  },
  "source": {
    "box64": "https://github.com/ptitSeb/box64",
    "wine": "https://gitlab.winehq.org/wine/wine"
  }
}
EOF

echo "[5/5] Validación"
test -x "${OUT}/bin/box64"
test -x "${OUT}/wine/usr/local/bin/wine64"
test -x "${OUT}/launch-windows"
test -f "${OUT}/runtime.json"

find "${OUT}" -type f -print | sort > "${OUT}/MANIFEST.txt"
du -sh "${OUT}"
echo "Runtime creado: ${OUT}"
