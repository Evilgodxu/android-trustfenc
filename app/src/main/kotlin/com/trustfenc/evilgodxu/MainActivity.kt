package com.trustfenc.evilgodxu

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.trustfenc.evilgodxu.localization.LocalizationManager
import com.trustfenc.evilgodxu.localization.ProvideLocalizedContext
import com.trustfenc.evilgodxu.navigation.AppNavHost
import com.trustfenc.evilgodxu.theme.MyApplicationTheme
import com.trustfenc.evilgodxu.ui.component.dialog.UpdateDialog
import com.trustfenc.evilgodxu.update.AppUpdateViewModel
import com.trustfenc.evilgodxu.update.LocalAppUpdateViewModel

// Activity 只做入口：挂载导航图与全局副作用，不持有状态字段、不参与业务
class MainActivity : ComponentActivity() {

    // 手动 DI：经 Application 容器取依赖，ViewModel 以工厂注入构造参数
    private val appContainer: AppContainer
        get() = (application as App).container

    private val localizationManager: LocalizationManager
        get() = appContainer.localizationManager

    private val mainViewModel: MainViewModel by viewModels {
        viewModelFactory {
            initializer {
                MainViewModel(
                    settingsRepository = appContainer.settingsRepository,
                    appVersion = appContainer.appVersion,
                )
            }
        }
    }

    private val updateViewModel: AppUpdateViewModel by viewModels {
        viewModelFactory {
            initializer {
                AppUpdateViewModel(
                    updateCheckRepository = appContainer.updateCheckRepository,
                    coordinator = appContainer.updateDownloadCoordinator,
                    installer = appContainer.apkInstaller,
                    appVersion = appContainer.appVersion,
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // 全局副作用：按窗口方向显隐系统栏
            SystemBarsVisibilityEffect()
            CompositionLocalProvider(
                LocalMainViewModel provides mainViewModel,
                LocalAppUpdateViewModel provides updateViewModel,
            ) {
                ProvideLocalizedContext(localizationManager) {
                    MainContent()
                }
            }
        }
    }

    @Composable
    private fun MainContent() {
        MyApplicationTheme {
            val updateUiState by updateViewModel.uiState.collectAsStateWithLifecycle()
            // 回到前台：若仍有待安装的更新包，重新弹出安装确认对话框
            LifecycleResumeEffect(Unit) {
                updateViewModel.onForeground()
                onPauseOrDispose { }
            }
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                AppNavHost()
            }
            // 更新对话框挂在应用级：两个页面共用同一份更新状态
            if (updateUiState.dialogVisible) {
                UpdateDialog(
                    state = updateUiState,
                    onDismiss = updateViewModel::dismissDialog,
                    onStartDownload = updateViewModel::startDownload,
                    onRetry = updateViewModel::retryDownload,
                    onInstall = updateViewModel::install,
                )
            }
        }
    }
}

// 按窗口方向显隐系统栏的全局副作用；横屏隐藏、竖屏显示
@Composable
private fun SystemBarsVisibilityEffect() {
    val view = LocalView.current
    if (view.isInEditMode) return
    val orientation = LocalConfiguration.current.orientation
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
