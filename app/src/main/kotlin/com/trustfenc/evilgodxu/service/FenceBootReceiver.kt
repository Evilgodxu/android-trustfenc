package com.trustfenc.evilgodxu.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.trustfenc.evilgodxu.App
import com.trustfenc.evilgodxu.data.fence.FenceStatus
import com.trustfenc.evilgodxu.log.CrashLogManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 开机与应用更新后恢复守护；基线为空时不再拉起前台服务
class FenceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> restoreGuard(context)
        }
    }

    private fun restoreGuard(context: Context) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val repository = (context.applicationContext as App).container.fenceRepository
                repository.awaitLoaded()
                if (repository.snapshot.value.status is FenceStatus.Guarding) {
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context, FenceGuardService::class.java),
                    )
                }
            } catch (e: Exception) {
                CrashLogManager.logException("FenceBootReceiver", "开机恢复守护失败", e)
            } finally {
                pending.finish()
            }
        }
    }
}
