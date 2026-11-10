<div align="center">

# trustfenc

**A modern, out-of-the-box Android app trustfenc built with Jetpack Compose, Material 3, MVVM and Navigation3.**

**English** | [简体中文](README.zh-CN.md)

![License](https://img.shields.io/badge/license-MIT-green)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.21-purple)
![AGP](https://img.shields.io/badge/AGP-9.5.1-blue)
![Gradle](https://img.shields.io/badge/Gradle-9.9.0-blue)
![Compose BOM](https://img.shields.io/badge/Compose%20BOM-2026.09.00-blue)
![minSdk](https://img.shields.io/badge/minSdk-34-orange)
![targetSdk](https://img.shields.io/badge/targetSdk-37-orange)

</div>

**trustfenc** is a production-ready starting point for new Android apps. The project skeleton and everyday plumbing are already wired up, so you can focus on business features instead of boilerplate.

## Features

- **Single-Activity architecture** with Jetpack Compose + Material 3, edge-to-edge rendering, and per-orientation system-bar visibility
- **MVVM with unidirectional data flow (UDF)** — app-wide state (theme / language / version) is aggregated in an Activity-scoped `MainViewModel` and consumed across the tree via a `CompositionLocal`; each screen adds its own immutable `UiState` exposed as a `StateFlow`. Events flow up, state flows down
- **Atomic UI decomposition + responsive layout** — the window size class is computed once in the screen entry and passed down as a parameter; form differences are expressed as layout flags (a content max-width on wide windows) instead of a second UI tree, so every form shares one state and one set of components
- **Navigation3** with typed routes and an explicit back stack (double-back-to-exit on the root)
- **Manual DI** — the `Application` owns a single `AppContainer`; the Activity and ViewModels pull dependencies from it
- **JVM unit tests** — version comparison, persisted-value parsing and both ViewModels, using in-memory fakes instead of a mocking framework
- **DataStore Preferences** persistence as the single source of truth behind a repository
- **Theme modes** — System / Light / Dark, with a circular reveal transition animation when switching
- **In-app language switching** — 简体中文 / English / Follow System, hot-swapped at runtime without recreating the Activity (per-language resource bundles disabled so switching always works)
- **Crash log manager** — uncaught and caught exceptions written to app-specific external storage, chaining to the system default handler; today's log can be shared from the Settings page
- **In-app updates** — checks the latest GitHub release once per day when Home is entered, or on demand from Settings; shows the release notes in a height-limited, scrollable dialog with Later / Update; downloads the APK in a foreground service (15s connect/read timeouts, progress notification, keeps downloading when the dialog is hidden), verifies the SHA-256 digest published by GitHub (aborting when it is missing or mismatched) before handing the APK to the system installer
- **Optimized build setup** — R8 + resource shrinking for release, signed release build, `arm64-v8a`-only ABI filter, deterministic APK naming

## Screens

| Screen | Contents |
| --- | --- |
| Home | Welcome and project overview cards, entry to Settings |
| Settings | Appearance (theme), Language, and About (version, share today's crash log, check for updates, GitHub link) |

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Kotlin 2.4.21 |
| UI | Jetpack Compose (BOM 2026.09.00) + Material 3 |
| Navigation | AndroidX Navigation3 1.2.1 (+ lifecycle-viewmodel-navigation3 2.11.0) |
| DI | Manual DI (app-level `AppContainer`) |
| Persistence | DataStore Preferences 1.3.0 |
| Serialization | kotlinx.serialization 1.12.0 |
| Lifecycle | androidx.lifecycle 2.11.0, activity-compose 1.13.0 |
| Testing | JUnit 4.13.2, kotlinx-coroutines-test 1.11.0 |
| Build | AGP 9.5.1, Gradle 9.9.0, refreshVersions 1.0.1 (local branch) |

## Project Structure

```
.
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/trustfenc/evilgodxu/
│   │   │   ├── data/                    # Data layer (single source of truth)
│   │   │   │   ├── repository/          #   Repository contracts + DataStore impls
│   │   │   │   └── settings/            #   Settings keys, enums & state
│   │   │   ├── log/                     # CrashLogManager (crash & exception logging)
│   │   │   ├── navigation/              # Navigation3 typed routes + NavHost
│   │   │   ├── screens/                 # Feature modules
│   │   │   │   ├── home/                #   Home (Screen + UiState + ViewModel)
│   │   │   │   │   ├── assembly/        #     Screen assembler (Scaffold + content pane)
│   │   │   │   │   └── component/       #     Home-only components (welcome/ about/)
│   │   │   │   └── settings/            #   Settings (Screen + UiState + ViewModel)
│   │   │   │       ├── assembly/        #     Screen assembler (Scaffold + content pane)
│   │   │   │       └── component/       #     Settings-only components (appearance/ language/ info/ dialog/)
│   │   │   ├── theme/                   # Material 3 color scheme & typography
│   │   │   ├── localization/            # In-app localization (LocalizationManager)
│   │   │   ├── permission/              # Permission state manager (runtime + special access)
│   │   │   ├── windowsize/              # Window size classes
│   │   │   ├── ui/                      # Cross-screen shared UI
│   │   │   │   ├── component/           #   Shared components (SectionCard/AppTopBar/ContentPane)
│   │   │   │   │   └── dialog/          #     UpdateDialog
│   │   │   │   └── icons/               #   Vector icons
│   │   │   ├── update/                  # Update check / download / install (GitHub API + foreground service)
│   │   │   ├── App.kt                   # Application entry
│   │   │   ├── AppContainer.kt          # Manual DI container (app-level)
│   │   │   ├── AppUiState.kt            # Immutable app-level UI state
│   │   │   ├── MainActivity.kt          # Single Activity
│   │   │   └── MainViewModel.kt         # Activity-scoped global UI state holder
│   │   └── res/                         # Resources (values / values-en)
│   ├── src/test/kotlin/…                # JVM unit tests (pure logic + ViewModels)
│   ├── lint.xml                         # Lint severity overrides & targeted ignores
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml               # Version catalog (dependencies)
│   └── wrapper/
├── AGENTS.md                            # Engineering conventions (build, tests, comments, lint, logging)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Architecture

### State management — MVVM + UDF

The app follows **MVVM with unidirectional data flow (UDF)**, forming a closed loop where state flows down and events flow up. State is split across two tiers:

- **App-level global state** — `MainViewModel` (Activity-scoped) aggregates app-wide UI state (`themeMode`, `language`, `version`) into the immutable `AppUiState`, exposed as a `StateFlow` and provided to the UI tree via the `LocalMainViewModel` `CompositionLocal`. Theme, localization and screen UIs all consume this single source; the UI layer never touches the data source directly.
- **Screen-level local state** — each screen's `{ScreenName}ViewModel` owns a `MutableStateFlow<{ScreenName}UiState>` as its single source of UI truth, exposed as an immutable `StateFlow` (e.g. `SettingsViewModel` + `SettingsUiState`). It is scoped to its navigation entry, so it is reclaimed when the page is popped.
- **Update checking** — kept in a separate Activity-scoped `AppUpdateViewModel` so `MainViewModel` stays focused: a once-per-day auto check runs when Home is entered, a manual check runs from Settings, results are delivered as non-replaying `SharedFlow` events, and the last auto-check date is persisted via `UpdateCheckRepository`.
- **Model** — the repository layer. `SettingsRepository` abstracts `DataStore Preferences`, which is the single source of truth for persisted settings; it is manually constructed and injected via `AppContainer` (swap-friendly for tests). User intents are received as plain methods (`setThemeMode`, `setLanguage`) and written back through the repository.

The typical flow: `DataStore → Repository → ViewModel → UiState → UI` for state, and the reverse path for events.

### UI composition — atomic decomposition + adaptive assemblies

Code is organized with a **modular pattern driven by window size classes**, mirroring Material's adaptive guidance:

- `{ScreenName}Screen.kt` — a thin screen entry that computes the window size class, hoists state and events, and hosts cross-form effects and page-level overlays. It contains **no layout code**.
- `{ScreenName}Assembly.kt` — owns screen-level scaffolding (Scaffold, top bar, content pane) and **assembles page components**. Form differences are a layout flag such as `ContentPane`'s max content width, not a second UI tree. Split into `compact/` and `expanded/` assemblies only when the two forms genuinely compose different blocks — and then both must consume the same `UiState` and the same event callbacks.
- `ui/component/` — cross-screen shared components (`SectionCard`, `AppTopBar`, `ContentPane`, plus `UpdateDialog` under `component/dialog/`), named by semantics. Components used by only one screen live under that screen's own `component/` subdirectory (e.g. `SettingsClickableItem`, `SingleChoiceDialog`). Dependencies point strictly downward: an assembly may compose components, but a component never composes back into an assembly, so the tree stays uncoupled.

DI is manual: `App` builds a fully-populated `AppContainer` (DataStore, repository, localization manager, version) at startup, and the Activity / ViewModels pull dependencies from it (ViewModels get constructor args via a `viewModelFactory`). Page-level ViewModels are obtained with `viewModel()`, scoped to their navigation entry through the Navigation3 `rememberViewModelStoreNavEntryDecorator()`. No framework or reflection.

Shared cross-feature code is hoisted to the top level (`data/`, `ui/`, `theme/`, `localization/`, `windowsize/`, `log/`, `update/`); code used by a single feature stays inside that feature module.

Engineering conventions — build verification, unit-test expectations, comment style, logging policy and Lint suppression — are defined in [`AGENTS.md`](AGENTS.md).

## Getting Started

### Prerequisites

- JDK 25
- Android Studio (latest stable recommended)
- Android SDK with API 37 (`compileSdk`)

### Build

```bash
git clone https://github.com/Evilgodxu/android-trustfenc.git
cd android-trustfenc

# Debug build
./gradlew assembleDebug

# Unit tests
./gradlew testDebugUnitTest

# Release build (requires signing config, see below)
./gradlew assembleRelease
```

APKs are emitted as `trustfenc-<versionName>-arm64.apk` under `app/build/outputs/apk/`.

### Release Signing

The release build reads signing credentials from `local.properties` in the project root:

```properties
KEYSTORE_PASSWORD=your_store_password
KEY_ALIAS=jh
KEY_PASSWORD=your_key_password
```

The keystore file is expected at `jh.keystore` in the project root (adjust `storeFile` in `app/build.gradle.kts` if needed). `jh.keystore` and `local.properties` are git-ignored — never commit them.

## Customizing the trustfenc

- **Rename the application / package**: update `namespace` and `applicationId` in `app/build.gradle.kts`, move the Kotlin sources under `app/src/main/kotlin/`, and update the manifest. Avoid a name collision with the existing `com.trustfenc.evilgodxu`.
- **App name**: edit `app_name` in `app/src/main/res/values/strings.xml`.
- **Theme colors**: edit `app/src/main/kotlin/.../theme/Color.kt`.
- **Supported ABIs**: adjust `ndk.abiFilters` in `app/build.gradle.kts` (currently `arm64-v8a`).
- **Add a new screen**: create a `screens/<name>/` feature module with its `UiState` + `ViewModel` + `assembly/<Name>Assembly.kt`, put single-screen components under its own `component/` (cross-screen ones go to `ui/component/`), register the route in `navigation/Screen.kt`, and add it to `AppNavHost`.

## License

[MIT](LICENSE) © 2026 Evilgodxu
