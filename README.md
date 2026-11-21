# Ración · Tu Diario de Mercado — Kotlin Multiplatform

Migración de la app **Ración** (originalmente Android/Jetpack Compose y luego iOS/SwiftUI) a un
proyecto **Kotlin Multiplatform + Compose Multiplatform**. Toda la lógica de negocio, la
persistencia, la red, el estado y **la navegación** viven en código compartido; Android e iOS
solo aportan el punto de entrada nativo.

| | SwiftUI (entrega anterior) | Esta entrega (KMP) |
|---|---|---|
| UI | SwiftUI + design system propio | **Compose Multiplatform** (`commonMain`) |
| Estado | `ObservableObject` + `@Published` | `StateFlow` + ViewModels compartidos |
| Navegación | `NavigationStack` + `Route` | **Voyager** en `commonMain` (un solo `Navigator`) |
| Red | `URLSession` + `Codable` | **Ktor Client** + `ContentNegotiation` + `kotlinx.serialization` |
| Persistencia | Archivos JSON en Application Support | **multiplatform-settings** (SharedPreferences / NSUserDefaults / Preferences) |
| Modelos | `struct` + `Codable` | `@Serializable data class` |
| Build | Xcode | Gradle Kotlin DSL (`:shared` + `:composeApp`) |
| Plataformas | iOS | **Android + iOS ( + Desktop opcional)** |

## Estado de la entrega

Verificado en esta máquina (Windows, JDK 21, Android SDK 35):

- ✅ `.\gradlew.bat :shared:desktopTest` → **14 pruebas, 0 fallos**.
- ✅ `.\gradlew.bat :composeApp:compileKotlinDesktop` → compila.
- ✅ `.\gradlew.bat :composeApp:assembleDebug` → genera `composeApp/build/outputs/apk/debug/composeApp-debug.apk`.
- ⚠️ iOS no se puede compilar en Windows; el host SwiftUI + XcodeGen está listo y se compila en macOS (ver abajo).

> La entrega SwiftUI anterior se conserva intacta en `legacy-swiftui/` (`Racion/`, `Racion.xcodeproj/`, `firestore.rules`) y
> [`legacy-swiftui/docs/README-SWIFTUI.md`](legacy-swiftui/docs/README-SWIFTUI.md). El código KMP nuevo no la modifica.

## Arquitectura

```
┌──────────────────────────────┐        ┌───────────────────────────────────┐
│ :composeApp                  │        │ :shared                           │
│  Compose Multiplatform UI    │──────▶ │  modelos @Serializable            │
│  Voyager (navegación)        │        │  AppStore + StateFlow             │
│  MainActivity / MainVC / main│        │  ViewModels (UI State)            │
└──────────────────────────────┘        │  RacionStorage (multiplatform-    │
                                        │    settings + JSON)               │
                                        │  Ktor + Open Food Facts + catálogo│
                                        └───────────────────────────────────┘
```

- **`:shared`** es Kotlin puro (sin Compose). Expone `AppContainer`, que construye
  `AppStore`, `RacionStorage`, el catálogo local y los ViewModels. La plataforma solo
  aporta una instancia de `Settings`.
- **`:composeApp`** contiene **toda** la UI y la navegación en `commonMain`. No hay
  `NavHost` de Android ni `NavigationStack` en SwiftUI.
- **ViewModels compartidos**: cada pantalla recibe un `StateFlow<UiState>` inmutable;
  las acciones (agregar comida, guardar metas, escanear) son métodos del ViewModel que
  delegan en `AppStore`.

### Estructura

```
.
├── shared/
│   └── src/
│       ├── commonMain/kotlin/com/racion/app/shared/
│       │   ├── models/        Nutrition, FoodProduct, DiaryEntry, NutritionGoals,
│       │   │                  UserProfile, DailySummary, WeeklyReport, AppSettings,
│       │   │                  AppError/AppResult
│       │   ├── data/          SeedData, LocalCatalog, ReportBuilder, AppFormat
│       │   ├── persistence/   RacionStorage (JSON sobre multiplatform-settings)
│       │   ├── network/       FoodCatalogService, OpenFoodFactsService, Ktor DTOs
│       │   ├── state/         AppState + AppStore (única fuente de verdad)
│       │   ├── viewmodel/     Home, Diary, Goals, Report, Credits, AddFood,
│       │   │                  Scanner, Confirm, SportProfile
│       │   └── di/            AppContainer
│       ├── androidMain/       Settings con SharedPreferences + motor OkHttp
│       ├── iosMain/           Settings con NSUserDefaults + motor Darwin
│       ├── desktopMain/       Settings con Preferences + motor CIO
│       └── commonTest/        Nutrition, ReportBuilder y RacionStorage
├── composeApp/
│   └── src/
│       ├── commonMain/kotlin/com/racion/app/
│       │   ├── App.kt                     tema + Navigator raíz
│       │   └── ui/
│       │       ├── navigation/RootScreen.kt
│       │       ├── screens/   Home, Diary, Goals, Report, Credits (+Contenido),
│       │       │              AddFood, Confirm, Scanner, SportProfile, Onboarding
│       │       └── theme/     RacionTheme, Components, Charts
│       ├── androidMain/       MainActivity + AndroidManifest + recursos
│       ├── iosMain/           MainViewController
│       └── desktopMain/       main.kt (ventana de escritorio)
├── iosApp/                    Host SwiftUI + project.yml (XcodeGen)
├── legacy-swiftui/            Entrega SwiftUI anterior + su documentación
└── gradle/libs.versions.toml  Catálogo de versiones
```

## Requisitos

| Herramienta | Versión usada |
|---|---|
| JDK | **17 o 21** (probado con JBR 21.0.11 y Temurin 21.0.13) |
| Android Studio | Ladybug o superior (AGP 8.7.3) |
| SDK de Android | Platform 35 + Build-Tools 34 (AGP los descarga si faltan) |
| Kotlin | 2.1.0 |
| Compose Multiplatform | 1.7.3 |
| Gradle | 8.9 (wrapper incluido) |
| macOS + Xcode | 16+ y **XcodeGen** (solo para iOS) |

> **JDK:** Gradle 8.9 no soporta Java 25. Si tu `java -version` muestra 25, definí
> `JAVA_HOME` a un JDK 17/21 antes de compilar.

> **Windows — `java` no se reconoce (CommandNotFoundException):**
>
> 1. Definí `JAVA_HOME` con la ruta del JDK 21, por ejemplo
>    `C:\Users\TU_USUARIO\.jdks\jbr-21.0.11`.
> 2. Agregá al `Path` **de usuario** la ruta literal
>    `C:\Users\TU_USUARIO\.jdks\jbr-21.0.11\bin`
>    (no uses `%JAVA_HOME%\bin`: al guardarse con `SetEnvironmentVariable` queda como
>    texto sin expandir y PowerShell no lo resuelve).
> 3. Cerrá y volvé a abrir la terminal (y Android Studio si estaba abierto).
> 4. Verificá con `java -version`. Debe decir `21.x`.

## Cómo compilar y ejecutar

### Android

1. Creá `local.properties` en la raíz (una sola vez):

   ```properties
   sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
   ```

   En macOS/Linux: `sdk.dir=/Users/TU_USUARIO/Library/Android/sdk`.

2. Compilá el APK:

   ```bash
   ./gradlew :composeApp:assembleDebug
   # Windows
   .\gradlew.bat :composeApp:assembleDebug
   ```

   Salida: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`.

3. Instalá en un emulador o dispositivo conectado:

   ```bash
   adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
   adb shell am start -n com.racion.app/.MainActivity
   ```

   También podés abrir la raíz del proyecto en Android Studio, esperar la sincronización
   de Gradle y ejecutar la configuración **composeApp** (▶ Run). El `applicationId` es
   `com.racion.app` y `minSdk` es 24.

### iOS (macOS)

El proyecto de Xcode se **genera** con XcodeGen; no se versiona el `.xcodeproj` a mano.

```bash
# 1. XcodeGen (una sola vez)
brew install xcodegen

# 2. Desde la raíz del repo: permisos del wrapper
chmod +x gradlew

# 3. Generar RacionKMP.xcodeproj
cd iosApp
xcodegen generate
open RacionKMP.xcodeproj
```

En Xcode elegí el esquema **RacionKMP**, un simulador de iPhone y pulsá **Run** (⌘R).
Antes de compilar Swift, el script *Compile Kotlin Framework* ejecuta:

```bash
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```

Requisitos para ese script:

- Un JDK 17/21 visible para Xcode. Si Xcode no encuentra Java, agregá a
  `gradle.properties` (local, no lo subas) `org.gradle.java.home=/ruta/al/jdk-21`.
- La primera compilación tarda varios minutos porque descarga Kotlin/Native.

Compilación por terminal:

```bash
cd iosApp
xcodebuild -project RacionKMP.xcodeproj -scheme RacionKMP \
           -sdk iphonesimulator -configuration Debug \
           -destination 'generic/platform=iOS Simulator' \
           CODE_SIGNING_ALLOWED=NO build
```

El host SwiftUI (`iosApp/iOSApp.swift` + `ContentView.swift`) solo envuelve
`MainViewControllerKt.MainViewController()`; no contiene navegación ni lógica.

### Desktop (opcional, ideal para verificar sin emulador)

```bash
./gradlew :composeApp:run
# Windows
.\gradlew.bat :composeApp:run
```

Abre una ventana de 430 × 900 dp con exactamente la misma UI y navegación que Android/iOS.
El comando queda ocupado mientras la ventana esté abierta; al cerrarla termina con código 0.

> **Windows, sin terminal:** doble clic en `run-desktop.bat`. El script busca solo un JDK
> 17–22 en la máquina, lo usa y abre la app. No hace falta configurar `JAVA_HOME` ni el `PATH`.

### Lanzadores con doble clic (Windows)

| Archivo | Qué hace |
|---|---|
| `run-desktop.bat` | Compila y abre la app de escritorio |
| `run-android.bat` | Compila el APK, lo instala y lo abre en el emulador/teléfono |

Ambos llaman a `scripts/racion.ps1`, que **no tiene rutas fijas**: detecta el JDK
(`JAVA_HOME`, `%USERPROFILE%\.jdks`, `C:\Program Files\Java`, Adoptium, Corretto, Zulu,
JBR de Android Studio…) y el SDK de Android (`ANDROID_HOME`, `ANDROID_SDK_ROOT`,
`%LOCALAPPDATA%\Android\Sdk`, `local.properties`).

### Llevar el proyecto a otra PC

| Plataforma | Qué necesita esa PC | Cómo se ejecuta |
|---|---|---|
| **Android** | Android Studio + SDK + emulador (o teléfono con depuración USB) | ▶ Run sobre `composeApp`, o `run-android.bat` |
| **Desktop** | Solo un JDK 17–22 (p. ej. Temurin 21) | `run-desktop.bat`, o `.\gradlew.bat :composeApp:run` |
| **iOS** | macOS + Xcode 16 + XcodeGen | ver la sección iOS |

- **No copies** `build/`, `.gradle/` ni `.kotlin/`: son artefactos y se regeneran.
- **Borrá `local.properties`** si el proyecto viene de otra máquina: guarda la ruta absoluta
  del SDK de Android de *esa* máquina. Android Studio la reescribe al sincronizar y
  `run-android.bat` la resuelve sola.
- **El JDK no viaja con el proyecto.** Si falta, Gradle falla con
  `Unable to locate a Java Runtime`. Instalá Temurin 21 en la PC destino.
- La primera compilación **necesita internet** y puede tardar varios minutos (descarga
  Gradle, Kotlin, Compose y AndroidX).
- En Android Studio revisá **Settings → Build, Execution, Deployment → Build Tools →
  Gradle → Gradle JDK**: debe ser 17–22. El JBR que trae Android Studio suele ser Java 25
  y Gradle 8.9 lo rechaza.

## Pruebas

```bash
./gradlew :shared:desktopTest
```

Incluye 14 comprobaciones ejecutables sobre:

- `Nutrition`: escalado, redondeo único y suma de nutrientes.
- `ReportBuilder`: semana de lunes a domingo, promedio sobre días con datos, mejor día,
  racha y semana vacía.
- `RacionStorage`: ida y vuelta de diario, metas, perfil y preferencias sobre
  `MapSettings`, más tolerancia a JSON corrupto.

## Persistencia

`RacionStorage` guarda **JSON** en `multiplatform-settings` bajo cuatro claves estables:

| Clave | Contenido |
|---|---|
| `diary.json` | Lista de `DiaryEntry` (producto, porciones, comida, fecha, total ya calculado) |
| `goals.json` | `NutritionGoals` (peso, kcal, días activos, macros) |
| `profile.json` | `UserProfile` (nombre, peso, altura, enfoque deportivo) |
| `settings.json` | `AppSettings` (modo oscuro, onboarding, demo sembrada, recordatorios) |

- **Android:** `SharedPreferences` (`racion_settings`).
- **iOS:** `NSUserDefaults`.
- **Desktop:** `java.util.prefs.Preferences`.

La demo se siembra **una sola vez** (`hasSeededDemoData`); después el diario es del
usuario y sobrevive a reinicios. `AppStore.resetDemoData()` la restaura sin tocar metas
ni perfil. Si una escritura falla, el estado expone `storageError` y la UI muestra un
aviso en lugar de fallar en silencio.

## Red y catálogo local

`OpenFoodFactsService` (Ktor 3 + ContentNegotiation + kotlinx.serialization) implementa
`FoodCatalogService`:

- `search(query)` → `cgi/search.pl` con proyección de campos.
- `product(barcode)` → `/api/v2/product/{barcode}.json`.
- **Un código inexistente devuelve HTTP 200 con `status = 0`**: se chequea `status`, no
  solo el código HTTP.
- 404 → `NotFound`; 429/503 → `RateLimited`; resto → `Server(código)`; sin red →
  `Network`. `expectSuccess = false` y todo error se traduce a `AppError` con mensaje
  para la persona.
- Si la consulta falla, `ScannerViewModel`/`AddFoodViewModel` caen al **catálogo local**
  (`LocalCatalog`, platos típicos colombianos) y la UI avisa que está mostrando datos
  offline.

Motores por plataforma: **OkHttp** (Android), **Darwin** (iOS), **CIO** (Desktop).

## Navegación compartida

Un único `Navigator` de Voyager arranca en `RootScreen` (`composeApp/.../ui/navigation`):

- `RootScreen` mantiene las 4 pestañas como estado local (`Inicio`, `Diario`, `Escanear`,
  `Perfil`) dentro de un `Scaffold` de Material 3.
- Los detalles se apilan con `navigator.push(...)`: Informe, Agregar alimento,
  Confirmar, Perfil deportivo y Créditos. Volver usa `navigator.pop()`; tras agregar
  una comida, `navigator.popUntilRoot()` regresa a las pestañas.
- El **onboarding** (aviso legal → enfoque deportivo) se muestra a pantalla completa
  mientras `settings.onboardingCompleted == false`, también desde código compartido.

Android e iOS no contienen ninguna ruta ni transición: solo llaman a `App(container)`.

## Funcionalidad migrada

| Pantalla / flujo | Archivo compartido |
|---|---|
| Inicio: resumen del día, progreso, accesos | `ui/screens/HomeScreen.kt` |
| Diario: por comida, editar porciones, borrar, vaciar, restaurar demo | `ui/screens/DiaryScreen.kt` |
| Metas y perfil: sliders, días activos, macros, recordatorios | `ui/screens/GoalsScreen.kt` |
| Informe semanal: barras, donut, promedio, racha | `ui/screens/ReportScreen.kt` |
| Créditos: equipo, fuentes, tecnología, modo oscuro | `ui/screens/CreditsScreen.kt` |
| Agregar alimento: búsqueda local/remota, categorías | `ui/screens/AddFoodScreen.kt` |
| Confirmar: porciones, comida, totales | `ui/screens/ConfirmScreen.kt` |
| Escanear: código manual, lectura simulada, recientes | `ui/screens/ScannerScreen.kt` |
| Perfil deportivo: enfoque y macros sugeridos | `ui/screens/SportProfileScreen.kt` |
| Onboarding: aviso + enfoque | `ui/screens/OnboardingOverlay.kt` |

Reglas de negocio compartidas y verificables: `Nutrition` es la única clase que hace
aritmética de nutrientes (redondeo una sola vez), `ReportBuilder` calcula el informe
desde las entradas reales y `AppStore` es la única que escribe en disco.

## Migración SwiftUI → KMP

| SwiftUI (entrega anterior) | KMP (esta entrega) |
|---|---|
| `Models/Nutrition.swift` | `shared/models/Nutrition.kt` |
| `Models/FoodProduct.swift` | `shared/models/FoodProduct.kt` |
| `Models/DiaryEntry.swift` | `shared/models/DiaryEntry.kt` + `MealSlot` |
| `Models/NutritionGoals.swift` | `shared/models/NutritionGoals.kt` |
| `Models/UserProfile.swift` | `shared/models/UserProfile.kt` + `SportFocus` |
| `Models/WeeklyReport.swift` | `shared/models/WeeklyReport.kt` + `DayTotal` |
| `Models/DailySummary.swift` | `shared/models/DailySummary.kt` + `MealGroup` |
| `Services/AppModel.swift` | `shared/state/AppStore.kt` + `AppState` |
| `Services/PersistenceService.swift` | `shared/persistence/RacionStorage.kt` |
| `Services/OpenFoodFactsService.swift` | `shared/network/OpenFoodFactsService.kt` (Ktor) |
| `Services/LocalCatalog.swift` | `shared/data/LocalCatalog.kt` |
| `ViewModels/*.swift` | `shared/viewmodel/*.kt` (`StateFlow<UiState>`) |
| `Views/*.swift` | `composeApp/ui/screens/*.kt` |
| `Navigation/Route.swift` + `RootView` | `composeApp/ui/navigation/RootScreen.kt` (Voyager) |
| `RacionApp.swift` | `MainActivity.kt` / `MainViewController.kt` |

## Limitaciones conocidas

- **Cámara real:** la lectura de códigos de barras con cámara queda fuera del módulo
  compartido. La pantalla de escaneo permite escribir el código o usar *lectura
  simulada*; ambos caminos usan el mismo `ScannerViewModel.resolve`, listo para
  conectarse a una cámara nativa con `handleDetectedCode(code)`.
- **Sin backend propio:** el catálogo en línea depende de Open Food Facts (límite de
  peticiones y disponibilidad). El catálogo local garantiza que la app nunca quede sin
  respuesta.
- **Cuenta local:** no hay autenticación ni sincronización; los datos viven en el
  dispositivo.
- **iOS:** compilación no verificable en Windows. El proyecto XcodeGen y el framework
  estático `ComposeApp` están configurados; requiere macOS + Xcode.

## Documentación adicional

- [`legacy-swiftui/docs/README-SWIFTUI.md`](legacy-swiftui/docs/README-SWIFTUI.md) — entrega iOS/SwiftUI anterior.
- [`legacy-swiftui/docs/BUILD.md`](legacy-swiftui/docs/BUILD.md), [`legacy-swiftui/docs/REFLEXION.md`](legacy-swiftui/docs/REFLEXION.md),
  [`legacy-swiftui/docs/RUBRICA.md`](legacy-swiftui/docs/RUBRICA.md) — documentación académica previa.

---

**Equipo:** Daniel Garcia · Julian Carvajal
**Electiva Aplicaciones Móviles — Corte 3**
