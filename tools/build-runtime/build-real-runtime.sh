#!/usr/bin/env bash
set -euo pipefail

# BLASTER EXE - reproducible runtime builder
# Produces a runtime containing real Box64 + Wine WOW64 + a Linux userspace.
# This script is intentionally source-based: no placeholder binaries are accepted.

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT="${ROOT}/dist/blaster-runtime"
SRC="${ROOT}/.runtime-src"
JOBS="${JOBS:-$(nproc)}"

rm -rf "${OUT}" "${SRC}"
mkdir -p "${OUT}/bin" "${OUT}/wine" "${OUT}/rootfs" "${SRC}"

echo "[1/5] Clone Box64"
git clone --depth 1 https://github.com/ptitSeb/box64.git "${SRC}/box64"
cmake -S "${SRC}/box64" -B "${SRC}/box64/build" \
  -DCMAKE_BUILD_TYPE=Release \
  -DARM_DYNAREC=ON \
  -DBOX32=ON \
  -DWOW64=ON \
  -DCMAKE_INSTALL_PREFIX=/usr
cmake --build "${SRC}/box64/build" -j"${JOBS}"
cmake --install "${SRC}/box64/build" --prefix "${OUT}"

echo "[2/5] Build x86_64 Wine WOW64"
git clone --depth 1 https://gitlab.winehq.org/wine/wine.git "${SRC}/wine"
cd "${SRC}/wine"
./configure --enable-win64 --with-xattr
make -j"${JOBS}"
make install DESTDIR="${OUT}/wine"

echo "[3/5] Prepare a glibc x86_64 root filesystem"
# The rootfs is deliberately produced by the CI image, not copied from the
# Android host. It contains the Linux loader and libraries Wine expects.
mkdir -p "${OUT}/rootfs"
apt-get update
apt-get install -y --no-install-recommends debootstrap ca-certificates
debootstrap --arch=amd64 --variant=minbase bookworm "${OUT}/rootfs" http://deb.debian.org/debian

echo "[4/5] Add launcher"
install -m 0755 "${ROOT}/tools/build-runtime/launch-windows.real.sh" "${OUT}/launch-windows"

cat > "${OUT}/runtime.json" <<EOF
{
  "name": "BLASTER Windows Runtime",
  "format": 1,
  "architecture": "arm64-host/x86_64-guest",
  "engine": "Box64 + Wine WOW64",
  "rootfs": "Debian Bookworm amd64",
  "source": {
    "box64": "https://github.com/ptitSeb/box64",
    "wine": "https://gitlab.winehq.org/wine/wine"
  }
}
EOF

echo "[5/5] Validate"
test -x "${OUT}/bin/box64"
test -x "${OUT}/launch-windows"
test -f "${OUT}/runtime.json"
find "${OUT}" -type f -print | sort > "${OUT}/MANIFEST.txt"

echo "Runtime created at: ${OUT}"
