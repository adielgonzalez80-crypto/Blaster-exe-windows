#!/usr/bin/env bash
set -euo pipefail

# Build/package helper for a REAL, separately built Wine + Box64 runtime.
# It intentionally does not download arbitrary binaries.
ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="$ROOT/runtime.zip"
STAGE="$ROOT/stage"
rm -rf "$STAGE" "$OUT"
mkdir -p "$STAGE/bin" "$STAGE/wine/bin"

: "${BOX64_BIN:?Set BOX64_BIN to a tested Android ARM64/Linux Box64 binary}"
: "${WINE64_BIN:?Set WINE64_BIN to a tested Wine64 binary}"
: "${LAUNCHER:?Set LAUNCHER to a tested launch-windows script}"

install -m 0755 "$BOX64_BIN" "$STAGE/bin/box64"
install -m 0755 "$WINE64_BIN" "$STAGE/wine/bin/wine64"
install -m 0755 "$LAUNCHER" "$STAGE/launch-windows"

cat > "$STAGE/runtime.json" <<'JSON'
{
  "runtime": "blaster-windows",
  "version": 1,
  "architecture": "arm64",
  "components": {
    "launcher": "launch-windows",
    "box64": "bin/box64",
    "wine64": "wine/bin/wine64"
  }
}
JSON

(
  cd "$STAGE"
  zip -r "$OUT" .
)
echo "Runtime package: $OUT"
