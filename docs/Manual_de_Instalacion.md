# Manual de Instalación — La Isla Perdida

**Equipo CtrlZ · BISOFT-38 Proyecto de Ingeniería del Software 4 · Universidad CENFOTEC · Q03-2026**

Guía técnica paso a paso para clonar, configurar, compilar, ejecutar las pruebas unitarias y desplegar el videojuego en **Android**, **Desktop JVM** e **iOS**.

> Los comandos de ejemplo usan Windows (`gradlew.bat`). En macOS o Linux se usa `./gradlew` con los mismos argumentos.

## 1. Qué es el proyecto

*La Isla Perdida* es un videojuego móvil 2D de narrativa interactiva construido con **Kotlin Multiplatform** y **Compose Multiplatform**. Casi todo el código vive en un módulo compartido (`shared/src/commonMain`) que se ejecuta en Android, iOS y escritorio (JVM).

| Módulo | Para qué sirve | Se construye con |
|---|---|---|
| `shared` | Lógica del juego, pantallas, datos y recursos (`commonMain`), más código propio de cada plataforma (`androidMain`, `jvmMain`, `iosMain`) | Gradle |
| `androidApp` | Aplicación Android (punto de entrada) | Gradle |
| `desktopApp` | Aplicación de escritorio (Desktop JVM), útil para pruebas rápidas | Gradle |
| `iosApp` | Proyecto de Xcode (punto de entrada iOS) | Xcode (llama a Gradle) |

El juego funciona 100 % sin conexión: no necesita servidor ni base de datos externa.

## 2. Requisitos previos

### 2.1 Software

| Herramienta | Versión | Para qué |
|---|---|---|
| Git (o GitHub Desktop) | cualquiera reciente | Clonar el repositorio |
| JDK | **21** (el proyecto usa Azul Zulu 21) | Compilar. Gradle puede descargarlo solo (ver sección 4.3) |
| Android Studio | Una versión reciente compatible con el Android Gradle Plugin 9.4.1 | Abrir, compilar y ejecutar |
| Android SDK | Plataforma **37** (`compileSdk`) | Compilar la app Android |
| Gradle | 9.6.0 (viene en el *wrapper*, **no se instala**) | Automatizar la construcción |
| Xcode | Última estable (solo macOS) | Compilar y ejecutar en iOS |

### 2.2 Versiones del proyecto

Definidas en `gradle/libs.versions.toml` y `shared/build.gradle.kts`.

| Componente | Versión |
|---|---|
| Kotlin | 2.4.20 |
| Android Gradle Plugin | 9.4.1 |
| Compose Multiplatform | 1.12.1 |
| Material 3 | 1.12.0-alpha03 |
| kotlinx-coroutines | 1.11.0 |
| kotlinx-serialization-json | 1.9.0 |
| multiplatform-settings | 1.3.0 |
| JLayer (decodifica mp3 en escritorio) | 1.0.1 |
| Android `minSdk` / `targetSdk` / `compileSdk` | 24 / 37 / 37 |

El juego corre en Android 7.0 (API 24) o superior.

### 2.3 Hardware y red

- Conexión a internet **solo la primera vez** (Gradle descarga dependencias, el JDK y el SDK).
- Unos 10 GB libres en disco (Android Studio, SDK, emulador y caché de Gradle).
- Para el emulador Android: virtualización activada en la BIOS (Hyper-V / WHPX en Windows) y al menos 8 GB de RAM.
- Para iOS: una Mac con Xcode (no se puede compilar iOS desde Windows ni Linux).

## 3. Clonar el repositorio

1. Instala Git y comprueba la instalación: `git --version`.
2. Clona el repositorio:

   ```bash
   git clone https://github.com/Stephabarca/CtrlZ-Team.git
   cd CtrlZ-Team
   ```

   Con **GitHub Desktop**: *File → Clone repository → URL*, pega `https://github.com/Stephabarca/CtrlZ-Team.git`, elige una carpeta local y pulsa *Clone*.

3. Cambia a la rama de entrega (la integrada y estable):

   ```bash
   git checkout main
   ```

   *(Equipo: confirmen cuál es la rama final antes de publicar este manual, y corrijan este paso si no es `main`.)*

4. Ramas del repositorio: `main` (entrega), `Desarrollo` (integración del equipo) y ramas por integrante o por funcionalidad (`feature/ep-12-mapa-isla`, `feature/ip-11-ronda-preguntas`, etc.).

## 4. Configurar el proyecto

### 4.1 Abrir en Android Studio

1. *File → Open* y elige la carpeta raíz `CtrlZ-Team` (la que contiene `settings.gradle.kts`, no la carpeta `shared`).
2. Espera a que termine la **sincronización de Gradle** (*Sync*). La primera vez tarda varios minutos porque descarga dependencias.
3. Si pide instalar componentes del SDK, acéptalo (plataforma Android 37).

### 4.2 Archivo `local.properties`

Android Studio lo crea solo con la ruta del SDK. No se sube a Git (está en `.gitignore`). Si lo necesitas a mano, en la raíz del proyecto:

```properties
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
```

### 4.3 JDK

El proyecto pide JDK 21. En *Settings → Build, Execution, Deployment → Build Tools → Gradle*, el campo **Gradle JDK** debe apuntar a un JDK 21 (por ejemplo `zulu-21`). Si no tienes uno, Gradle lo descarga automáticamente al sincronizar gracias al plugin *foojay-resolver*.

### 4.4 Recursos y contenido

- Imágenes, fuentes y sonidos están en `shared/src/commonMain/composeResources/` y se acceden con la clase generada `Res`.
- Las carpetas de primer nivel válidas dentro de `composeResources` son solo: `drawable`, `font`, `values` y `files`. Cualquier otra (por ejemplo `sprites`) rompe la compilación con *Unknown resource type* (ver sección 8).
- El contenido narrativo (personajes, noches, preguntas, motivos, finales y minijuegos) está en JSON en `composeResources/files/` y se puede editar sin tocar código. El modelo de datos está documentado en `shared/src/docs/modelo-de-datos.md`.

## 5. Compilar y ejecutar

### 5.1 Android

**Desde Android Studio**

1. Crea un emulador: *Device Manager → Create Device* (por ejemplo un Pixel con Android 14 o superior) o conecta un celular con **depuración USB** activada.
2. Elige la configuración de ejecución **androidApp** y el dispositivo en la barra superior.
3. Pulsa **Run ▶**.

**Desde la terminal**

```bash
.\gradlew.bat :androidApp:assembleDebug
```

El APK queda en `androidApp/build/outputs/apk/debug/androidApp-debug.apk`. Para instalarlo en un dispositivo o emulador conectado:

```bash
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

### 5.2 Desktop JVM

```bash
.\gradlew.bat :desktopApp:run
```

Con recarga en caliente durante el desarrollo: `.\gradlew.bat :desktopApp:hotRun --auto`. También se puede usar la configuración de ejecución **desktopApp** del IDE. Se abre una ventana con el juego.

### 5.3 iOS (solo macOS)

1. Instala Xcode y abre `iosApp/iosApp.xcodeproj`.
2. En *Signing & Capabilities*, elige tu **Team** (una cuenta de Apple ID gratuita sirve para simulador y para tu propio dispositivo).
3. Selecciona un simulador o dispositivo y pulsa **Run ▶**.

Xcode ejecuta automáticamente `./gradlew :shared:embedAndSignAppleFrameworkForXcode` para compilar el módulo compartido, así que la primera compilación es lenta.

> **Estado en esta entrega:** el equipo desarrolló y probó en Android y Desktop JVM. El proyecto está configurado para iOS, pero esta plataforma **no fue verificada** en esta entrega. Además, en iOS aún no hay música ni efectos de sonido (el audio es un reproductor vacío, pendiente de implementar con `AVAudioPlayer`).

## 6. Ejecutar las pruebas unitarias

El proyecto tiene alrededor de 227 casos de prueba (anotaciones `@Test`) sobre la lógica del juego: motor de diálogo, temporizador, rondas de preguntas, reglas de progreso y afinidad, guardado, ciclo de días, cabaña, minijuego de pesca, resumen de noche, ajustes, menú y audio. Están en `shared/src/commonTest` (compartidas), `shared/src/jvmTest` y `shared/src/androidHostTest`.

| Objetivo | Comando |
|---|---|
| Desktop JVM (el más rápido, recomendado) | `.\gradlew.bat :shared:jvmTest` |
| Android (en la JVM, sin emulador) | `.\gradlew.bat :shared:testAndroidHostTest` |
| iOS (solo macOS) | `./gradlew :shared:iosSimulatorArm64Test` |

También puedes ejecutar una prueba o una clase desde el IDE con el botón verde junto a la función.

**Resultado esperado:** `BUILD SUCCESSFUL` y ningún test en rojo. El informe en HTML queda en `shared/build/reports/tests/`. Si una prueba falla, Gradle muestra su nombre y el motivo; ábrelo en el informe para ver el detalle.

## 7. Despliegue

### 7.1 Android

- **APK de depuración** (para demostraciones y pruebas): el de la sección 5.1. Se instala con `adb` o copiándolo al celular y permitiendo *instalar apps de origen desconocido*.
- **APK de publicación** (para *Releases* de GitHub): `.\gradlew.bat :androidApp:assembleRelease`. Sin una firma configurada, Android no instala un APK *release*. Genera un *keystore* (*Build → Generate Signed App Bundle / APK*), firma el APK y guarda el *keystore* **fuera** del repositorio.
- **Publicar en GitHub Releases:** en GitHub, *Releases → Draft a new release*, crea una etiqueta (por ejemplo `v1.0.0`), adjunta el `.apk` y publica.

### 7.2 Desktop JVM

La aplicación de escritorio está configurada (en `desktopApp/build.gradle.kts`) para generar instaladores `.msi` (Windows), `.dmg` (macOS) y `.deb` (Linux). Cada formato se genera **solo en su propio sistema operativo**:

```bash
.\gradlew.bat :desktopApp:packageMsi    # en Windows
./gradlew :desktopApp:packageDmg        # en macOS
./gradlew :desktopApp:packageDeb        # en Linux
```

En Windows, `packageMsi` requiere tener instalado el **WiX Toolset**. Los instaladores quedan en `desktopApp/build/compose/binaries/main/`. Para simplemente ejecutar el juego en una máquina de desarrollo no hace falta empaquetar: basta `:desktopApp:run`.

### 7.3 iOS

Requiere macOS. Para instalar en un iPhone propio: conéctalo, selecciónalo en Xcode y ejecuta (la primera vez hay que confiar en el perfil de desarrollador en *Ajustes → General → Gestión de dispositivos*). Para distribuir a otras personas se necesita una cuenta de desarrollador de Apple de pago (TestFlight o App Store); esto queda fuera del alcance de la entrega.

## 8. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Unknown resource type: 'sprites'` al compilar | Hay una carpeta de primer nivel no válida dentro de `composeResources` | Mueve las imágenes a `composeResources/drawable` (o `files`) y elimina la carpeta. Luego `.\gradlew.bat clean` y sincroniza |
| *Unresolved reference* en `Res.string...` o `Res.drawable...` | Los recursos generados quedaron desactualizados por un error previo | Corrige el error de recursos, ejecuta `.\gradlew.bat clean` y *Sync Project with Gradle Files* |
| *SDK location not found* | Falta `local.properties` o `sdk.dir` es incorrecto | Ver sección 4.2 |
| Gradle falla por la versión de Java | Gradle JDK distinto de 21 | Ver sección 4.3 |
| La app abre en negro en el emulador | Emulador sin aceleración, imagen del sistema dañada o excepción al iniciar | Reinicia con *Cold Boot* o prueba otro emulador, y revisa **Logcat** (nivel *Error*) para ver la excepción |
| La primera sincronización tarda mucho | Gradle descarga dependencias | Es normal; necesita internet y puede tardar varios minutos |
| El `.msi` no se genera | Falta el WiX Toolset en Windows | Instálalo y vuelve a ejecutar `packageMsi` |
| No se oye la música en escritorio | El mp3 se decodifica con JLayer; falla si el archivo falta o el dispositivo de audio no está disponible | Verifica `composeResources/files/music/` y el dispositivo de audio del sistema. El juego sigue funcionando sin sonido |

## 9. Estructura del repositorio

```text
CtrlZ-Team/
├── androidApp/        App Android
├── desktopApp/        App Desktop JVM
├── iosApp/            Proyecto Xcode (iOS)
├── shared/
│   └── src/
│       ├── commonMain/      Lógica, pantallas y recursos compartidos
│       │   ├── kotlin/com/example/videojuego/
│       │   │   ├── data/ narrative/ round/ timer/ world/
│       │   │   ├── minigame/ summary/ settings/ menu/ audio/
│       │   │   └── ui/   (pantallas y componentes Compose)
│       │   └── composeResources/ (drawable, font, files)
│       ├── commonTest/ jvmTest/ androidHostTest/ iosTest/   Pruebas
│       ├── androidMain/ jvmMain/ iosMain/    Código por plataforma
│       └── docs/            Modelo de datos y esquema SQL
├── gradle/            Versiones (libs.versions.toml) y wrapper
└── docs/              Manuales (instalación, usuario, IAG)
```
