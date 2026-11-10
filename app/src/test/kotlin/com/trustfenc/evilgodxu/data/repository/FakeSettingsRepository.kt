package com.trustfenc.evilgodxu.data.repository

import com.trustfenc.evilgodxu.data.settings.AppLanguage
import com.trustfenc.evilgodxu.data.settings.SettingsState
import com.trustfenc.evilgodxu.data.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// 内存实现：以两个 StateFlow 充当 DataStore，行为等价于「写入即经数据流回流」
class FakeSettingsRepository(
    initialTheme: ThemeMode = ThemeMode.SYSTEM,
    initialLanguage: AppLanguage = AppLanguage.SYSTEM,
) : SettingsRepository {

    private val settingsState = MutableStateFlow(SettingsState(themeMode = initialTheme))
    private val languageState = MutableStateFlow(initialLanguage)

    override val settings: Flow<SettingsState> = settingsState.asStateFlow()
    override val appLanguage: Flow<AppLanguage> = languageState.asStateFlow()

    var savedTheme: ThemeMode? = null
        private set
    var savedLanguage: AppLanguage? = null
        private set

    override suspend fun saveThemeMode(mode: ThemeMode) {
        savedTheme = mode
        settingsState.value = SettingsState(themeMode = mode)
    }

    override suspend fun setAppLanguage(language: AppLanguage) {
        savedLanguage = language
        languageState.value = language
    }
}
