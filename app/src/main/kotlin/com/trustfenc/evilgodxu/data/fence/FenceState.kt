package com.trustfenc.evilgodxu.data.fence

// 篱笆的独占状态：同一时刻只会处于其中一种，消费侧可穷举处理
sealed interface FenceStatus {
    data object Idle : FenceStatus

    data object Building : FenceStatus

    // 记录立起的时刻，用于控制台展示守护时长
    data class Guarding(val sinceMillis: Long) : FenceStatus

    data object Dismantling : FenceStatus
}

// 被拦截并卸载的应用记录
data class InterceptedApp(
    val label: String,
    val packageName: String,
    val interceptedAtMillis: Long,
)

// 控制台渲染所需的完整快照，由仓库单一持有
data class FenceSnapshot(
    val status: FenceStatus = FenceStatus.Idle,
    val baselineCount: Int = 0,
    val interceptedCount: Int = 0,
    val recentIntercepts: List<InterceptedApp> = emptyList(),
)
