# La Isla Perdida

Videojuego móvil 2D de narrativa interactiva (*dating sim*) hecho por el **equipo CtrlZ** para el curso BISOFT-38 Proyecto de Ingeniería del Software 4, Universidad CENFOTEC (Q03-2026). Construido con **Kotlin Multiplatform** y **Compose Multiplatform** para Android, Desktop JVM e iOS.

## De qué trata

Una isla, tres supervivientes (Valentina, Lucas y Lila) y cuatro fases de juego. De día se explora el mapa de la isla (recolectar, guardar cosas en la cabaña, dar regalos) y de noche se comparte una fogata con un superviviente: responder preguntas contra el reloj, ver cómo cambia la afinidad y su motivo, y jugar un minijuego. Lo que decides cambia tu vínculo con cada personaje y el final de la historia.

**Características**

- Rondas de preguntas con temporizador, afinidad por etapas y explicación del motivo de cada cambio.
- Mapa de la isla con ciclo de días, recolección, cabaña y regalos.
- Minijuego de pesca (Noche 1).
- Resumen de cada noche con consejos, guardado local y ajustes (velocidad del texto y volúmenes).
- Música y efectos de sonido (Android y escritorio).
- Contenido narrativo en archivos JSON editables, sin servidor: el juego funciona sin conexión.

## Tecnologías

Kotlin 2.4.20 · Compose Multiplatform 1.12.1 · Gradle 9.6.0 · Android Gradle Plugin 9.4.1 · kotlinx-serialization · multiplatform-settings. Android `minSdk` 24.

## Ejecutar

Requisitos: JDK 21 y Android Studio. La guía completa, con requisitos, configuración, despliegue y solución de problemas, está en el [**Manual de Instalación**](docs/Manual_de_Instalacion.md).

```bash
git clone https://github.com/Stephabarca/CtrlZ-Team.git
cd CtrlZ-Team
```

| Plataforma | Comando |
|---|---|
| Android (APK de depuración) | `./gradlew :androidApp:assembleDebug` |
| Desktop JVM | `./gradlew :desktopApp:run` |
| iOS | Abrir `iosApp/iosApp.xcodeproj` en Xcode (solo macOS) |

En Windows usa `gradlew.bat`.

## Pruebas

```bash
./gradlew :shared:jvmTest              # Desktop JVM
./gradlew :shared:testAndroidHostTest  # Android (en la JVM)
./gradlew :shared:iosSimulatorArm64Test  # iOS (solo macOS)
```

## Estructura

| Carpeta | Contenido |
|---|---|
| `shared/` | Lógica, pantallas, datos y recursos compartidos (`commonMain`), pruebas y código por plataforma |
| `androidApp/` | Aplicación Android |
| `desktopApp/` | Aplicación Desktop JVM |
| `iosApp/` | Proyecto Xcode (iOS) |
| `docs/` | Manuales: instalación y declaración de uso de IA |
| `shared/src/docs/` | Modelo de datos y esquema SQL |

## Documentación

- [Manual de Instalación](docs/Manual_de_Instalacion.md)
- [Anexo: Declaración de Uso de IAG](docs/Anexo_Declaracion_IAG.md) (también va dentro del Manual de Usuario)
- [Modelo de datos](shared/src/docs/modelo-de-datos.md)

## Uso de Inteligencia Artificial Generativa

El equipo utilizó herramientas de IAG como apoyo; el detalle (herramientas, tareas y ejemplos de prompt) está en el [Anexo de Declaración de IAG](docs/Anexo_Declaracion_IAG.md).

## Equipo

Equipo **CtrlZ**: [COMPLETAR: nombres de los integrantes y su rol].

## Créditos

Inspirado en las ideas de *Sobre el mito del amor romántico. Amores cinematográficos y educación*, de Alicia Pascual Fernández. Música y efectos: ver el anexo de IAG (sección Recursos de arte y audio).
