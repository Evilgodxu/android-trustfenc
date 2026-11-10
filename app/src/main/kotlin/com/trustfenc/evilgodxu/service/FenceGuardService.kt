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

// 守护前台服务：常驻通知保活，动态监听安装广播并对越界应用执行卸载
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
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )
        scope.launch {
            repository.awaitLoaded()
            if (repository.snapshot.value.status !is FenceStatus.Guarding) {
                stopSelf()
                return@launch
            }
            registerPackageReceiver()
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

    override fun onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(packageReceiver)
            receiverRegistered = false
        }
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerPackageReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED).apply {
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(
            this,
            packageReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
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
    }
}
