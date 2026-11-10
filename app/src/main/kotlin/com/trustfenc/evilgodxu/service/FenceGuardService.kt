package com.trustfenc.evilgodxu.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.trustfenc.evilgodxu.App
import com.trustfenc.evilgodxu.MainActivity
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.data.fence.FenceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 官方前台服务：常驻通知保证后台存活，动态监听安装广播并对越界应用执行卸载
class FenceGuardService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val repository get() = (application as App).container.fenceRepository
    private var receiverRegistered = false

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != Intent.ACTION_PACKAGE_ADDED) return
            // 覆盖安装属于合法更新，放行
            if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return
            val packageName = intent.data?.schemeSpecificPart ?: return
            scope.launch {
                // 安装广播可能早于包信息落定，稍等再判定与卸载
                delay(INSTALL_SETTLE_DELAY_MS)
                repository.evaluateInstalledPackage(packageName)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(baselineCount = 0, interceptedCount = 0),
            // specialUse 自 API 34 起才有定义；更低版本按普通前台服务运行
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            },
        )
        // 同步注册监听，避免服务启动窗口内漏掉安装事件
        registerPackageReceiver()
        repository.onGuardServiceStarted()
        scope.launch {
            repository.awaitLoaded()
            if (repository.snapshot.value.status !is FenceStatus.Guarding) {
                stopSelf()
                return@launch
            }
            // 广播在部分 ROM 上可能被拦截或延迟，轮询当前安装列表兜底
            launch {
                while (repository.snapshot.value.status is FenceStatus.Guarding) {
                    delay(WATCHDOG_INTERVAL_MS)
                    repository.verifyInstalledPackages()
                }
            }
            repository.snapshot.collect { snapshot ->
                if (snapshot.status !is FenceStatus.Guarding) {
                    stopSelf()
                    return@collect
                }
                updateNotification(snapshot.baselineCount, snapshot.interceptedCount)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    // 从最近任务划掉应用时部分 ROM 会顺带停掉服务，守护状态下立即拉起
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (repository.snapshot.value.status is FenceStatus.Guarding) {
            runCatching {
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, FenceGuardService::class.java),
                )
            }
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(packageReceiver)
            receiverRegistered = false
        }
        repository.onGuardServiceStopped()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerPackageReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED).apply {
            addDataScheme("package")
        }
        // PACKAGE_ADDED 是系统保护的广播，第三方无法伪造；按系统广播接收要求使用 EXPORTED
        ContextCompat.registerReceiver(
            this,
            packageReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED,
        )
        receiverRegistered = true
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.fence_notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.fence_notification_channel_desc)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(baselineCount: Int, interceptedCount: Int): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_fence_notification)
            .setContentTitle(getString(R.string.fence_notification_title))
            .setContentText(getString(R.string.fence_notification_text, baselineCount, interceptedCount))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(contentIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(baselineCount: Int, interceptedCount: Int) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(baselineCount, interceptedCount))
    }

    private companion object {
        const val CHANNEL_ID = "fence_guard"
        const val NOTIFICATION_ID = 1001
        const val INSTALL_SETTLE_DELAY_MS = 1_000L
        const val WATCHDOG_INTERVAL_MS = 5_000L
    }
}
