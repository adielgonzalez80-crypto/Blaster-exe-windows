#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT="${ROOT}/dist/blaster-runtime"
SRC="${ROOT}/.runtime-src"
JOBS="${JOBS:-$(nproc)}"
NDK="${ANDROID_NDK_HOME:-}"
ANDROID_API="${ANDROID_API:-28}"
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
  -DTERMUX=OFF \
  -DARM_DYNAREC=ON \
  -DBOX32=OFF \
  -DWOW64=OFF \
  -DCMAKE_C_FLAGS=-D_GNU_SOURCE \
  -DCMAKE_TOOLCHAIN_FILE="${NDK}/build/cmake/android.toolchain.cmake" \
  -DANDROID_ABI="${ABIs}" \
  -DANDROID_PLATFORM="android-${ANDROID_API}" \
  -DCMAKE_INSTALL_PREFIX=/usr

cmake --build "${SRC}/box64/build" -j"${JOBS}"

# Avoid CMake's full install target: it also writes host integration files
# under /etc/binfmt.d, which is forbidden on GitHub runners and irrelevant
# inside an Android app. Stage only the Android executable.
BOX64_BIN="${SRC}/box64/build/box64"
if [ ! -f "${BOX64_BIN}" ]; then
  BOX64_BIN="$(find "${SRC}/box64/build" -type f -name box64 -perm -u+x -print -quit)"
fi
if [ -z "${BOX64_BIN}" ] || [ ! -f "${BOX64_BIN}" ]; then
  echo "ERROR: Box64 compiló, pero no se encontró el ejecutable."
  find "${SRC}/box64/build" -maxdepth 4 -type f -name 'box64*' -print || true
  exit 21
fi
install -m 0755 "${BOX64_BIN}" "${OUT}/bin/box64"

NDK_CXX="${NDK}/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/lib/aarch64-linux-android/libc++_shared.so"
if [ -f "${NDK_CXX}" ]; then
  mkdir -p "${OUT}/lib"
  install -m 0644 "${NDK_CXX}" "${OUT}/lib/libc++_shared.so"
fi

if [ ! -x "${OUT}/bin/box64" ]; then
  echo "ERROR: Box64 ARM64 no fue generado."
  find "${OUT}" -maxdepth 3 -type f -print || true
  exit 21
fi

echo "[2/5] Compilar Wine x86_64 dentro de Debian Bookworm"
git clone --depth 1 https://gitlab.winehq.org/wine/wine.git "${SRC}/wine"

# Build Wine against the same Debian Bookworm glibc family shipped in rootfs.
# This avoids compiling against Ubuntu's newer glibc and then packaging older
# Bookworm libraries that may not satisfy Wine's ELF symbol requirements.
docker run --rm \
  -v "${SRC}/wine:/src" \
  -v "${OUT}/wine:/out" \
  -e "JOBS=${JOBS}" \
  -w /src \
  debian:bookworm-slim bash -lc '
    set -e
    printf "deb-src http://deb.debian.org/debian bookworm main\n" >> /etc/apt/sources.list
    printf "deb-src http://deb.debian.org/debian bookworm-updates main\n" >> /etc/apt/sources.list
    printf "deb-src http://security.debian.org/debian-security bookworm-security main\n" >> /etc/apt/sources.list
    apt-get update
    apt-get install -y --no-install-recommends build-essential gcc-mingw-w64-x86-64 gcc-mingw-w64-i686 flex bison gettext perl python3 pkg-config ca-certificates
    apt-get build-dep -y wine
    ./configure --enable-win64 --with-xattr --prefix=/usr/local
    make -j"${JOBS}"
    make install DESTDIR=/out
    if [ ! -x /out/usr/local/bin/wine64 ] && [ -x /out/usr/local/bin/wine ]; then ln -s wine /out/usr/local/bin/wine64; fi
    rm -rf /var/lib/apt/lists/*
  '

if [ ! -x "${OUT}/wine/usr/local/bin/wine" ] && [ ! -x "${OUT}/wine/usr/local/bin/wine64" ]; then
  echo "ERROR: No se encontró el lanzador Wine en el entorno Debian Bookworm."
  find "${OUT}/wine/usr/local/bin" -maxdepth 1 -type f -o -type l || true
  exit 22
fi
# wine64 alias is created inside the build container to avoid root-owned output permission errors.

echo "[3/5] Crear rootfs amd64 Debian Bookworm para el guest"
cd "${ROOT}"
sudo apt-get update
sudo apt-get install -y --no-install-recommends debootstrap ca-certificates
sudo debootstrap --arch=amd64 --variant=minbase bookworm "${OUT}/rootfs" http://deb.debian.org/debian

# Wine's Unix-side components need their runtime shared libraries inside the
# guest rootfs. A minbase-only rootfs is not enough to start Wine.
sudo chroot "${OUT}/rootfs" apt-get update
sudo chroot "${OUT}/rootfs" apt-get install -y --no-install-recommends \
  ca-certificates libgcc-s1 libstdc++6 libx11-6 libxext6 libxrender1 \
  libxrandr2 libxinerama1 libxcursor1 libxi6 libxfixes3 libxcomposite1 \
  libxdamage1 libxkbcommon0 libfreetype6 libfontconfig1 libasound2 \
  libdbus-1-3 libgnutls30 libldap-2.5-0 libpulse0 libudev1 \
  libwayland-client0 libgl1 libvulkan1 libopengl0 libxshmfence1 \
  libxxf86vm1 libxss1 libxmu6 libxt6 libsm6 libice6 \
  libgstreamer1.0-0 libgstreamer-plugins-base1.0-0
sudo chroot "${OUT}/rootfs" apt-get clean
sudo rm -rf "${OUT}/rootfs/var/lib/apt/lists/"*

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

sudo find "${OUT}" -type f -print | sort > "${OUT}/MANIFEST.txt"
sudo du -sh "${OUT}"
echo "Runtime creado: ${OUT}"
