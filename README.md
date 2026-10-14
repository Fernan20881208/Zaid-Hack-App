# Zaid Hack — Pink Edition (APK)

Aplicación Android nativa Kotlin + Jetpack Compose / Material 3 con tema rosa.

## Compatibilidad

- Android **12+** (`minSdk 31`) y dispositivo **ARM64** (`arm64-v8a`).
- **Root** mediante KernelSU, Magisk o un administrador de `su` compatible.
- Paquete de juego heredado del script: `com.dts.freefireth`.
- El repositorio es **público** y **no contiene el ELF** del overlay; no publique claves personales ni el ejecutable privado.

## Instalación

1. Descargar el APK de la pestaña **Actions** → **Build Android APK** → **Artifacts** (artefacto `Zaid-Hack-Pink-Android12-ARM64-debug`).
2. Instalar la APK y conceder acceso root.
3. Descargar el archivo propio `Zaid-Hack-v1.3.sh` y usar **Importar archivo .sh** dentro de la aplicación. La app extrae solamente el payload ELF64 AArch64 ubicado después de `__PAYLOAD__`.
4. Escribir una key ZH y pulsar **Activar / verificar**; la aplicación consulta la Edge Function de Supabase usando HTTPS de Android (no requiere curl ni Termux).
5. Pulsar **Iniciar overlay y Free Fire**. **Detener overlay** intenta cerrar únicamente el ELF importado.

La app implementa en Kotlin el flujo de licencia del script original, y usa scripts shell pequeños, empaquetados como assets, para iniciar, detener y supervisar la expiración. **No ejecuta directamente el script `v1.3.sh` completo.**

## Compilación

El workflow `.github/workflows/android.yml` compila `assembleDebug` con Gradle 8.10.2, Java 17 y Android Gradle Plugin 8.7.3. Es una **APK debug**; su firma no es estable para distribuir actualizaciones públicas. Para producción, configurar una firma release segura mediante secretos de GitHub Actions.

## Limitaciones y seguridad

- Requiere una key vigente y conexión a Supabase al iniciar. El contador del dashboard puede mostrar el último vencimiento comprobado, pero **no sustituye** la verificación remota.
- Al vencer la key se supervisa el PID del overlay con un proceso root; Android puede retrasar el vigilante si suspende sus procesos. No es un mecanismo infalible contra manipulación de la hora.
- Importar un ELF y ejecutarlo como root es una operación de alto riesgo: utiliza únicamente scripts propios y de confianza.
- La compilación de GitHub Actions valida que la APK se construye, pero **no prueba** root, compatibilidad gráfica, kernel ni la ejecución del juego en hardware real.


## Pink Monet v1.1

Logo ZH minimalista, icono adaptable y diseño de tarjetas Material You en rosa.
El proyecto mantiene el soporte para Android 12+ ARM64 y root.
Descarga el APK actualizado y el patch ZIP desde la última compilación de **Actions**.
