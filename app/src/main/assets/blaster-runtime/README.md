BLASTER Windows Runtime

Esta carpeta define el contrato del runtime que BLASTER EXE espera encontrar
en filesDir/blaster-runtime/ después de una instalación.

Estructura mínima:
  runtime.json
  launch-windows
  bin/box64
  wine/bin/wine64

Requisitos:
- binarios compilados para Android/Linux ARM64 cuando corresponda;
- Wine/WOW64 y sus dependencias compatibles con el runtime;
- launcher ejecutable que configure WINEPREFIX, PATH y las bibliotecas;
- no se deben descargar binarios arbitrarios durante la compilación de la APK.

El APK base no incluye todavía esos binarios nativos. La aplicación detecta
su ausencia y no intenta ejecutar un EXE sin un runtime válido.

Prueba inicial recomendada:
1. Instalar el runtime en filesDir/blaster-runtime.
2. Verificar que launch-windows, box64 y wine64 sean ejecutables.
3. Seleccionar un EXE Win32/Win64 muy pequeño.
4. Ejecutarlo desde BLASTER y revisar blaster-runtime-last.log.
5. Solo después avanzar a aplicaciones grandes.
