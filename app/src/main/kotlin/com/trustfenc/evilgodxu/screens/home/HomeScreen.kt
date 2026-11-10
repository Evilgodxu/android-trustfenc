package com.trustfenc.evilgodxu.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.trustfenc.evilgodxu.App
import com.trustfenc.evilgodxu.screens.home.assembly.HomeAssembly
import com.trustfenc.evilgodxu.screens.home.component.dialog.DismantleFenceDialog
import com.trustfenc.evilgodxu.update.LocalAppUpdateViewModel
import com.trustfenc.evilgodxu.windowsize.rememberWindowSizeClass

// 页面入口：承载跨形态副作用与页面级弹层，窗口尺寸类在此计算后作为参数下传
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DailyUpdateCheckEffect()
    val viewModel = homeViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeAssembly(
        windowSizeClass = rememberWindowSizeClass(),
        uiState = uiState,
        onOpenSettings = onOpenSettings,
        onPrimaryAction = viewModel::onPrimaryAction,
        modifier = modifier,
    )

    if (uiState.showDismantleConfirm) {
        DismantleFenceDialog(
            onConfirm = viewModel::confirmDismantle,
            onDismiss = viewModel::dismissDismantleConfirm,
        )
    }
}

// 手动 DI：页面级 ViewModel 以工厂注入仓库，作用域跟随导航条目
@Composable
private fun homeViewModel(): HomeViewModel {
    val repository = (LocalContext.current.applicationContext as App).container.fenceRepository
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer { HomeViewModel(repository) }
        },
    )
    return viewModel
}

// 每日检查是应用级动作，故不进页面 ViewModel，避免仅做转发的中间层
@Composable
private fun DailyUpdateCheckEffect() {
    val updateViewModel = LocalAppUpdateViewModel.current
    LaunchedEffect(Unit) { updateViewModel.checkDailyOnce() }
}
