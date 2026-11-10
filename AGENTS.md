## 技术栈与依赖

- 单 Activity + Jetpack Compose（Material 3）+ Navigation3，状态管理为 MVVM + 单向数据流。
- 依赖版本统一写在 `gradle/libs.versions.toml`，用 `./gradlew refreshVersions` 巡检；不手写单个依赖版本。
- 巡检插件从本机仓库（`~/.m2`）消费，`settings.gradle.kts` 里的版本号须与 `../refreshVersions/plugins/version.txt` 一致；改动该源码分支后先在其 `plugins/` 目录执行 `./gradlew publishToMavenLocal`，再巡检。
- 只接受正式版：`settings.gradle.kts` 的 `rejectVersionIf` 已滤除带连字符的预发布标签，升级时不要绕过该过滤。
- 取舍顺序：语言标准库 → Compose / AndroidX 原生能力 → 按源码等价自实现 → 引入依赖。
- 调用链路取最短：不设只做转发的中间层；拆分以抑制碎片化为界，聚合以上帝类为界。
- Release 优化统一走 AGP 9.3+ 的 `optimization { enable = true }`：它同时开启代码优化、优化型资源缩减并内置等价于 `proguard-android-optimize.txt` 的默认规则，不要再写回 `isMinifyEnabled` / `isShrinkResources` / `getDefaultProguardFile`。

## 架构与状态

```text
DataStore → Repository → ViewModel → UiState → UI      状态自上而下
UI → ViewModel → Repository → DataStore                事件自下而上
```

- **应用级状态**（主题 / 语言 / 版本）由 Activity 作用域的 `MainViewModel` 聚合为 `AppUiState`，经 `LocalMainViewModel` 下发；主题、本地化与各页面共用这一份。
- **页面级状态**由 `{Screen}ViewModel` 持有 `MutableStateFlow<{Screen}UiState>`，对外只暴露不可变 `StateFlow`；作用域由 Navigation3 的 `rememberViewModelStoreNavEntryDecorator()` 绑定到导航条目。
- 状态变化用框架原生机制响应：`StateFlow`、`collectAsStateWithLifecycle()`、`CompositionLocal`。禁止把状态包成自定义回调层层转发，也禁止为同一份状态另存副本。
- 依赖注入是**手动 DI**：`App` 构造一站式 `AppContainer`，Activity 与 ViewModel 经 `viewModelFactory` 取构造参数。不引入 Hilt / Koin / Dagger。
- ViewModel 只依赖仓库接口，不直接触碰 DataStore、文件或网络。

## 数据持久化

- DataStore Preferences 是唯一数据源；不引入 SharedPreferences，也不在内存里另存设置副本。
- 写入一律 `suspend` + `dataStore.edit {}`；同一 `edit` 内修改多个键以保证原子性。
- 新增设置项：键放 `SettingsKeys`，枚举以稳定字符串落盘，读取时未知值回落到默认项而非抛错（参照 `ThemeMode.fromValue`）。

## 单元测试

- 位置 `app/src/test/kotlin/`，包名与主源码一致。
- 覆盖对象：纯逻辑（版本比较、持久化值解析）与状态持有者（ViewModel 的写入与回流）；不测像素级布局。
- 用内存 fake 实现仓库接口替代 mock 框架，不引入 Mockito / MockK。
- 网络与磁盘不参与单测；时间与调度用 `kotlinx-coroutines-test`：`Dispatchers.setMain(StandardTestDispatcher())` 配合 `advanceUntilIdle()`，实验性 API 显式标注 `@OptIn`。
- 测试方法名用反引号中文描述行为（如 `` `写入主题经仓库回流更新状态` ``），断言对外可见的状态，不断言私有实现。

## 构建验证

- 改动后至少验证 `:app:assembleDebug`、`:app:testDebugUnitTest`、`:app:lintDebug` 三项。
- lint 必须保持 **0 errors / 0 warnings**；新增问题当轮修掉，不积压、不用基线文件兜底。
- 覆盖 UI 与资源变更时同时验证 `:app:assembleRelease`（R8 优化与资源缩减路径与 debug 不同）。
- Kotlin 已开启 `allWarningsAsErrors`，警告即构建失败；`app/lint.xml` 已把 `Deprecated` 与 `UnsafeOptIn*` 提级为 error，弃用 API 须迁移到等价接口而非抑制。

## 注释

- 解释意图与设计决策，不复述代码行为。
- 代码变更时同步更新，不留过期注释。
- 不写内容溯源与背景交代（「曾经」「来自某版本」这类），历史交给 git。

## 删除与重命名

- 删除须指定精确路径，逐条列举、分批执行；严禁通配符匹配或递归大范围删除。
- 删除前确认存在可回退存档：已纳入 git 即可回退，未跟踪文件先备份。
- 文件改名用 `git mv`，保留历史；改名的同时清掉旧文件名与旧符号的全部引用。

## 日志：按需埋点，不做防御性埋点

日志不是资产，是为回答某个具体问题临时支起来的脚手架——**问题答完就撤**。判据只有一条：
**它要回答的那个问题是否还存在**；与级别无关，异常级的留痕同样适用。

本模板只保留崩溃与已捕获异常的留痕（`CrashLogManager`），服务的问题是「用户报问题时能拿到现场」。
新增留痕之前先过这几条：

- **先问它回答哪个具体问题**，以及这个问题现在是否存在。答不上来就不写。
- **「怕出错」不是理由**：防御性留痕会在没人看的前提下持续输出，最后只剩噪音。真出了错按现场补，
  比预先猜准得多——猜错的那部分就是纯负担。
- **「换个环境才出现」也不是理由**：只在特定机型、特定格式上触发，只说明问题难查，不说明该预先埋点。
- **不为守卫与设计内降级留痕**：参数校验、多方案逐个降级这类「设计上就会走到」的分支不记；
  只有「不该发生却发生了」才值得留痕。
- **同一失败只留一处**：下层记完又向上抛的，上层再记一遍就是重复条目——记在最终处置它的那一层。
- **留在读得到的地方**：能进分享日志的统一通道（`CrashLogManager`）就不要直写 `android.util.Log`
  ——用户报问题时读不到，等于没留。`CrashLogManager` 自身落盘失败时降级到 `Log` 是唯一例外。
- **文案带定位上下文**：写清设备、路径、URI 这类「换个环境就不同」的量，不写
  `Download failed` 这种无上下文的英文串。

要求：

- **撤就撤干净**：留痕连同只为它存在的死代码一起撤——记账计数器、失败文案、只被它调用的辅助函数、
  仅为它引入的依赖。留一半比不留更糟。
- **每轮改动复核留痕是否仍对应现存问题**，过期即撤（与「不留过期注释」同一条口径）。

## Lint 未使用资源（UnusedResources）的判断

`UnusedResources` 对**「被依赖库按资源名解析」的资源**会误报。这类资源的引用不在应用源码里，
而在依赖库自身的 `values.xml` 中，因此**「全仓库文本搜索无引用」不足以判定资源已废弃**。

删除任何被判为未使用的资源前，须按顺序排查：

1. **查依赖库是否声明了同名资源**——遍历本地 AAR 缓存，看是否有库打包了同名文件。
2. **查库的 `values.xml` 是否按名引用它**——这一步才是关键。库常以
   `<drawable name="X">@drawable/<资源名></drawable>` 的形式间接引用。
3. **确认是否为本应用的覆盖版本**——比对字节哈希。哈希不同说明是自定义覆盖，不是冗余副本，
   删除等于放弃自定义外观。
4. 确认是误报后，用**定向 `lint.xml`** 排除（见下节），不要删除资源，也不要 `disable` 整个检查。

## Lint 抑制的写法

优先选作用域最小、能自证理由的方式：

| 场景 | 做法 |
|---|---|
| 单个 XML 元素可标注 | 该元素上 `tools:ignore="检查名"`（根元素补 `xmlns:tools`） |
| 单个二进制资源 / 需要限定路径 | `app/lint.xml` 内 `<ignore path="...">`，仅排除该文件 |
| 检查与源码位置无关（取决于 ABI 等构建配置） | `build.gradle.kts` 的 `lint { disable += ... }` |

要求：

- **定向优先于整体**：能只排除一个文件就不要 `disable` 整个检查——整体关闭会连带放过真实问题。
- **每处抑制都写明理由**：说明为什么这是有意为之而非疏漏。二进制资源无法内联注释，理由写在
  `app/lint.xml` 该条目的注释里；现有 `ChromeOsAbiSupport` 的忽略即按此格式记录。
- **能真修就不要抑制**：先确认该项是否指出了真实缺陷。例：`IconLocation` 指出位图放在无密度限定的
  `drawable/`，官方做法是移入 `drawable-nodpi/`（刻意不随密度缩放时），而非抑制。

## 依赖库资源的覆盖

覆盖依赖库资源（图标、主题、字符串）时：

- **同名同类型放置**在对应 `res/` 目录下，库按名解析即可命中覆盖版本。
- **位图放 `drawable-nodpi/`**：覆盖库的通知小图标等须按原尺寸呈现的位图不随密度缩放；
  放在无限定的 `drawable/` 会被 `IconLocation` 判为位置不当。
- **保留覆盖意图**：在 `app/lint.xml` 的对应条目或资源旁的注释里记明该资源覆盖的是哪个库，
  避免后人（或静态检查）再把它当无用资源删除。

## 组件归属

- 跨页面复用的组件上提至 `ui/component/`。
- 无跨页面复用的组件下沉到所属页面的 `component/`。

## 页面根包

- 页面根包只允许存放骨架文件，其余业务文件一律进入子目录收敛。
- 骨架白名单：`XxxScreen.kt`、`XxxUiState.kt`、`XxxViewModel.kt`。
- 其余文件进入 `component/`、`compact/`、`expanded/` 等子目录，或按业务语义新建子目录。

## 布局与自适应

- 屏幕必须同时适配窄窗与宽窗。窗口尺寸类在 `{Screen}Screen` 单一位置计算，作为参数显式下传给组装器；组件只按收到的布局标志位渲染，不自行读取窗口尺寸。
- 形态差异优先用「布局标志位 + 宽度上限」表达（`ContentPane` 的行宽上限即此类）。同一份内容不维护两套组装器。
- 只有当两种形态的功能区块结构确实不同（区块增减、导航形态改变）时，才拆 `compact/` 与 `expanded/` 两套组装器；二者必须消费同一份 `UiState` 与同一套事件回调，禁止各自定义状态或事件。
- 布局由组装器组装页面 `component/` 或 `ui/component/` 下的组件得出，不在同一文件内硬编码多种布局；共用组件经组装器参数注入。
- 拆分判据是「单一问题」：可独立交互 / 预览 / 测试的功能区块才拆成组件；单行 `Text`、`Spacer` 这类微结构内联；只服务当前屏幕的私有组装留在同文件的 `private` Composable 中。
- 页面级弹层与跨形态副作用挂在 `{Screen}Screen` 骨架层，不随形态重复实现。

## 页面模块目录示例

```text
screens/home/
├── assembly/                      # 组装器(必须)
│   └── HomeAssembly.kt            #   结构差异大时改为 compact/ + expanded/ 两套
├── component/                     # 页面组件(必须)
│   ├── …/
│   │   └── ….kt
│   └── …/
├── HomeScreen.kt                  # 骨架(必须)
├── HomeUiState.kt                 # 骨架(必须)
└── HomeViewModel.kt               # 骨架(必须)
```

`{Screen}Screen` 只做 ViewModel 连接、尺寸类计算与副作用；至少有一个无状态 Composable 接收 `UiState` 与事件回调，脱离 ViewModel 即可预览。

## 未明确决策

- 设计存在多种方案时择优；影响面超出单文件且无官方推荐时，先确认再动手。
- 当前技术栈有官方推荐做法时，按官方推荐处理。
- 重构后原类职责须显著下降，且不得引入新的隐式依赖或状态双份。
