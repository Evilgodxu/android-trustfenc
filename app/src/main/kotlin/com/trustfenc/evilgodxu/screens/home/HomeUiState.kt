package com.trustfenc.evilgodxu.screens.home

import com.trustfenc.evilgodxu.data.fence.FenceStatus
import com.trustfenc.evilgodxu.data.fence.InterceptedApp

// 控制台 UI 状态：聚合篱笆仓库快照与页面级确认弹窗
data class HomeUiState(
    val status: FenceStatus = FenceStatus.Idle,
    val baselineCount: Int = 0,
    val interceptedCount: Int = 0,
    val recentIntercepts: List<InterceptedApp> = emptyList(),
    val showDismantleConfirm: Boolean = false,
)
