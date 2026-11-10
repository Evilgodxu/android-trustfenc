package com.trustfenc.evilgodxu.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.trustfenc.evilgodxu.data.fence.FenceFailure
import com.trustfenc.evilgodxu.data.fence.FenceSnapshot
import com.trustfenc.evilgodxu.data.fence.FenceStatus
import com.trustfenc.evilgodxu.data.fence.InstalledAppScanner
import com.trustfenc.evilgodxu.data.fence.InterceptedApp
import com.trustfenc.evilgodxu.data.shizuku.ShizukuShell
import com.trustfenc.evilgodxu.data.shizuku.ShizukuState
import com.trustfenc.evilgodxu.log.CrashLogManager
import com.trustfenc.evilgodxu.service.FenceGuardService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// 篱笆仓库：DataStore 持有基线与统计，Shizuku 负责扫描与卸载，前台服务负责安装监听
class DataStoreFenceRepository(
    context: Context,
    private val dataStore: DataStore<Preferences>,
    private val scanner: InstalledAppScanner,
    private val shell: ShizukuShell,
) : FenceRepository {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true }

    private val _snapshot = MutableStateFlow(FenceSnapshot(shizuku = shell.state.value))
    override val snapshot: StateFlow<FenceSnapshot> = _snapshot.asStateFlow()

    private val _failures = MutableSharedFlow<FenceFailure>(extraBufferCapacity = 4)
    override val failures: SharedFlow<FenceFailure> = _failures.asSharedFlow()

    private val loaded = CompletableDeferred<Unit>()
    private val transitionMutex = Mutex()

    init {
        scope.launch {
            shell.state.collect { state ->
                _snapshot.update { it.copy(shizuku = state) }
            }
        }
        scope.launch {
            restoreFromStorage()
            loaded.complete(Unit)
        }
    }

    override suspend fun buildFence() {
        loaded.await()
        if (!transitionMutex.tryLock()) return
        try {
            if (_snapshot.value.status != FenceStatus.Idle) return
            _snapshot.update { it.copy(status = FenceStatus.Building) }

            if (!ensureShizukuReady()) {
                _snapshot.update { it.copy(status = FenceStatus.Idle) }
                return
            }

            val baseline = try {
                scanner.scanBaseline()
            } catch (e: Exception) {
                CrashLogManager.logException("FenceRepository", "扫描已安装应用失败", e)
                _failures.emit(FenceFailure.ScanFailed)
                _snapshot.update { it.copy(status = FenceStatus.Idle) }
                return
            }

            val since = System.currentTimeMillis()
            dataStore.edit { prefs ->
                prefs[BASELINE] = baseline
                prefs[GUARDING] = true
                prefs[GUARDING_SINCE] = since
                prefs[INTERCEPTED_COUNT] = 0
                prefs.remove(RECENT_INTERCEPTS)
            }

            val serviceStarted = runCatching { startGuardService() }.onFailure {
                CrashLogManager.logException("FenceRepository", "启动守护服务失败", it)
            }.isSuccess
            if (!serviceStarted) {
                dataStore.edit { it.clearFence() }
                _failures.emit(FenceFailure.GuardServiceFailed)
                _snapshot.update {
                    it.copy(
                        status = FenceStatus.Idle,
                        baselineCount = 0,
                        interceptedCount = 0,
                        recentIntercepts = emptyList(),
                    )
                }
                return
            }

            _snapshot.update {
                it.copy(
                    status = FenceStatus.Guarding(since),
                    baselineCount = baseline.size,
                    interceptedCount = 0,
                    recentIntercepts = emptyList(),
                )
            }
        } finally {
            transitionMutex.unlock()
        }
    }

    override suspend fun dismantleFence() {
        loaded.await()
        if (!transitionMutex.tryLock()) return
        try {
            if (_snapshot.value.status !is FenceStatus.Guarding) return
            _snapshot.update { it.copy(status = FenceStatus.Dismantling) }
            runCatching { stopGuardService() }.onFailure {
                CrashLogManager.logException("FenceRepository", "停止守护服务失败", it)
            }
            dataStore.edit { it.clearFence() }
            _snapshot.update {
                it.copy(
                    status = FenceStatus.Idle,
                    baselineCount = 0,
                    interceptedCount = 0,
                    recentIntercepts = emptyList(),
                )
            }
        } finally {
            transitionMutex.unlock()
        }
    }

    override suspend fun evaluateInstalledPackage(packageName: String) {
        if (packageName == appContext.packageName) return
        val prefs = runCatching { dataStore.data.first() }.getOrElse {
            CrashLogManager.logException("FenceRepository", "读取基线失败：$packageName", it)
            return
        }
        val baseline = prefs[BASELINE].orEmpty()
        if (baseline.isEmpty() || _snapshot.value.status !is FenceStatus.Guarding) return
        if (packageName in baseline) return

        val pm = appContext.packageManager
        val appInfo = runCatching {
            pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
        }.getOrNull()
        // 系统应用与系统更新不属于安装拦截范围，避免 OTA 新增系统组件被误判
        val systemFlags = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
        if (appInfo != null && appInfo.flags and systemFlags != 0) return
        val label = appInfo?.let { pm.getApplicationLabel(it).toString() } ?: packageName

        val output = shell.execute("pm uninstall $packageName").getOrNull()
        if (output == null || !output.trim().startsWith("Success")) {
            // 卸载失败发生在真机上时，记录包名与命令输出，便于用户反馈后定位
            CrashLogManager.logException(
                "FenceRepository",
                "卸载越界应用失败：$packageName，输出：${output?.trim().orEmpty()}",
                IllegalStateException("pm uninstall failed"),
            )
            return
        }

        val record = InterceptedApp(
            label = label,
            packageName = packageName,
            interceptedAtMillis = System.currentTimeMillis(),
        )
        val updated = dataStore.edit { prefsToUpdate ->
            val count = (prefsToUpdate[INTERCEPTED_COUNT] ?: 0) + 1
            prefsToUpdate[INTERCEPTED_COUNT] = count
            val recent = decodeIntercepts(prefsToUpdate[RECENT_INTERCEPTS])
            prefsToUpdate[RECENT_INTERCEPTS] = json.encodeToString(
                (listOf(record) + recent).take(MAX_RECENT_INTERCEPTS),
            )
        }
        _snapshot.update {
            it.copy(
                interceptedCount = updated[INTERCEPTED_COUNT] ?: 0,
                recentIntercepts = decodeIntercepts(updated[RECENT_INTERCEPTS]),
            )
        }
    }

    override suspend fun awaitLoaded() {
        loaded.await()
    }

    private suspend fun ensureShizukuReady(): Boolean = when (shell.state.value) {
        ShizukuState.NotInstalled -> {
            _failures.emit(FenceFailure.ShizukuNotInstalled)
            false
        }

        ShizukuState.NotRunning -> {
            _failures.emit(FenceFailure.ShizukuNotRunning)
            false
        }

        ShizukuState.PermissionRequired, ShizukuState.PermissionDenied -> {
            val granted = shell.requestPermission()
            if (!granted) {
                _failures.emit(FenceFailure.PermissionDenied)
            }
            granted
        }

        ShizukuState.Ready -> true
    }

    private suspend fun restoreFromStorage() {
        val prefs = runCatching { dataStore.data.first() }.getOrElse {
            CrashLogManager.logException("FenceRepository", "读取篱笆持久化状态失败", it)
            emptyPreferences()
        }
        val baseline = prefs[BASELINE].orEmpty()
        val guarding = prefs[GUARDING] == true && baseline.isNotEmpty()
        _snapshot.update {
            it.copy(
                status = if (guarding) FenceStatus.Guarding(prefs[GUARDING_SINCE] ?: 0L) else FenceStatus.Idle,
                baselineCount = baseline.size,
                interceptedCount = prefs[INTERCEPTED_COUNT] ?: 0,
                recentIntercepts = decodeIntercepts(prefs[RECENT_INTERCEPTS]),
            )
        }
        if (guarding) {
            // 应用被系统回收后重新拉起时恢复监听，避免基线存在但无人守望
            runCatching { startGuardService() }.onFailure {
                CrashLogManager.logException("FenceRepository", "恢复守护服务失败", it)
            }
        }
    }

    private fun decodeIntercepts(raw: String?): List<InterceptedApp> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<InterceptedApp>>(raw) }.getOrDefault(emptyList())
    }

    private fun startGuardService() {
        ContextCompat.startForegroundService(appContext, guardServiceIntent())
    }

    private fun stopGuardService() {
        appContext.stopService(guardServiceIntent())
    }

    private fun guardServiceIntent() = Intent(appContext, FenceGuardService::class.java)

    private fun MutablePreferences.clearFence() {
        remove(BASELINE)
        remove(GUARDING)
        remove(GUARDING_SINCE)
        remove(INTERCEPTED_COUNT)
        remove(RECENT_INTERCEPTS)
    }

    private companion object {
        val BASELINE = stringSetPreferencesKey("fence_baseline")
        val GUARDING = booleanPreferencesKey("fence_guarding")
        val GUARDING_SINCE = longPreferencesKey("fence_guarding_since")
        val INTERCEPTED_COUNT = intPreferencesKey("fence_intercepted_count")
        val RECENT_INTERCEPTS = stringPreferencesKey("fence_recent_intercepts")
        const val MAX_RECENT_INTERCEPTS = 20
    }
}
