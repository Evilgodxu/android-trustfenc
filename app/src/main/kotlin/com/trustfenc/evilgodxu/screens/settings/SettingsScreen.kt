package com.trustfenc.evilgodxu.screens.settings

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trustfenc.evilgodxu.LocalMainViewModel
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.screens.settings.assembly.SettingsAssembly
import com.trustfenc.evilgodxu.screens.settings.component.dialog.LanguageSelectionDialog
import com.trustfenc.evilgodxu.screens.settings.component.dialog.ThemeSelectionDialog
import com.trustfenc.evilgodxu.theme.LocalThemeTransitionController
import com.trustfenc.evilgodxu.ui.component.fui.FuiConsoleTheme
import com.trustfenc.evilgodxu.update.LocalAppUpdateViewModel
import com.trustfenc.evilgodxu.update.UpdateMessage
import com.trustfenc.evilgodxu.windowsize.rememberWindowSizeClass
import kotlinx.coroutines.flow.Flow

// 页面入口：提升状态与事件，承载跨形态副作用与页面级弹层，窗口尺寸类在此计算后作为参数下传
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activityViewModel = LocalMainViewModel.current
    val updateViewModel = LocalAppUpdateViewModel.current
    // 作用域跟随导航条目，页面出栈即回收
    val settingsViewModel: SettingsViewModel = viewModel()
    val appUiState by activityViewModel.uiState.collectAsStateWithLifecycle()
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val onThemeReveal: (Offset) -> Unit = LocalThemeTransitionController.current::revealAt

    CheckUpdateMessagesEffect(updateViewModel.messages)

    // 设置页沿用控制台的 FUI 黑白主题，页面与弹窗共享同一配色作用域
    FuiConsoleTheme {
        SettingsAssembly(
            windowSizeClass = rememberWindowSizeClass(),
            uiState = appUiState,
            onBack = onBack,
            onShowThemeDialog = settingsViewModel::showThemeDialog,
            onShowLanguageDialog = settingsViewModel::showLanguageDialog,
            onCheckForUpdate = updateViewModel::checkNow,
            modifier = modifier,
        )

        // 弹层与形态无关，统一挂在骨架层，新增形态时不会重复实现
        if (settingsUiState.showThemeDialog) {
            ThemeSelectionDialog(
                currentTheme = appUiState.themeMode,
                onDismiss = settingsViewModel::dismissThemeDialog,
                // 先从点击位置播放圆形揭示，再落库主题：揭示动画需要旧主题画面作为底图
                onThemeSelected = { mode ->
                    onThemeReveal(settingsUiState.pendingThemeClickPosition)
                    activityViewModel.setThemeMode(mode)
                    settingsViewModel.dismissThemeDialog()
                },
            )
        }

        if (settingsUiState.showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguage = appUiState.language,
                onDismiss = settingsViewModel::dismissLanguageDialog,
                // 先关闭弹窗再切换语言：语言变化会就地重建弹窗内文案，视觉上会看到一次闪跳
                onLanguageSelected = { language ->
                    settingsViewModel.dismissLanguageDialog()
                    activityViewModel.setLanguage(language)
                },
            )
        }
    }
}

// 手动检查的一次性结果；以文案为键重建收集，避免语言切换后仍提示旧语言文案
@Composable
private fun CheckUpdateMessagesEffect(messages: Flow<UpdateMessage>) {
    val context = LocalContext.current
    val upToDateText = stringResource(R.string.settings_update_latest)
    val checkFailedText = stringResource(R.string.settings_update_failed)
    val incompleteReleaseText = stringResource(R.string.settings_update_incomplete)
    LaunchedEffect(upToDateText, checkFailedText, incompleteReleaseText) {
        messages.collect { message ->
            val text = when (message) {
                UpdateMessage.UpToDate -> upToDateText
                UpdateMessage.CheckFailed -> checkFailedText
                UpdateMessage.IncompleteRelease -> incompleteReleaseText
            }
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }
}
