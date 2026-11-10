package com.trustfenc.evilgodxu.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trustfenc.evilgodxu.data.fence.FenceFailure
import com.trustfenc.evilgodxu.data.fence.FenceStatus
import com.trustfenc.evilgodxu.data.repository.FenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 控制台页面级 ViewModel：把仓库快照、确认弹窗与失败提示合成唯一 UI 状态
class HomeViewModel(
    private val fenceRepository: FenceRepository,
) : ViewModel() {

    private val showDismantleConfirm = MutableStateFlow(false)
    private val notice = MutableStateFlow<FenceFailure?>(null)

    init {
        viewModelScope.launch {
            fenceRepository.failures.collect { failure -> notice.value = failure }
        }
    }

    val uiState: StateFlow<HomeUiState> =
        combine(
            fenceRepository.snapshot,
            showDismantleConfirm,
            notice,
        ) { snapshot, confirm, currentNotice ->
            HomeUiState(
                status = snapshot.status,
                shizuku = snapshot.shizuku,
                serviceRunning = snapshot.serviceRunning,
                baselineCount = snapshot.baselineCount,
                interceptedCount = snapshot.interceptedCount,
                recentIntercepts = snapshot.recentIntercepts,
                showDismantleConfirm = confirm,
                notice = currentNotice,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    // 主按钮：未修筑则修筑，守护中只请求确认，过渡态不响应连点
    fun onPrimaryAction() {
        when (fenceRepository.snapshot.value.status) {
            FenceStatus.Idle -> viewModelScope.launch { fenceRepository.buildFence() }
            is FenceStatus.Guarding -> showDismantleConfirm.value = true
            FenceStatus.Building, FenceStatus.Dismantling -> Unit
        }
    }

    fun dismissDismantleConfirm() {
        showDismantleConfirm.value = false
    }

    fun confirmDismantle() {
        showDismantleConfirm.value = false
        viewModelScope.launch { fenceRepository.dismantleFence() }
    }

    fun dismissNotice() {
        notice.value = null
    }
}
