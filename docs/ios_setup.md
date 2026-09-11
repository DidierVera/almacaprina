# Correr la app iOS en un MacBook

Guía para dejar andando la versión iOS de Almacaprina. La UI es la misma de Android
(Compose Multiplatform en `shared/src/commonMain`); `iosApp` solo es el contenedor Xcode.

---

## 1. Limitación importante en MacBook Intel

**En un Mac Intel no se puede usar el simulador de iPhone con este proyecto.**

Compose Multiplatform 1.11.1 (`org.jetbrains.compose.runtime`, `foundation`, `ui`,
`material3`) ya no publica la variante `iosX64`, que es la que usa el simulador en Macs
Intel. Verificado resolviendo dependencias por target:

| Target Kotlin | Para qué sirve | Resuelve dependencias |
|---|---|---|
| `iosArm64` | iPhone físico | ✅ sí |
| `iosSimulatorArm64` | Simulador en Mac Apple Silicon (M1–M4) | ✅ sí |
| `iosX64` | Simulador en Mac Intel | ❌ no (no existe el artefacto) |

Por eso `shared/build.gradle.kts` declara solo `iosArm64` y `iosSimulatorArm64`.

**Opciones en un Mac Intel:**

1. **Correr en un iPhone físico conectado por cable** — es la vía recomendada y la que
   cubre esta guía. Funciona porque el iPhone es arm64.
2. Bajar Compose Multiplatform a una versión que todavía publique `iosX64`. No
   recomendado: `material3 1.11.0-alpha07`, `lifecycle 2.11.0-beta01` y
   `navigation-compose 2.9.2` esperan la versión actual, así que arrastraría una cadena
   de downgrades.
3. Usar un Mac Apple Silicon (propio o un Mac en la nube) para el simulador.

> En un Mac Apple Silicon esta guía sirve igual: basta con elegir un simulador en vez
> de un iPhone en el paso 5.

---

## 2. Requisitos en el Mac

| Herramienta | Versión | Por qué |
|---|---|---|
| macOS | 14.5 o superior | Requisito de Xcode 16 |
| Xcode | 16.0 o superior | `project.pbxproj` usa `objectVersion = 77` |
| Command Line Tools | las de Xcode 16 | `xcode-select --install` |
| JDK | 17 o 21 | AGP 9.0.1 y Gradle 9.1 lo exigen |

> Xcode 26 ya no corre en Macs Intel. Con Xcode 16.x este proyecto compila sin problema:
> el mínimo de despliegue quedó en iOS 16.0.

Instalación del JDK (Homebrew):

```sh
brew install --cask temurin@21
/usr/libexec/java_home -V     # debe listar el 21
```

No hace falta exportar `JAVA_HOME` a mano: la fase de build de Xcode lo resuelve sola
con `/usr/libexec/java_home` si no está definido.

---

## 3. Traer el proyecto al Mac

```sh
git clone <url-del-repo> almacaprina
cd almacaprina
git checkout feature/creating-database
```

**Clonar, no copiar la carpeta desde Windows.** `local.properties` está en `.gitignore`
y contiene un `sdk.dir` con ruta de Windows que rompería Gradle en el Mac.

Si solo vas a compilar iOS no necesitas `local.properties`. Si además quieres compilar
Android en el Mac, créalo con la ruta local del SDK:

```sh
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```

Verifica que el wrapper sea ejecutable (ya está marcado `100755` en git):

```sh
ls -l gradlew        # debe mostrar -rwxr-xr-x
./gradlew --version  # descarga Gradle 9.1.0 la primera vez
```

---

## 4. Compilar el framework compartido

Antes de abrir Xcode, comprueba que el código Kotlin compila para iOS:

```sh
./gradlew :shared:linkDebugFrameworkIosArm64
```

La primera vez descarga el toolchain de Kotlin/Native (~1 GB) y tarda varios minutos.

---

## 5. Abrir y correr desde Xcode

1. `open iosApp/iosApp.xcodeproj`
2. Conecta el iPhone por cable y confía en el Mac desde el teléfono.
3. En el iPhone: **Ajustes → Privacidad y seguridad → Modo de desarrollador** → activar
   y reiniciar (obligatorio desde iOS 16).
4. Firma de código — **paso necesario para instalar en un iPhone físico**:
   - Xcode → Settings → Accounts → añade tu Apple ID (una cuenta gratuita sirve; el
     perfil caduca cada 7 días y hay que reinstalar).
   - En Xcode, target `iosApp` → pestaña **Signing & Capabilities** → marca *Automatically
     manage signing* y elige tu Team.
   - Copia el Team ID (Xcode → Settings → Accounts → Manage Certificates, o
     developer.apple.com) y ponlo en `iosApp/Configuration/Config.xcconfig`:
     ```
     TEAM_ID=ABCDE12345
     ```
     Ese valor también entra en el bundle id
     (`com.didiprogrammer.almacaprina.almacaprina$(TEAM_ID)`), lo que evita choques si
     alguien más ya registró el identificador base.
5. Elige tu iPhone como destino y ▶ **Run**.
6. La primera vez el iPhone rechaza el certificado: **Ajustes → General → VPN y gestión
   de dispositivos → confía en tu perfil de desarrollador**. Vuelve a abrir la app.

Al correr, Xcode ejecuta la fase *Compile Kotlin Framework*, que llama a
`./gradlew :shared:embedAndSignAppleFrameworkForXcode` y deja `Shared.framework` dentro
del `.app`.

---

## 6. Qué se cambió para dejarlo listo

| Archivo | Cambio | Motivo |
|---|---|---|
| `gradlew` | Modo git `100644` → `100755` | Sin el bit de ejecución, la fase de build de Xcode fallaba con `Permission denied` |
| `.gitattributes` (nuevo) | `gradlew`/`*.sh` forzados a LF; `*.pbxproj` sin normalizar | El repo se trabaja en Windows con `core.autocrlf=true`; un `gradlew` con CRLF da `bad interpreter` en macOS |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | `IPHONEOS_DEPLOYMENT_TARGET` 18.2 → 16.0 | Permite iPhones desde iOS 16 y no obliga a Xcode 16.2+ |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | Se quitó `ARCHS = arm64` fijo | Deja que Xcode use `ARCHS_STANDARD` según el destino |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | Fallback de `JAVA_HOME` vía `/usr/libexec/java_home` en la fase *Compile Kotlin Framework* | Xcode no hereda el entorno de la terminal; sin esto Gradle falla con "Unable to locate a Java Runtime" |
| `iosApp/iosApp/Info.plist` | `NSPhotoLibraryUsageDescription`, `NSCameraUsageDescription`, `CFBundleDisplayName` | El formulario de cabra usa el selector de imágenes (peekaboo); sin las descripciones iOS mata la app al abrir el picker |
| `shared/build.gradle.kts` | Comentario explicando por qué no hay `iosX64` | Evita que alguien lo agregue y choque con el mismo error |

No se tocó nada de configuración de Supabase: `SupabaseConfig` tiene la URL y la clave
publicable en `commonMain`, así que iOS las toma igual que Android.

---

## 7. Problemas frecuentes

**`./gradlew: Permission denied` en la fase de Xcode**
```sh
chmod +x gradlew
```

**`Unable to locate a Java Runtime`**
El JDK no está instalado o `java_home` no lo encuentra. Instala Temurin 21 (paso 2) y
verifica con `/usr/libexec/java_home -V`.

**`Could not find org.jetbrains.compose...:...-iosx64`**
Alguien agregó el target `iosX64`. Quítalo — ver sección 1.

**`No such module 'Shared'` en `ContentView.swift`**
Gradle no alcanzó a generar el framework. Corre a mano
`./gradlew :shared:linkDebugFrameworkIosArm64`, luego en Xcode
**Product → Clean Build Folder** (⇧⌘K) y vuelve a correr.

Si persiste, agrega en Xcode (target `iosApp` → Build Settings → Framework Search Paths):
```
$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)
```

**La caché de configuración de Gradle da problemas al invocarse desde Xcode**
Desactívala temporalmente en `gradle.properties`:
```
org.gradle.configuration-cache=false
```

**El build tarda muchísimo la primera vez**
Normal: se descargan Gradle 9.1, el toolchain de Kotlin/Native y todas las dependencias.
Las siguientes compilaciones usan caché.
