package com.trustfenc.evilgodxu.screens.settings.component.appearance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.data.settings.ThemeMode
import com.trustfenc.evilgodxu.screens.settings.component.SettingsClickableItem
import com.trustfenc.evilgodxu.ui.component.fui.FuiPanel
import com.trustfenc.evilgodxu.ui.component.fui.FuiSectionHeader
import com.trustfenc.evilgodxu.ui.icons.AppIcons

// 外观设置：主题入口，点击位置用于主题切换的扩散动效
@Composable
fun Appearance(themeMode: ThemeMode, onThemeClick: (Offset) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FuiSectionHeader(
            index = "01",
            title = stringResource(R.string.settings_section_appearance),
            tag = stringResource(R.string.fui_tag_appearance),
        )
        FuiPanel(modifier = Modifier.fillMaxWidth()) {
            SettingsClickableItem(
                icon = AppIcons.Palette,
                title = stringResource(R.string.settings_theme_title),
                subtitle = when (themeMode) {
                    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                    ThemeMode.DARK -> stringResource(R.string.theme_dark)
                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                },
                onClick = onThemeClick,
            )
        }
    }
}
