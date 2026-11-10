package com.trustfenc.evilgodxu.screens.home.assembly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.screens.home.HomeUiState
import com.trustfenc.evilgodxu.screens.home.component.console.FenceGuideCard
import com.trustfenc.evilgodxu.screens.home.component.console.FenceOverviewCard
import com.trustfenc.evilgodxu.screens.home.component.console.FenceStatusCard
import com.trustfenc.evilgodxu.screens.home.component.console.RecentInterceptsCard
import com.trustfenc.evilgodxu.ui.component.AppTopBar
import com.trustfenc.evilgodxu.ui.component.ContentPane
import com.trustfenc.evilgodxu.ui.icons.AppIcons
import com.trustfenc.evilgodxu.windowsize.WindowSizeClass
import com.trustfenc.evilgodxu.windowsize.contentMaxWidth

// 控制台组装器：功能区块固定，宽窗把「状态 + 概况」与「拦截 + 说明」并排，窄窗保持单列
@Composable
fun HomeAssembly(
    windowSizeClass: WindowSizeClass,
    uiState: HomeUiState,
    onOpenSettings: () -> Unit,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.app_name),
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = AppIcons.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        ContentPane(
            maxContentWidth = windowSizeClass.contentMaxWidth,
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .padding(innerPadding),
        ) {
            if (windowSizeClass == WindowSizeClass.Compact) {
                CompactConsole(uiState = uiState, onPrimaryAction = onPrimaryAction)
            } else {
                WideConsole(uiState = uiState, onPrimaryAction = onPrimaryAction)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CompactConsole(
    uiState: HomeUiState,
    onPrimaryAction: () -> Unit,
) {
    FenceStatusCard(status = uiState.status, onPrimaryAction = onPrimaryAction)
    FenceOverviewCard(
        status = uiState.status,
        baselineCount = uiState.baselineCount,
        interceptedCount = uiState.interceptedCount,
    )
    RecentInterceptsCard(intercepts = uiState.recentIntercepts)
    FenceGuideCard()
}

@Composable
private fun WideConsole(
    uiState: HomeUiState,
    onPrimaryAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            FenceStatusCard(status = uiState.status, onPrimaryAction = onPrimaryAction)
            FenceOverviewCard(
                status = uiState.status,
                baselineCount = uiState.baselineCount,
                interceptedCount = uiState.interceptedCount,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            RecentInterceptsCard(intercepts = uiState.recentIntercepts)
            FenceGuideCard()
        }
    }
}
