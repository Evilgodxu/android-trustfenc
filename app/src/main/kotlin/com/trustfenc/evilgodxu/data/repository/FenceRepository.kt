package com.trustfenc.evilgodxu.data.repository

import com.trustfenc.evilgodxu.data.fence.FenceFailure
import com.trustfenc.evilgodxu.data.fence.FenceSnapshot
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

// 篱笆数据源：对外只暴露只读快照与一次性失败事件
interface FenceRepository {
    val snapshot: StateFlow<FenceSnapshot>

    val failures: SharedFlow<FenceFailure>

    suspend fun buildFence()

    suspend fun dismantleFence()

    // 收到新应用安装后的判定与处理：越界即卸载并记账
    suspend fun evaluateInstalledPackage(packageName: String)

    // 守护服务在开机等场景启动时，等待持久化基线加载完成再决定去留
    suspend fun awaitLoaded()
}
