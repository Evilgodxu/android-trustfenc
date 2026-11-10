<div align="center">

# trustfenc

**一个开箱即用的现代化 Android 应用模板，基于 Jetpack Compose、Material 3、MVVM 与 Navigation3 构建。**

[English](README.md) | **简体中文**

![License](https://img.shields.io/badge/license-MIT-green)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.21-purple)
![AGP](https://img.shields.io/badge/AGP-9.5.1-blue)
![Gradle](https://img.shields.io/badge/Gradle-9.9.0-blue)
![Compose BOM](https://img.shields.io/badge/Compose%20BOM-2026.09.00-blue)
![minSdk](https://img.shields.io/badge/minSdk-34-orange)
![targetSdk](https://img.shields.io/badge/targetSdk-37-orange)

</div>

**trustfenc** 是一个可直接上手的 Android 应用起点工程。工程骨架与常用基础设施已搭建完毕，你可以专注于业务功能，而非重复搭建样板代码。

## 特性

- **单 Activity 架构**：基于 Jetpack Compose + Material 3，支持边到边（edge-to-edge）渲染，并按屏幕方向显隐系统栏
- **MVVM + 单向数据流（UDF）**：应用级状态（主题 / 语言 / 版本）由 Activity 作用域的 `MainViewModel` 聚合，经 `CompositionLocal` 供界面树消费；各页面另以不可变 `UiState` 经 `StateFlow` 暴露。事件自下而上、状态自上而下
- **原子化 UI 拆分 + 响应式布局**：窗口尺寸类在页面入口计算一次并作为参数下传；形态差异用布局标志位（宽窗下的内容行宽上限）表达，而不是第二套 UI 树，因此各形态共用同一份状态与同一套组件
- **Navigation3 导航**：类型安全路由 + 显式返回栈（根页面支持双击返回退出）
- **手动依赖注入**：`Application` 持有一站式 `AppContainer`，Activity 与 ViewModel 从容器取依赖
- **单元测试**：版本比较、持久化值解析与两个 ViewModel，用内存 fake 替代 mock 框架
- **DataStore Preferences 持久化**：作为仓库层背后的唯一数据源（single source of truth）
- **主题模式**：跟随系统 / 浅色 / 深色，切换时带圆形扩散过渡动效
- **应用内多语言**：简体中文 / English / 跟随系统，运行时热切换、无需重建 Activity（已禁用按语言分包，保证切换始终生效）
- **崩溃日志管理**：未捕获异常与捕获异常写入应用专属外部目录，并链式调用系统默认处理器；设置页可一键分享今日日志
- **应用内更新**：进入首页每日自动检查一次 GitHub 最新 Release，设置页亦可手动检查；发现新版本弹出限高可滚动的更新内容对话框（稍后 / 更新）；安装包由前台服务下载（连接/读取 15 秒超时、进度通知，关闭对话框不中断下载），完成后比对 GitHub 公布的 SHA-256 摘要（缺失或不匹配即中止），通过后交由系统安装器安装
- **构建优化**：Release 启用 R8 + 资源压缩、签名构建，仅打 `arm64-v8a` ABI，APK 输出命名固定

## 页面

| 页面 | 内容 |
| --- | --- |
| 首页 | 欢迎与项目简介卡片，设置入口 |
| 设置 | 外观（主题）、语言、关于（版本、分享今日崩溃日志、检查更新、GitHub 链接） |

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 语言 | Kotlin 2.4.21 |
| UI | Jetpack Compose（BOM 2026.09.00）+ Material 3 |
| 导航 | AndroidX Navigation3 1.2.1（+ lifecycle-viewmodel-navigation3 2.11.0） |
| 依赖注入 | 手动 DI（Application 级 `AppContainer`） |
| 持久化 | DataStore Preferences 1.3.0 |
| 序列化 | kotlinx.serialization 1.12.0 |
| 生命周期 | androidx.lifecycle 2.11.0、activity-compose 1.13.0 |
| 测试 | JUnit 4.13.2、kotlinx-coroutines-test 1.11.0 |
| 构建 | AGP 9.5.1、Gradle 9.9.0、refreshVersions 1.0.1（本地分支） |

## 项目结构

```
.
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/trustfenc/evilgodxu/
│   │   │   ├── data/                    # 数据层（唯一数据源）
│   │   │   │   ├── repository/          #   仓库契约 + DataStore 实现
│   │   │   │   └── settings/            #   设置键、枚举与状态
│   │   │   ├── log/                     # 崩溃与异常日志（CrashLogManager）
│   │   │   ├── navigation/              # Navigation3 类型安全路由 + 导航宿主
│   │   │   ├── screens/                 # 页面模块
│   │   │   │   ├── home/                #   首页（Screen + UiState + ViewModel）
│   │   │   │   │   ├── assembly/        #     组装器（Scaffold + 内容区）
│   │   │   │   │   └── component/       #     首页专用组件（welcome/ about/）
│   │   │   │   └── settings/            #   设置页（Screen + UiState + ViewModel）
│   │   │   │       ├── assembly/        #     组装器（Scaffold + 内容区）
│   │   │   │       └── component/       #     设置页专用组件（appearance/ language/ info/ dialog/）
│   │   │   ├── theme/                   # Material 3 配色与字体
│   │   │   ├── localization/            # 应用内多语言管理（LocalizationManager）
│   │   │   ├── permission/              # 权限状态管理器（运行时权限与特殊权限统一出口）
│   │   │   ├── windowsize/              # 窗口尺寸类
│   │   │   ├── ui/                      # 跨页面复用 UI
│   │   │   │   ├── component/           #   共享组件（SectionCard/AppTopBar/ContentPane）
│   │   │   │   │   └── dialog/          #     UpdateDialog
│   │   │   │   └── icons/               #   矢量图标
│   │   │   ├── update/                  # 版本检查 / 下载 / 安装（GitHub API + 前台服务）
│   │   │   ├── App.kt                   # Application 入口
│   │   │   ├── AppContainer.kt          # 手动 DI 容器（Application 级）
│   │   │   ├── AppUiState.kt            # 不可变的应用级 UI 状态
│   │   │   ├── MainActivity.kt          # 唯一 Activity
│   │   │   └── MainViewModel.kt         # Activity 作用域全局 UI 状态持有者
│   │   └── res/                         # 资源（values / values-en）
│   ├── src/test/kotlin/…                # JVM 单元测试（纯逻辑 + ViewModel）
│   ├── lint.xml                         # Lint 严重级别与定向排除
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml               # 版本目录（依赖管理）
│   └── wrapper/
├── AGENTS.md                            # 工程约定（构建、测试、注释、Lint、日志）
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 架构

### 状态管理 —— MVVM + 单向数据流

应用采用 **MVVM + 单向数据流（UDF）**，状态自上而下流动、事件自下而上传递，形成闭环。状态分两层管理：

- **应用级全局状态**：`MainViewModel`（Activity 作用域）将应用级 UI 状态（`themeMode`、`language`、`version`）聚合进不可变的 `AppUiState`，以 `StateFlow` 暴露，并通过 `LocalMainViewModel` `CompositionLocal` 提供给界面树；主题、本地化与各页面 UI 共同消费这一唯一状态源，UI 层不直连数据源。
- **页面级局部状态**：每个页面的 `{ScreenName}ViewModel` 持有 `MutableStateFlow<{ScreenName}UiState>` 作为 UI 唯一状态源，对外暴露不可变 `StateFlow`（如 `SettingsViewModel` + `SettingsUiState`）；其作用域绑定导航条目，页面出栈即回收。
- **更新检查**：独立为 Activity 作用域的 `AppUpdateViewModel`，避免 `MainViewModel` 膨胀：进入首页触发每日一次的自动检查、设置页可手动检查，结果经无回放的 `SharedFlow` 一次性下发，最近一次自动检查日期经 `UpdateCheckRepository` 持久化。
- **Model（仓库层）**：`SettingsRepository` 抽象了 `DataStore Preferences`，持久化设置以 DataStore 为唯一事实源，经 `AppContainer` 手动构造注入便于测试替换；用户意图以普通方法接收（`setThemeMode`、`setLanguage`），经仓库写回。

典型流向：`DataStore → Repository → ViewModel → UiState → UI`（状态），事件则沿相反路径上行。

### UI 组合 —— 原子化拆分 + 自适应组装

代码遵循 **按窗口尺寸类拆分的模块化模式**（对齐 Material 自适应指南）：

- `{ScreenName}Screen.kt` —— 轻量页面入口：计算窗口尺寸类、提升（hoist）状态与事件，承载跨形态副作用与页面级弹层，**不含布局代码**。
- `{ScreenName}Assembly.kt` —— 负责页面级布局骨架（Scaffold、顶栏、内容区）并**组装页面组件**。形态差异是布局标志位（如 `ContentPane` 的行宽上限），而不是第二套 UI 树。只有当两种形态的功能区块结构确实不同时，才拆成 `compact/` 与 `expanded/` 两套组装器，且二者必须消费同一份 `UiState` 与同一套事件回调。
- `ui/component/` —— 跨页面共享组件（`SectionCard`、`AppTopBar`、`ContentPane`，以及 `component/dialog/` 下的 `UpdateDialog`），按语义命名；仅单页使用的组件下沉到该页面自己的 `component/` 子目录（如 `SettingsClickableItem`、`SingleChoiceDialog`）。依赖严格向下：组装器可组合组件，组件绝不反向组合进组装器，组件树因此保持解耦。

依赖注入采用手动 DI：`App` 在启动时构造成员齐全的 `AppContainer`（DataStore、仓库、语言管理器、版本号），`MainActivity` 与 ViewModel 自容器取依赖（ViewModel 经 `viewModelFactory` 注入构造参数）；页面级 ViewModel 经 `viewModel()` 获取，作用域由 Navigation3 的 `rememberViewModelStoreNavEntryDecorator()` 绑定到导航条目，无框架反射。

通用能力被多个页面复用时上提至顶层（`data/`、`ui/`、`theme/`、`localization/`、`windowsize/`、`log/`、`update/`）；仅单页使用的代码保留在页面模块内。

工程约定——构建验证、单元测试口径、注释规范、日志策略与 Lint 抑制——见 [`AGENTS.md`](AGENTS.md)。

## 快速开始

### 环境要求

- JDK 25
- Android Studio（建议最新稳定版）
- 包含 API 37（`compileSdk`）的 Android SDK

### 构建

```bash
git clone https://github.com/Evilgodxu/android-trustfenc.git
cd android-trustfenc

# 调试包
./gradlew assembleDebug

# 单元测试
./gradlew testDebugUnitTest

# 发布包（需先配置签名，见下文）
./gradlew assembleRelease
```

APK 输出为 `app/build/outputs/apk/` 下的 `trustfenc-<版本号>-arm64.apk`。

### 发布签名

Release 构建从项目根目录的 `local.properties` 读取签名凭据：

```properties
KEYSTORE_PASSWORD=你的签名库密码
KEY_ALIAS=jh
KEY_PASSWORD=你的别名密码
```

签名库文件默认位于项目根目录 `jh.keystore`（如需调整请修改 `app/build.gradle.kts` 中的 `storeFile`）。`jh.keystore` 与 `local.properties` 已被 git 忽略，请勿提交。

## 定制模板

- **修改应用名 / 包名**：同步修改 `app/build.gradle.kts` 中的 `namespace` 与 `applicationId`，移动 `app/src/main/kotlin/` 下对应源码目录，并更新 Manifest，避免与现有 `com.trustfenc.evilgodxu` 冲突
- **应用显示名**：修改 `app/src/main/res/values/strings.xml` 中的 `app_name`
- **主题配色**：修改 `app/src/main/kotlin/.../theme/Color.kt`
- **支持的 ABI**：调整 `app/build.gradle.kts` 中的 `ndk.abiFilters`（当前仅 `arm64-v8a`）
- **新增页面**：在 `screens/<name>/` 下创建页面模块（`UiState` + `ViewModel` + `assembly/<Name>Assembly.kt`），仅单页使用的组件放该页面自己的 `component/`（跨页面复用的上提到 `ui/component/`），在 `navigation/Screen.kt` 注册路由，并加入 `AppNavHost`

## License

[MIT](LICENSE) © 2026 Evilgodxu
