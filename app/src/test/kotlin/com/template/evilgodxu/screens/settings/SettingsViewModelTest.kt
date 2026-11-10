package com.trustfenc.evilgodxu.screens.settings

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// 页面级状态只承担弹窗显隐与动效起点，行为边界固定，不需要协程调度
class SettingsViewModelTest {

    @Test
    fun `初始状态不展示任何弹窗`() {
        val state = SettingsViewModel().uiState.value

        assertFalse(state.showThemeDialog)
        assertFalse(state.showLanguageDialog)
        assertEquals(Offset.Zero, state.pendingThemeClickPosition)
    }

    @Test
    fun `打开主题弹窗时记录动效起点`() {
        val viewModel = SettingsViewModel()
        val position = Offset(120f, 480f)

        viewModel.showThemeDialog(position)

        assertTrue(viewModel.uiState.value.showThemeDialog)
        assertEquals(position, viewModel.uiState.value.pendingThemeClickPosition)
    }

    @Test
    fun `关闭主题弹窗保留动效起点`() {
        val viewModel = SettingsViewModel()
        val position = Offset(10f, 20f)
        viewModel.showThemeDialog(position)

        viewModel.dismissThemeDialog()

        assertFalse(viewModel.uiState.value.showThemeDialog)
        // 动效在弹窗关闭后才播放，起点必须跨过这次状态变更仍然可用
        assertEquals(position, viewModel.uiState.value.pendingThemeClickPosition)
    }

    @Test
    fun `两个弹窗的显隐互不影响`() {
        val viewModel = SettingsViewModel()

        viewModel.showLanguageDialog()
        assertTrue(viewModel.uiState.value.showLanguageDialog)
        assertFalse(viewModel.uiState.value.showThemeDialog)

        viewModel.showThemeDialog(Offset(1f, 1f))
        assertTrue(viewModel.uiState.value.showLanguageDialog)

        viewModel.dismissLanguageDialog()
        assertFalse(viewModel.uiState.value.showLanguageDialog)
        assertTrue(viewModel.uiState.value.showThemeDialog)
    }
}
