package com.trustfenc.evilgodxu.data.repository

import com.trustfenc.evilgodxu.data.fence.FenceSnapshot
import kotlinx.coroutines.flow.StateFlow

// 篱笆数据源：对外只暴露只读快照，修筑与拆除为一次性异步动作
interface FenceRepository {
    val snapshot: StateFlow<FenceSnapshot>

    suspend fun buildFence()

    suspend fun dismantleFence()
}
