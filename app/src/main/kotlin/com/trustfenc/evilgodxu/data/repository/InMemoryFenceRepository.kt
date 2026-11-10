package com.trustfenc.evilgodxu.data.repository

import com.trustfenc.evilgodxu.data.fence.FenceSnapshot
import com.trustfenc.evilgodxu.data.fence.FenceStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex

// 控制台骨架阶段的内存实现：只维护状态机与过渡耗时，
// 真正的应用扫描、基线持久化与安装监听接入数据层后替换
class InMemoryFenceRepository(
    private val buildingDurationMillis: Long = 900,
    private val dismantlingDurationMillis: Long = 400,
) : FenceRepository {

    private val _snapshot = MutableStateFlow(FenceSnapshot())
    override val snapshot: StateFlow<FenceSnapshot> = _snapshot.asStateFlow()

    // 同一时刻只允许一次状态迁移，避免连点产生交叉过渡
    private val transitionMutex = Mutex()

    override suspend fun buildFence() {
        if (!transitionMutex.tryLock()) return
        try {
            if (_snapshot.value.status != FenceStatus.Idle) return
            val previous = _snapshot.value
            _snapshot.value = previous.copy(status = FenceStatus.Building)
            try {
                delay(buildingDurationMillis)
            } catch (e: CancellationException) {
                // 页面中途离开时回到修筑前，避免状态卡在过渡态
                _snapshot.value = previous
                throw e
            }
            _snapshot.value = FenceSnapshot(
                status = FenceStatus.Guarding(System.currentTimeMillis()),
            )
        } finally {
            transitionMutex.unlock()
        }
    }

    override suspend fun dismantleFence() {
        if (!transitionMutex.tryLock()) return
        try {
            if (_snapshot.value.status !is FenceStatus.Guarding) return
            val previous = _snapshot.value
            _snapshot.value = previous.copy(status = FenceStatus.Dismantling)
            try {
                delay(dismantlingDurationMillis)
            } catch (e: CancellationException) {
                _snapshot.value = previous
                throw e
            }
            _snapshot.value = FenceSnapshot()
        } finally {
            transitionMutex.unlock()
        }
    }
}
