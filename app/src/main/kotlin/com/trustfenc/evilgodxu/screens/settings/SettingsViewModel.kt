package com.trustfenc.evilgodxu.screens.settings

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 设置页 ViewModel：仅持有页面级 UI 状态（弹窗显隐与动效起点）；
// 主题、语言、版本与更新检查均属应用级，分别由 MainViewModel、AppUpdateViewModel 提供
class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // 点击主题项：记录动效起点并打开主题弹窗
    fun showThemeDialog(position: Offset) {
        _uiState.update { it.copy(showThemeDialog = true, pendingThemeClickPosition = position) }
    }

    fun dismissThemeDialog() {
        _uiState.update { it.copy(showThemeDialog = false) }
    }

    fun showLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = true) }
    }

    fun dismissLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = false) }
    }
}
