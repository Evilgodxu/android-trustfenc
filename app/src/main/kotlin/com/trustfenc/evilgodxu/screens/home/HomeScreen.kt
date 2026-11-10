package com.trustfenc.evilgodxu.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.trustfenc.evilgodxu.screens.home.assembly.HomeAssembly
import com.trustfenc.evilgodxu.update.LocalAppUpdateViewModel
import com.trustfenc.evilgodxu.windowsize.rememberWindowSizeClass

// 页面入口：承载跨形态副作用，窗口尺寸类在此计算后作为参数下传，不含布局
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DailyUpdateCheckEffect()
    HomeAssembly(
        windowSizeClass = rememberWindowSizeClass(),
        onOpenSettings = onOpenSettings,
        modifier = modifier,
    )
}

// 每日检查是应用级动作，故不进页面 ViewModel，避免仅做转发的中间层
@Composable
private fun DailyUpdateCheckEffect() {
    val updateViewModel = LocalAppUpdateViewModel.current
    LaunchedEffect(Unit) { updateViewModel.checkDailyOnce() }
}
