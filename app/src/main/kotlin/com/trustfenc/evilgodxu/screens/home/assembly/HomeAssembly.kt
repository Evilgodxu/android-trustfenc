package com.trustfenc.evilgodxu.screens.home.assembly

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.screens.home.component.about.About
import com.trustfenc.evilgodxu.screens.home.component.welcome.Welcome
import com.trustfenc.evilgodxu.ui.component.AppTopBar
import com.trustfenc.evilgodxu.ui.component.ContentPane
import com.trustfenc.evilgodxu.ui.icons.AppIcons
import com.trustfenc.evilgodxu.windowsize.WindowSizeClass
import com.trustfenc.evilgodxu.windowsize.contentMaxWidth

// 首页组装器：骨架为 Scaffold + 单栏内容区，形态差异经 ContentPane 的行宽上限表达
@Composable
fun HomeAssembly(
    windowSizeClass: WindowSizeClass,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.home_title),
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
            Welcome()
            About()
        }
    }
}
