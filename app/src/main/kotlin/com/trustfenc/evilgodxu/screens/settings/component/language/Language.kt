package com.trustfenc.evilgodxu.screens.settings.component.language

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.data.settings.AppLanguage
import com.trustfenc.evilgodxu.screens.settings.component.SettingsClickableItem
import com.trustfenc.evilgodxu.ui.component.SectionCard
import com.trustfenc.evilgodxu.ui.icons.AppIcons

@Composable
fun Language(language: AppLanguage, onShowDialog: () -> Unit) {
    SectionCard(title = stringResource(R.string.settings_section_language)) {
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
