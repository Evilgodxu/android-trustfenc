package com.trustfenc.evilgodxu.screens.settings.assembly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.trustfenc.evilgodxu.AppUiState
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.screens.settings.component.appearance.Appearance
import com.trustfenc.evilgodxu.screens.settings.component.info.AppInfo
import com.trustfenc.evilgodxu.screens.settings.component.language.Language
import com.trustfenc.evilgodxu.ui.component.ContentPane
import com.trustfenc.evilgodxu.ui.component.fui.FuiBackdrop
import com.trustfenc.evilgodxu.ui.component.fui.FuiIconButton
import com.trustfenc.evilgodxu.ui.component.fui.FuiTopBar
import com.trustfenc.evilgodxu.ui.component.fui.LocalFuiPalette
import com.trustfenc.evilgodxu.ui.icons.AppIcons
import com.trustfenc.evilgodxu.windowsize.WindowSizeClass
import com.trustfenc.evilgodxu.windowsize.contentMaxWidth

// 设置页组装器：与控制台共用 FUI 背景与细线面板，单栏内容区随窗口收窄行宽
@Composable
fun SettingsAssembly(
    windowSizeClass: WindowSizeClass,
    uiState: AppUiState,
    onBack: () -> Unit,
    onShowThemeDialog: (Offset) -> Unit,
    onShowLanguageDialog: () -> Unit,
    onCheckForUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalFuiPalette.current
    Scaffold(
        modifier = modifier,
        containerColor = palette.background,
        topBar = {
            FuiTopBar(
                title = stringResource(R.string.fui_title_settings),
                navigationIcon = {
                    FuiIconButton(
                        icon = AppIcons.ChevronLeft,
                        contentDescription = stringResource(R.string.back),
                        onClick = onBack,
                    )
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            FuiBackdrop(modifier = Modifier.fillMaxSize())
            ContentPane(
                maxContentWidth = windowSizeClass.contentMaxWidth,
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding),
            ) {
                Appearance(themeMode = uiState.themeMode, onThemeClick = onShowThemeDialog)
                Language(language = uiState.language, onShowDialog = onShowLanguageDialog)
                AppInfo(version = uiState.version, onCheckForUpdate = onCheckForUpdate)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
