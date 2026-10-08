# BLASTER Windows Runtime Builder

Este directorio documenta la construcción del paquete que BLASTER instala desde
**Instalar paquete del motor Windows**.

## Paquete esperado

runtime.zip
  launch-windows
  bin/box64
  wine/bin/wine64
  wine/... (dependencias de Wine)
  runtime.json

## Regla

No descargues ni incluyas binarios de Wine/Box64 de procedencia desconocida.
El runtime debe construirse desde fuentes/licencias compatibles y probarse en
un dispositivo Android ARM64.

## Launcher

launch-windows debe:
1. recibir la ruta absoluta del EXE como primer argumento;
2. establecer BLASTER_RUNTIME;
3. establecer WINEPREFIX dentro del runtime o almacenamiento de BLASTER;
4. preparar PATH y LD_LIBRARY_PATH;
5. invocar Box64/Wine con el EXE;
6. devolver el código de salida y escribir diagnóstico.

La APK ya valida el paquete ZIP, rechaza rutas inseguras y exige launcher + Box64
+ Wine64 antes de activarlo.
