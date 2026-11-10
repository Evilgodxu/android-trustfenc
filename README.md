<div align="center">

# TrustFenc · 白篱

**An allowlist fence for Android apps — lock in the apps you trust, and anything installed later is removed automatically.**

**English** | [简体中文](README.zh-CN.md)

![License](https://img.shields.io/badge/license-MIT-green)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.21-purple)
![AGP](https://img.shields.io/badge/AGP-9.5.1-blue)
![Gradle](https://img.shields.io/badge/Gradle-9.9.0-blue)
![Compose BOM](https://img.shields.io/badge/Compose%20BOM-2026.09.00-blue)
![Shizuku](https://img.shields.io/badge/Shizuku-13.1.5-lightgrey)
![minSdk](https://img.shields.io/badge/minSdk-26-orange)
![targetSdk](https://img.shields.io/badge/targetSdk-37-orange)

</div>

TrustFenc (白篱, "white fence") locks the current set of installed third-party apps into a baseline allowlist. While the fence is up, a foreground guard service watches for package installs and uninstalls anything that is not in the baseline, using the ADB shell channel provided by Shizuku.

> Removing an app is irreversible. Raise the fence only when the current app set is the one you want to keep.

## How it works

1. **Scan** — non-system apps are read through both `PackageManager` and Shizuku's `pm list packages -3`; the union becomes the baseline. A failed or empty shell scan aborts the build instead of saving a partial list.
2. **Lock** — baseline, guarding flag, start timestamp and counters are written to DataStore in a single atomic edit.
3. **Watch** — `FenceGuardService` runs as a `specialUse` foreground service, registers `PACKAGE_ADDED` synchronously on start, and re-checks the installed list every 5 seconds as a fallback for ROMs that delay broadcasts.
4. **Remove** — an app outside the baseline is uninstalled with `pm uninstall`, retried once with `--user 0`, then recorded (label, package, time) in the console.

Normal app updates (`EXTRA_REPLACING`), system apps and updated system apps are ignored. The guard is restored after reboot, after TrustFenc itself is updated, when the process is recreated, and when the task is swiped away.

## Features

- **Baseline allowlist** — a complete scan is mandatory; shell enumeration errors fail the build rather than leaving gaps that later look like intruders
- **Foreground guard** — ongoing notification shows baseline size and block count; the service stops itself if the guarding state disappears
- **Automatic removal** — out-of-baseline installs are uninstalled through Shizuku and counted; broadcasts and the watchdog are serialized so one package is not uninstalled concurrently
- **Durable state** — baseline, guarding flag/since, block count and the last 20 records live in DataStore; the guard comes back on boot, app update, process death and task removal
- **Black-and-white console** — FUI-style console with guard status, `SHIZUKU` and `WATCH` heartbeat rows, a three-stat overview (baseline / blocked / uptime), recent blocks and a four-step guide; wide windows lay the same cards out in two columns
- **Fence-aware onboarding** — before raising, the app asks for notification permission and battery-optimization exemption so the guard can survive in the background
- **Settings** — theme (System / Light / Dark with a circular reveal), in-app language (简体中文 / English / System, hot-swapped without recreating the Activity), version check, share today's crash log, GitHub link
- **In-app updates** — checks the latest GitHub release once per day on Home entry, or on demand from Settings; downloads in a foreground service with progress notification, verifies the published SHA-256 digest, then hands the APK to the system installer
- **Crash logging** — uncaught and caught exceptions written to app-specific external storage and chained to the system default handler
- **Optimized release build** — AGP 9.5 `optimization { enable = true }`, signed release, universal APK without an ABI filter, deterministic APK naming

## Screens

| Screen | Contents |
| --- | --- |
| Home (console) | Guard status card with the single primary action (raise / take down), Shizuku and WATCH runtime rows, overview stats, recent blocked apps, how-it-works steps, entry to Settings |
| Settings | Appearance (theme), Language, About (version + update check, today's crash log, GitHub) |

## Requirements

- **Device** — Android 8.0+ (`minSdk 26`)
- **Runtime** — [Shizuku](https://shizuku.rikka.app/) installed and running, with ADB access granted to TrustFenc
- **Build** — JDK 25, Android Studio (latest stable recommended), Android SDK with API 37 (`compileSdk`)

## Getting Started

### Raise the fence

1. Install Shizuku and start its service (wireless debugging on Android 11+, or root).
2. Install TrustFenc and open it.
3. Tap **Raise fence**; grant notification permission and the battery-optimization exemption when asked, then allow ADB access in the Shizuku prompt.
4. Wait for the scan to finish — the status card switches to `SYS.ONLINE` / "The fence is up" and the console shows `SHIZUKU READY` and `WATCH ONLINE`.
5. Any newly installed app that is not in the baseline is removed automatically.

To stop guarding, tap **Take down** and confirm. The baseline is cleared, so apps installed afterwards are no longer blocked.

### Build

```bash
git clone https://github.com/Evilgodxu/android-trustfenc.git
cd android-trustfenc

# Debug build
./gradlew assembleDebug

# Unit tests
./gradlew testDebugUnitTest

# Lint (must stay at 0 errors / 0 warnings)
./gradlew lintDebug

# Release build (requires signing config, see below)
./gradlew assembleRelease
```

APKs are emitted as `TrustFenc-<versionName>.apk` under `app/build/outputs/apk/`.

### Release Signing

The release build reads signing credentials from `local.properties` in the project root:

```properties
KEYSTORE_PASSWORD=your_store_password
KEY_ALIAS=jh
KEY_PASSWORD=your_key_password
```

The keystore file is expected at `jh.keystore` in the project root (adjust `storeFile` in `app/build.gradle.kts` if needed). `jh.keystore` and `local.properties` are git-ignored — never commit them.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Kotlin 2.4.21 |
| UI | Jetpack Compose (BOM 2026.09.00) + Material 3, black-and-white FUI console |
| Navigation | AndroidX Navigation3 1.2.1 (+ lifecycle-viewmodel-navigation3 2.11.0) |
| Privileged access | Shizuku 13.1.5 (API + provider, AIDL `UserService`) |
| DI | Manual DI (app-level `AppContainer`) |
| Persistence | DataStore Preferences 1.3.0 |
| Serialization | kotlinx.serialization 1.12.0 |
| Lifecycle | androidx.lifecycle 2.11.0, activity-compose 1.13.0, core-ktx 1.19.1 |
| Testing | JUnit 4.13.2, kotlinx-coroutines-test 1.11.0 |
| Build | AGP 9.5.1, Gradle 9.9.0, refreshVersions 1.0.1 (local branch) |

## Project Structure

```
.
├── app/
│   ├── src/main/
│   │   ├── aidl/com/trustfenc/evilgodxu/service/
│   │   │   └── ICommandService.aidl      # Shizuku UserService command channel
│   │   ├── kotlin/com/trustfenc/evilgodxu/
│   │   │   ├── data/
│   │   │   │   ├── fence/                # Fence states, failures, installed-app scanner
│   │   │   │   ├── repository/           # Fence / settings / update-check repositories
│   │   │   │   ├── settings/             # Settings keys, ThemeMode, AppLanguage
│   │   │   │   └── shizuku/              # ShizukuShell (state machine + shell channel)
│   │   │   ├── localization/             # Runtime language switching
│   │   │   ├── log/                      # CrashLogManager
│   │   │   ├── navigation/               # Navigation3 typed routes + nav host
│   │   │   ├── permission/               # Runtime permission helper
│   │   │   ├── screens/
│   │   │   │   ├── home/                 # Console (Screen + UiState + ViewModel)
│   │   │   │   │   ├── assembly/         #   Scaffold + compact/wide console
│   │   │   │   │   └── component/        #   console/ cards, dialog/ overlays
│   │   │   │   └── settings/             # Settings (Screen + UiState + ViewModel)
│   │   │   │       ├── assembly/
│   │   │   │       └── component/        #   appearance/ language/ info/ dialog/
│   │   │   ├── service/                  # Guard FGS, boot receiver, Shizuku UserService
│   │   │   ├── theme/                    # Material 3 scheme + reveal transition
│   │   │   ├── ui/
│   │   │   │   ├── component/            # ContentPane, FUI kit (fui/), UpdateDialog
│   │   │   │   └── icons/                # Local Material Symbols vectors
│   │   │   ├── update/                   # GitHub check / download / install
│   │   │   ├── windowsize/               # Window size classes
│   │   │   ├── App.kt                    # Application entry
│   │   │   ├── AppContainer.kt           # Manual DI container
│   │   │   ├── AppUiState.kt             # App-level UI state
│   │   │   ├── MainActivity.kt           # Single Activity
│   │   │   └── MainViewModel.kt          # Activity-scoped global UI state holder
│   │   └── res/                          # values / values-en / drawable / xml
│   ├── src/test/kotlin/…                 # JVM unit tests
│   ├── lint.xml                          # Lint severity overrides & targeted ignores
│   └── build.gradle.kts
├── docs/重构与构建配置参考.md            # Build configuration notes (Chinese)
├── gradle/
│   ├── libs.versions.toml                # Version catalog
│   └── wrapper/
├── AGENTS.md                             # Engineering conventions (build, tests, comments, lint, logging)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Architecture

### State management — MVVM + UDF

State flows down and events flow up, in two tiers:

- **App-level** — `MainViewModel` aggregates theme, language and version into `AppUiState`; `AppUpdateViewModel` owns the update phase and dialog visibility. Both are Activity-scoped and delivered through `LocalMainViewModel` / `LocalAppUpdateViewModel`.
- **Screen-level** — `HomeViewModel` combines `FenceRepository.snapshot` with page-only dialog and notice state into `HomeUiState`; `SettingsViewModel` holds dialog visibility and the theme-reveal origin. They are scoped to their Navigation3 entries via `rememberViewModelStoreNavEntryDecorator()`.
- **Data** — `FenceRepository` is the single source of truth. It exposes a read-only `StateFlow<FenceSnapshot>` plus a non-replaying `SharedFlow<FenceFailure>` for one-shot notices, and writes through DataStore. `ShizukuShell` publishes `ShizukuState`, while `InstalledAppScanner` merges `PackageManager` visibility with the shell package list.

The typical flow is `DataStore / Shizuku → Repository → ViewModel → UiState → UI` for state, with events travelling the other way.

### Guard chain

```text
HomeScreen → HomeViewModel → FenceRepository ─┬→ InstalledAppScanner → ShizukuShell (pm list packages -3)
                                              ├→ FenceGuardService (raise / stop)
                                              └→ DataStore (baseline, counters, records)

PACKAGE_ADDED → FenceGuardService → FenceRepository.evaluateInstalledPackage
             → baseline check → pm uninstall → record + count
```

`FenceStatus` is a sealed interface (`Idle` / `Building` / `Guarding(since)` / `Dismantling`), so consumers handle every state at compile time. Build and dismantle transitions are guarded by a mutex; install evaluations are serialized by a second mutex, and the baseline is cached in memory so each install event does not hit the disk.

### UI composition — atomic decomposition + adaptive assemblies

- `{ScreenName}Screen.kt` — thin entry: obtains the ViewModel, computes the window size class, hosts cross-form effects (permission and battery-optimization prompts, daily update check) and page-level dialogs. No layout code.
- `{ScreenName}Assembly.kt` — owns the Scaffold, FUI top bar and `ContentPane`, and **assembles page components**. The console switches between a compact single column and a wide two-column layout from the same `UiState` and the same event callback; both forms reuse the same cards.
- `ui/component/` — cross-screen pieces (`ContentPane`, the FUI kit under `fui/`, `UpdateDialog`). Screen-only components stay under that screen's `component/`.
- The console and Settings share `FuiConsoleTheme`, a black-and-white palette driven only by light/dark; Material 3 still owns system surfaces and the theme-reveal animation.

DI is manual: `App` builds a fully-populated `AppContainer`, and the Activity / ViewModels pull dependencies from it through `viewModelFactory`. No framework or reflection.

Engineering conventions — build verification, unit-test expectations, comment style, logging policy and Lint suppression — are defined in [`AGENTS.md`](AGENTS.md).

## Limitations

- **Shizuku is mandatory.** Without it, raising fails with a dialog explaining the missing precondition (not installed / not running / permission denied / scan failed / service blocked).
- **Broadcasts can be delayed.** The 5-second watchdog usually catches an intruder within a few seconds, but an app can run briefly before it is removed.
- **System apps and normal updates are never touched.** Only newly installed third-party packages are evaluated.
- **Removal is destructive and not reversible.** Taking the fence down clears the baseline instead of restoring anything.

## Customizing

- **App name** — `app_name` in `app/src/main/res/values/strings.xml` (白篱) and `values-en/strings.xml` (TrustFenc).
- **Rename the package** — update `namespace` and `applicationId` in `app/build.gradle.kts`, move the Kotlin sources under `app/src/main/kotlin/`, update the manifest, and keep the Shizuku provider authority tied to `${applicationId}`.
- **Update source** — point `AppUpdateChecker.LATEST_RELEASE_URL` and `GITHUB_URL` in `AppInfo.kt` at your own repository.
- **Console palette** — edit `ui/component/fui/FuiTheme.kt` for the black-and-white FUI colors; edit `theme/Color.kt` for the Material 3 scheme.
- **Add a new screen** — create a `screens/<name>/` module with its `UiState` + `ViewModel` + `assembly/<Name>Assembly.kt`, register the route in `navigation/Screen.kt`, and add it to `AppNavHost`.

## License

MIT © 2026 Evilgodxu
