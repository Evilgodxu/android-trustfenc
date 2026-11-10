package com.trustfenc.evilgodxu.data.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.trustfenc.evilgodxu.log.CrashLogManager
import com.trustfenc.evilgodxu.service.CommandUserService
import com.trustfenc.evilgodxu.service.ICommandService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

// Shizuku 的可用状态：UI 据此提示运行条件，仓库据此决定能否修筑
sealed interface ShizukuState {
    data object NotInstalled : ShizukuState

    data object NotRunning : ShizukuState

    data object PermissionRequired : ShizukuState

    data object PermissionDenied : ShizukuState

    data object Ready : ShizukuState
}

// ADB 权限通道：申请 Shizuku 授权，并通过 UserService 执行 shell 命令
class ShizukuShell(context: Context) {

    private val appContext = context.applicationContext
    private val serviceArgs by lazy { CommandUserService.createServiceArgs(appContext) }

    private val _state = MutableStateFlow<ShizukuState>(ShizukuState.NotRunning)
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    private val bindMutex = Mutex()
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var commandService: ICommandService? = null

    @Volatile
    private var binding = false

    private var pendingPermission: CompletableDeferred<Boolean>? = null
    private var pendingBinding: CompletableDeferred<ICommandService>? = null

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        refreshState()
        if (_state.value == ShizukuState.Ready) {
            bindUserService()
        }
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        commandService = null
        binding = false
        _state.value = ShizukuState.NotRunning
    }

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == PERMISSION_REQUEST_CODE) {
            refreshState()
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                bindUserService()
            }
            pendingPermission?.complete(grantResult == PackageManager.PERMISSION_GRANTED)
            pendingPermission = null
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binding = false
            val connected = service?.let { ICommandService.Stub.asInterface(it) }
            commandService = connected
            if (connected != null) {
                pendingBinding?.complete(connected)
            } else {
                pendingBinding?.completeExceptionally(IllegalStateException("UserService binder is null"))
            }
            pendingBinding = null
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binding = false
            commandService = null
            pendingBinding?.completeExceptionally(IllegalStateException("UserService disconnected"))
            pendingBinding = null
        }
    }

    init {
        // 始终注册监听：用户中途安装或启动 Shizuku 后，无需重启应用即可重新识别
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionListener)
        refreshState()
        if (_state.value == ShizukuState.Ready) {
            bindUserService()
        }
    }

    // 申请授权；已授权返回 true，Shizuku 未运行或用户拒绝返回 false
    suspend fun requestPermission(): Boolean = withContext(Dispatchers.Main) {
        if (!Shizuku.pingBinder()) {
            refreshState()
            return@withContext false
        }
        if (hasPermission()) {
            refreshState()
            bindUserService()
            return@withContext true
        }
        if (Shizuku.isPreV11() || Shizuku.shouldShowRequestPermissionRationale()) {
            refreshState()
            return@withContext false
        }
        val deferred = CompletableDeferred<Boolean>()
        pendingPermission = deferred
        try {
            Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
            withTimeoutOrNull(PERMISSION_TIMEOUT_MS) { deferred.await() } == true
        } finally {
            pendingPermission = null
        }
    }

    suspend fun execute(command: String): Result<String> = withContext(Dispatchers.IO) {
        val service = awaitService()
            ?: return@withContext Result.failure(IllegalStateException("Shizuku UserService 未连接"))
        runCatching { service.executeCommand(command) }
    }

    private suspend fun awaitService(): ICommandService? {
        commandService?.let { return it }
        if (_state.value != ShizukuState.Ready) return null
        return bindMutex.withLock {
            commandService?.let { return@withLock it }
            val deferred = CompletableDeferred<ICommandService>()
            pendingBinding = deferred
            if (!binding) {
                binding = true
                withContext(Dispatchers.Main) {
                    runCatching { Shizuku.bindUserService(serviceArgs, serviceConnection) }
                        .onFailure {
                            binding = false
                            CrashLogManager.logException("ShizukuShell", "绑定 UserService 失败", it)
                            deferred.completeExceptionally(it)
                        }
                }
            }
            withTimeoutOrNull(BIND_TIMEOUT_MS) { deferred.await() }
        }
    }

    private fun bindUserService() {
        if (commandService != null || binding || _state.value != ShizukuState.Ready) return
        binding = true
        // 仓库可能在后台线程创建，绑定必须回到主线程执行
        mainHandler.post {
            if (_state.value != ShizukuState.Ready) {
                binding = false
                return@post
            }
            runCatching { Shizuku.bindUserService(serviceArgs, serviceConnection) }
                .onFailure {
                    binding = false
                    CrashLogManager.logException("ShizukuShell", "绑定 UserService 失败", it)
                }
        }
    }

    private fun refreshState() {
        if (!isManagerInstalled()) {
            _state.value = ShizukuState.NotInstalled
            return
        }
        if (!Shizuku.pingBinder()) {
            _state.value = ShizukuState.NotRunning
            return
        }
        _state.value = when {
            hasPermission() -> ShizukuState.Ready
            Shizuku.isPreV11() -> ShizukuState.PermissionDenied
            Shizuku.shouldShowRequestPermissionRationale() -> ShizukuState.PermissionDenied
            else -> ShizukuState.PermissionRequired
        }
    }

    private fun hasPermission(): Boolean =
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    private fun isManagerInstalled(): Boolean = runCatching {
        appContext.packageManager.getPackageInfo(MANAGER_PACKAGE, 0)
    }.isSuccess

    companion object {
        const val MANAGER_PACKAGE = ShizukuProvider.MANAGER_APPLICATION_ID

        private const val PERMISSION_REQUEST_CODE = 1001
        private const val PERMISSION_TIMEOUT_MS = 120_000L
        private const val BIND_TIMEOUT_MS = 10_000L
    }
}
