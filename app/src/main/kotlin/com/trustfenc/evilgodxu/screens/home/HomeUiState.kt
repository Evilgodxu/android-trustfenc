package com.trustfenc.evilgodxu.screens.home

import com.trustfenc.evilgodxu.data.fence.FenceFailure
import com.trustfenc.evilgodxu.data.fence.FenceStatus
import com.trustfenc.evilgodxu.data.fence.InterceptedApp
import com.trustfenc.evilgodxu.data.shizuku.ShizukuState

// 控制台 UI 状态：聚合篱笆仓库快照、页面级确认弹窗与一次性失败提示
data class HomeUiState(
    val status: FenceStatus = FenceStatus.Idle,
    val shizuku: ShizukuState = ShizukuState.NotRunning,
    val baselineCount: Int = 0,
    val interceptedCount: Int = 0,
    val recentIntercepts: List<InterceptedApp> = emptyList(),
    val showDismantleConfirm: Boolean = false,
    val notice: FenceFailure? = null,
)
