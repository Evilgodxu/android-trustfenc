package com.trustfenc.evilgodxu.screens.home

import com.trustfenc.evilgodxu.data.fence.FenceFailure
import com.trustfenc.evilgodxu.data.fence.FenceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// 控制台状态持有者：验证「仓库快照 → UI 状态」的回流，以及修筑/拆除事件的状态迁移
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val mainDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初始状态未修筑且不弹确认框`() = runTest(mainDispatcher) {
        val viewModel = HomeViewModel(FakeFenceRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect { } }

        assertEquals(FenceStatus.Idle, viewModel.uiState.value.status)
        assertFalse(viewModel.uiState.value.showDismantleConfirm)
    }

    @Test
    fun `修筑篱笆经仓库回流进入守护状态`() = runTest(mainDispatcher) {
        val viewModel = HomeViewModel(FakeFenceRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect { } }

        viewModel.onPrimaryAction()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.status is FenceStatus.Guarding)
        assertEquals(3, viewModel.uiState.value.baselineCount)
        assertFalse(viewModel.uiState.value.showDismantleConfirm)
    }

    @Test
    fun `守护中再次点击只请求确认不立即拆除`() = runTest(mainDispatcher) {
        val viewModel = HomeViewModel(FakeFenceRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect { } }
        viewModel.onPrimaryAction()
        advanceUntilIdle()

        viewModel.onPrimaryAction()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showDismantleConfirm)
        assertTrue(viewModel.uiState.value.status is FenceStatus.Guarding)
    }

    @Test
    fun `确认拆除经仓库回流回到未修筑`() = runTest(mainDispatcher) {
        val viewModel = HomeViewModel(FakeFenceRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect { } }
        viewModel.onPrimaryAction()
        advanceUntilIdle()

        viewModel.confirmDismantle()
        advanceUntilIdle()

        assertEquals(FenceStatus.Idle, viewModel.uiState.value.status)
        assertFalse(viewModel.uiState.value.showDismantleConfirm)
    }

    @Test
    fun `取消拆除保持守护状态`() = runTest(mainDispatcher) {
        val viewModel = HomeViewModel(FakeFenceRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect { } }
        viewModel.onPrimaryAction()
        advanceUntilIdle()
        viewModel.onPrimaryAction()

        viewModel.dismissDismantleConfirm()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showDismantleConfirm)
        assertTrue(viewModel.uiState.value.status is FenceStatus.Guarding)
    }

    @Test
    fun `失败事件呈现为提示并可关闭`() = runTest(mainDispatcher) {
        val repository = FakeFenceRepository()
        val viewModel = HomeViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect { } }
        advanceUntilIdle()

        repository.emitFailure(FenceFailure.PermissionDenied)
        advanceUntilIdle()

        assertEquals(FenceFailure.PermissionDenied, viewModel.uiState.value.notice)

        viewModel.dismissNotice()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.notice)
    }
}
