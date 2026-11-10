package com.trustfenc.evilgodxu.screens.home

import com.trustfenc.evilgodxu.data.fence.FenceFailure
import com.trustfenc.evilgodxu.data.fence.FenceSnapshot
import com.trustfenc.evilgodxu.data.fence.FenceStatus
import com.trustfenc.evilgodxu.data.repository.FenceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 测试用内存仓库：只实现状态迁移，不触碰 Shizuku、DataStore 与前台服务
class FakeFenceRepository(
    initial: FenceSnapshot = FenceSnapshot(),
) : FenceRepository {

    private val _snapshot = MutableStateFlow(initial)
    override val snapshot: StateFlow<FenceSnapshot> = _snapshot.asStateFlow()

    private val _failures = MutableSharedFlow<FenceFailure>(extraBufferCapacity = 4)
    override val failures: SharedFlow<FenceFailure> = _failures.asSharedFlow()

    override suspend fun buildFence() {
        _snapshot.update {
            it.copy(
                status = FenceStatus.Guarding(System.currentTimeMillis()),
                baselineCount = 3,
            )
        }
    }

    override suspend fun dismantleFence() {
        _snapshot.value = FenceSnapshot(shizuku = _snapshot.value.shizuku)
    }

    override suspend fun evaluateInstalledPackage(packageName: String) = Unit

    override suspend fun verifyInstalledPackages() = Unit

    override fun onGuardServiceStarted() {
        _snapshot.update { it.copy(serviceRunning = true) }
    }

    override fun onGuardServiceStopped() {
        _snapshot.update { it.copy(serviceRunning = false) }
    }

    override suspend fun awaitLoaded() = Unit

    fun emitFailure(failure: FenceFailure) {
        _failures.tryEmit(failure)
    }
}
