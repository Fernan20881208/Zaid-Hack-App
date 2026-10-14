# Patch Zaid Hack — Pink Monet (v1.1.0)

Esta carpeta documenta los cambios del rediseño: logo ZH minimalista, icono adaptable Android 12+, paleta tonal rosa y tarjetas Material 3.

- `app/src/main/res/drawable/logo_zh.webp` — logo original optimizado a WebP.
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher*.xml` — icono del lanzador.
- `app/src/main/java/com/zaidhack/pink/ui/theme/ZaidHackTheme.kt` — colores y formas Monet rosa.
- `app/src/main/java/com/zaidhack/pink/MainActivity.kt` — nueva cabecera, estado de licencia y tarjetas Material 3.

Para descargar el ZIP del patch, abre la compilación más reciente en **Actions > Build Android APK > Artifacts**, archivo **Zaid-Hack-Monet-Pink-Patch**.

No hay claves ZH ni ELF privado en este repositorio. Se preservaron las funciones existentes de importar script, validar key, iniciar, detener y ver logs.

Este tema usa una paleta rosa *inspirada* en Monet: no aplica los colores extraídos del wallpaper, porque la identidad visual de la marca debe mantenerse rosa.
