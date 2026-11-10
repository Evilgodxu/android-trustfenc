package com.trustfenc.evilgodxu.screens.settings

import androidx.compose.ui.geometry.Offset

// 设置页 UI 状态：页面级弹窗显隐与主题切换动效起点
data class SettingsUiState(
    val showThemeDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val pendingThemeClickPosition: Offset = Offset.Zero,
)
