package com.trustfenc.evilgodxu

import com.trustfenc.evilgodxu.data.repository.FakeSettingsRepository
import com.trustfenc.evilgodxu.data.settings.AppLanguage
import com.trustfenc.evilgodxu.data.settings.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// 应用级状态持有者：验证「仓库 → UI 状态」的回流方向，以及写入后经同一路径回到 UI
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

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
    fun `仓库当前设置回流为初始 UI 状态`() = runTest(mainDispatcher) {
        val repository = FakeSettingsRepository(ThemeMode.DARK, AppLanguage.ENGLISH)

        val viewModel = MainViewModel(repository, appVersion = "1.0.0")
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
        assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.language)
        assertEquals("1.0.0", viewModel.uiState.value.version)
    }

    @Test
    fun `写入主题经仓库回流更新状态`() = runTest(mainDispatcher) {
        val repository = FakeSettingsRepository(ThemeMode.SYSTEM)
        val viewModel = MainViewModel(repository, appVersion = "1.0.0")
        advanceUntilIdle()

        viewModel.setThemeMode(ThemeMode.LIGHT)
        advanceUntilIdle()

        assertEquals(ThemeMode.LIGHT, repository.savedTheme)
        assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.themeMode)
        // 语言未被触碰，不应随之变化
        assertEquals(AppLanguage.SYSTEM, viewModel.uiState.value.language)
    }

    @Test
    fun `写入语言经仓库回流更新状态`() = runTest(mainDispatcher) {
        val repository = FakeSettingsRepository()
        val viewModel = MainViewModel(repository, appVersion = "1.0.0")
        advanceUntilIdle()

        viewModel.setLanguage(AppLanguage.CHINESE)
        advanceUntilIdle()

        assertEquals(AppLanguage.CHINESE, repository.savedLanguage)
        assertEquals(AppLanguage.CHINESE, viewModel.uiState.value.language)
        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)
    }
}
