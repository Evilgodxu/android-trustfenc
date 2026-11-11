<div align="center">

# TrustFenc · 白篱

**为 Android 应用立一道基线白名单篱笆——锁住当前可信的应用，之后安装的应用会被自动卸载。**

[English](README.md) | **简体中文**

![License](https://img.shields.io/badge/license-AGPL--3.0-blue)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.21-purple)
![AGP](https://img.shields.io/badge/AGP-9.5.1-blue)
![Gradle](https://img.shields.io/badge/Gradle-9.9.0-blue)
![Compose BOM](https://img.shields.io/badge/Compose%20BOM-2026.09.00-blue)
![Shizuku](https://img.shields.io/badge/Shizuku-13.1.5-lightgrey)
![minSdk](https://img.shields.io/badge/minSdk-26-orange)
![targetSdk](https://img.shields.io/badge/targetSdk-37-orange)

</div>

白篱（TrustFenc）会把本机当前已安装的第三方应用锁进一份基线白名单。篱笆立起期间，前台守护服务持续监听应用安装，并借助 Shizuku 提供的 ADB shell 通道，卸载任何不在基线中的应用。

> 卸载不可撤销。只有确认当前应用集合就是你想保留的那一批时，再立起篱笆。

## 工作方式

1. **扫描** —— 同时通过 `PackageManager` 与 Shizuku 的 `pm list packages -3` 读取非系统应用，二者并集作为基线。shell 枚举失败或为空时直接中止修筑，不落盘残缺基线。
2. **锁定** —— 基线、守护标志、起始时间与计数器在同一次 DataStore 原子写入中落盘。
3. **监听** —— `FenceGuardService` 以 `specialUse` 前台服务常驻，启动时同步注册 `PACKAGE_ADDED`，并每 5 秒比对一次已安装列表，兜底部分 ROM 延迟或拦截广播的情况。
4. **清除** —— 不在基线中的应用先以 `pm uninstall` 卸载，失败后换 `--user 0` 重试一次，成功后把应用名、包名与时间记入控制台。

正常应用更新（`EXTRA_REPLACING`）、系统应用与系统更新的应用一律放行。开机、白篱自身更新、进程被回收、任务被划掉后，守护都会自动恢复。

## 特性

- **基线白名单** —— 扫描必须完整：shell 枚举出错即判修筑失败，避免基线上出现缺口、把后来的正常应用误判为越界
- **前台守护** —— 常驻通知展示基线规模与拦截次数；守护状态消失时服务自行停止
- **自动清除** —— 越界安装经 Shizuku 卸载并计数；广播与轮询巡检串行执行，避免并发重复卸载
- **状态持久化** —— 基线、守护标志与起始时间、拦截次数、最近 20 条记录存入 DataStore；开机、应用更新、进程重建、任务移除后均恢复守护
- **黑白控制台** —— FUI 终端风格界面：守护状态、`SHIZUKU` 与 `WATCH` 心跳行、三栏概况（基线 / 已拦截 / 守护时长）、最近拦截与四步工作说明；宽窗下同样这组卡片改为两列排布
- **修筑前引导** —— 首次修筑先申请通知权限与电池优化白名单，保证守护能在后台持续存活
- **设置页** —— 主题（跟随系统 / 浅色 / 深色，圆形揭示过渡）、应用内语言（简体中文 / English / 跟随系统，热切换无需重建 Activity）、版本检查、分享今日崩溃日志、GitHub 链接
- **应用内更新** —— 进入首页每日自动检查一次 GitHub 最新 Release，设置页亦可手动检查；前台服务下载并展示进度通知，校验 GitHub 公布的 SHA-256 摘要后交由系统安装器安装
- **崩溃日志** —— 未捕获异常与捕获异常写入应用专属外部目录，并链式调用系统默认处理器
- **发布构建优化** —— AGP 9.5 的 `optimization { enable = true }`、签名构建、不限制 ABI 的通用安装包、APK 命名固定

## 页面

| 页面 | 内容 |
| --- | --- |
| 首页（控制台） | 守护状态卡与唯一主操作（修筑 / 拆除）、Shizuku 与 WATCH 运行状态行、概况统计、最近拦截记录、工作方式四步说明、设置入口 |
| 设置 | 外观（主题）、语言、关于（版本与检查更新、今日崩溃日志、GitHub） |

## 环境要求

- **设备** —— Android 8.0+（`minSdk 26`）
- **运行时** —— 已安装并运行 [Shizuku](https://shizuku.rikka.app/)，且已向白篱授予 ADB 权限
- **构建** —— JDK 25、Android Studio（建议最新稳定版）、包含 API 37（`compileSdk`）的 Android SDK

## 快速开始

### 立起篱笆

1. 安装 Shizuku 并启动其服务（Android 11+ 可借助无线调试，或使用 Root）。
2. 安装并打开白篱。
3. 点击 **修筑篱笆**；按提示授予通知权限与电池优化白名单，再在 Shizuku 弹窗中允许白篱访问。
4. 等待扫描完成——状态卡切换为 `SYS.ONLINE` /「篱笆已立起」，并显示 `SHIZUKU READY` 与 `WATCH ONLINE`。
5. 之后任何不在基线中的新应用都会被自动卸载。

需要停止守护时点击 **拆除篱笆** 并确认。基线会被清空，此后安装的应用不再受拦截。

### 构建

```bash
git clone https://github.com/Evilgodxu/android-trustfenc.git
cd android-trustfenc

# 调试包
./gradlew assembleDebug

# 单元测试
./gradlew testDebugUnitTest

# Lint（须保持 0 errors / 0 warnings）
./gradlew lintDebug

# 发布包（需先配置签名，见下文）
./gradlew assembleRelease
```

APK 输出为 `app/build/outputs/apk/` 下的 `TrustFenc-<版本号>.apk`。

### 发布签名

Release 构建从项目根目录的 `local.properties` 读取签名凭据：

```properties
KEYSTORE_PASSWORD=你的签名库密码
KEY_ALIAS=jh
KEY_PASSWORD=你的别名密码
```

签名库文件默认位于项目根目录 `jh.keystore`（如需调整请修改 `app/build.gradle.kts` 中的 `storeFile`）。`jh.keystore` 与 `local.properties` 已被 git 忽略，请勿提交。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 语言 | Kotlin 2.4.21 |
| UI | Jetpack Compose（BOM 2026.09.00）+ Material 3，黑白 FUI 控制台 |
| 导航 | AndroidX Navigation3 1.2.1（+ lifecycle-viewmodel-navigation3 2.11.0） |
| 特权通道 | Shizuku 13.1.5（API + provider，AIDL `UserService`） |
| 依赖注入 | 手动 DI（Application 级 `AppContainer`） |
| 持久化 | DataStore Preferences 1.3.0 |
| 序列化 | kotlinx.serialization 1.12.0 |
| 生命周期 | androidx.lifecycle 2.11.0、activity-compose 1.13.0、core-ktx 1.19.1 |
| 测试 | JUnit 4.13.2、kotlinx-coroutines-test 1.11.0 |
| 构建 | AGP 9.5.1、Gradle 9.9.0、refreshVersions 1.0.1（本地分支） |

## 项目结构

```
.
├── app/
│   ├── src/main/
│   │   ├── aidl/com/trustfenc/evilgodxu/service/
│   │   │   └── ICommandService.aidl      # Shizuku UserService 命令通道
│   │   ├── kotlin/com/trustfenc/evilgodxu/
│   │   │   ├── data/
│   │   │   │   ├── fence/                # 篱笆状态、失败原因、已安装应用扫描
│   │   │   │   ├── repository/           # 篱笆 / 设置 / 更新检查仓库
│   │   │   │   ├── settings/             # 设置键、ThemeMode、AppLanguage
│   │   │   │   └── shizuku/              # ShizukuShell（状态机 + shell 通道）
│   │   │   ├── localization/             # 应用内语言切换
│   │   │   ├── log/                      # CrashLogManager
│   │   │   ├── navigation/               # Navigation3 类型安全路由 + 导航宿主
│   │   │   ├── permission/               # 运行时权限辅助
│   │   │   ├── screens/
│   │   │   │   ├── home/                 # 控制台（Screen + UiState + ViewModel）
│   │   │   │   │   ├── assembly/         #   Scaffold + 窄窗/宽窗控制台
│   │   │   │   │   └── component/        #   console/ 卡片、dialog/ 弹层
│   │   │   │   └── settings/             # 设置页（Screen + UiState + ViewModel）
│   │   │   │       ├── assembly/
│   │   │   │       └── component/        #   appearance/ language/ info/ dialog/
│   │   │   ├── service/                  # 守护前台服务、开机接收器、Shizuku UserService
│   │   │   ├── theme/                    # Material 3 配色 + 揭示过渡
│   │   │   ├── ui/
│   │   │   │   ├── component/            # ContentPane、FUI 组件（fui/）、UpdateDialog
│   │   │   │   └── icons/                # 内置 Material Symbols 矢量图标
│   │   │   ├── update/                   # GitHub 检查 / 下载 / 安装
│   │   │   ├── windowsize/               # 窗口尺寸类
│   │   │   ├── App.kt                    # Application 入口
│   │   │   ├── AppContainer.kt           # 手动 DI 容器
│   │   │   ├── AppUiState.kt             # 应用级 UI 状态
│   │   │   ├── MainActivity.kt           # 唯一 Activity
│   │   │   └── MainViewModel.kt          # Activity 作用域全局 UI 状态持有者
│   │   └── res/                          # values / values-en / drawable / xml
│   ├── src/test/kotlin/…                 # JVM 单元测试
│   ├── lint.xml                          # Lint 严重级别与定向排除
│   └── build.gradle.kts
├── docs/重构与构建配置参考.md            # 构建配置参考
├── gradle/
│   ├── libs.versions.toml                # 版本目录
│   └── wrapper/
├── AGENTS.md                             # 工程约定（构建、测试、注释、Lint、日志）
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 架构

### 状态管理 —— MVVM + 单向数据流

状态自上而下、事件自下而上，分两层：

- **应用级** —— `MainViewModel` 将主题、语言与版本聚合为 `AppUiState`；`AppUpdateViewModel` 持有更新阶段与弹窗显隐。二者均为 Activity 作用域，经 `LocalMainViewModel` / `LocalAppUpdateViewModel` 下发。
- **页面级** —— `HomeViewModel` 把 `FenceRepository.snapshot` 与页面级弹窗、提示状态合并为 `HomeUiState`；`SettingsViewModel` 持有弹窗显隐与主题揭示起点。作用域经 `rememberViewModelStoreNavEntryDecorator()` 绑定到导航条目。
- **数据层** —— `FenceRepository` 是唯一事实源：对外暴露只读 `StateFlow<FenceSnapshot>`，一次性失败经无回放的 `SharedFlow<FenceFailure>` 下发，写入统一落到 DataStore。`ShizukuShell` 发布 `ShizukuState`，`InstalledAppScanner` 合并 `PackageManager` 可见应用与 shell 包列表。

典型流向为 `DataStore / Shizuku → Repository → ViewModel → UiState → UI`，事件沿相反路径上行。

### 守护链路

```text
HomeScreen → HomeViewModel → FenceRepository ─┬→ InstalledAppScanner → ShizukuShell（pm list packages -3）
                                              ├→ FenceGuardService（修筑 / 停止）
                                              └→ DataStore（基线、计数、记录）

PACKAGE_ADDED → FenceGuardService → FenceRepository.evaluateInstalledPackage
             → 基线比对 → pm uninstall → 记账与计数
```

`FenceStatus` 是密封接口（`Idle` / `Building` / `Guarding(since)` / `Dismantling`），消费侧在编译期穷举全部状态。修筑与拆除由一把互斥锁串行，安装判定由另一把互斥锁串行；基线缓存在内存中，避免每条安装事件都读盘。

### UI 组合 —— 原子化拆分 + 自适应组装

- `{ScreenName}Screen.kt` —— 轻量页面入口：取 ViewModel、计算窗口尺寸类、承载跨形态副作用（权限与电池优化引导、每日更新检查）与页面级弹层，不含布局代码。
- `{ScreenName}Assembly.kt` —— 负责 Scaffold、FUI 顶栏与 `ContentPane`，并**组装页面组件**。控制台在窄窗单列与宽窗两列之间切换，两种形态消费同一份 `UiState` 与同一个事件回调，复用同一组卡片。
- `ui/component/` —— 跨页面复用件（`ContentPane`、`fui/` 下的 FUI 组件、`UpdateDialog`）；仅单页使用的组件留在该页面的 `component/` 下。
- 控制台与设置页共用 `FuiConsoleTheme`：只由明暗决定的黑白配色；系统界面与主题揭示动效仍由 Material 3 承载。

依赖注入为手动 DI：`App` 启动时构造成员齐全的 `AppContainer`，Activity 与 ViewModel 经 `viewModelFactory` 取依赖，无框架反射。

工程约定——构建验证、单元测试口径、注释规范、日志策略与 Lint 抑制——见 [`AGENTS.md`](AGENTS.md)。

## 已知限制

- **Shizuku 是硬前提。** 缺少它时修筑会失败，并以弹窗说明原因（未安装 / 未运行 / 未授权 / 扫描失败 / 服务被系统阻止）。
- **广播可能延迟。** 5 秒轮询通常能在数秒内补上，但越界应用仍可能短暂运行。
- **系统应用与正常更新不受影响。** 只判定新安装的第三方应用。
- **卸载不可逆。** 拆除篱笆只会清空基线，不会恢复任何应用。

## 定制

- **应用显示名** —— 修改 `app/src/main/res/values/strings.xml` 的 `app_name`（白篱）与 `values-en/strings.xml` 的 `app_name`（TrustFenc）。
- **修改包名** —— 同步修改 `app/build.gradle.kts` 的 `namespace` 与 `applicationId`，移动 `app/src/main/kotlin/` 下源码目录，更新 Manifest，并保持 Shizuku provider 的 authority 与 `${applicationId}` 绑定。
- **更新来源** —— 把 `AppUpdateChecker.LATEST_RELEASE_URL` 与 `AppInfo.kt` 中的 `GITHUB_URL` 指向你自己的仓库。
- **控制台配色** —— 黑白 FUI 颜色在 `ui/component/fui/FuiTheme.kt`；Material 3 配色在 `theme/Color.kt`。
- **新增页面** —— 在 `screens/<name>/` 下创建页面模块（`UiState` + `ViewModel` + `assembly/<Name>Assembly.kt`），在 `navigation/Screen.kt` 注册路由并加入 `AppNavHost`。

## License

[GNU Affero General Public License v3.0](LICENSE) © 2026 Evilgodxu
