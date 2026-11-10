package com.trustfenc.evilgodxu.screens.settings.component.language

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.data.settings.AppLanguage
import com.trustfenc.evilgodxu.screens.settings.component.SettingsClickableItem
import com.trustfenc.evilgodxu.ui.component.fui.FuiPanel
import com.trustfenc.evilgodxu.ui.component.fui.FuiSectionHeader
import com.trustfenc.evilgodxu.ui.icons.AppIcons

// 语言设置：当前语言作为副标题直接呈现
@Composable
fun Language(language: AppLanguage, onShowDialog: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FuiSectionHeader(
            index = "02",
            title = stringResource(R.string.settings_section_language),
            tag = stringResource(R.string.fui_tag_language),
        )
        FuiPanel(modifier = Modifier.fillMaxWidth()) {
            SettingsClickableItem(
                icon = AppIcons.Language,
                title = stringResource(R.string.settings_language_title),
                subtitle = when (language) {
                    AppLanguage.SYSTEM -> stringResource(R.string.language_system)
                    AppLanguage.CHINESE -> stringResource(R.string.language_chinese)
                    AppLanguage.ENGLISH -> stringResource(R.string.language_english)
                },
                onClick = { onShowDialog() },
            )
        }
    }
}
