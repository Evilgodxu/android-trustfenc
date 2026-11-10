package com.trustfenc.evilgodxu.screens.home.component.console.fui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// 控制台专用黑白配色：不跟随 Material 色板，只由主题明暗决定黑底或浅底
@Immutable
data class FuiPalette(
    val isLight: Boolean,
    val background: Color,
    val ink: Color,
    val inkMuted: Color,
    val line: Color,
    val panel: Color,
    val solidPanel: Color,
    val inverse: Color,
    val onInverse: Color,
)

private val DarkFuiPalette = FuiPalette(
    isLight = false,
    background = Color.Black,
    ink = Color(0xFFF2F3F5),
    inkMuted = Color(0xFFF2F3F5).copy(alpha = 0.58f),
    line = Color(0xFFF2F3F5).copy(alpha = 0.20f),
    panel = Color(0xFF060607),
    solidPanel = Color(0xFF060607),
    inverse = Color(0xFFF2F3F5),
    onInverse = Color.Black,
)

private val LightFuiPalette = FuiPalette(
    isLight = true,
    background = Color(0xFFF1F2F5),
    ink = Color(0xFF0B0C0E),
    inkMuted = Color(0xFF0B0C0E).copy(alpha = 0.60f),
    line = Color(0xFF0B0C0E).copy(alpha = 0.22f),
    panel = Color.White.copy(alpha = 0.55f),
    solidPanel = Color(0xFFF7F8FA),
    inverse = Color(0xFF0B0C0E),
    onInverse = Color(0xFFF7F8FA),
)

val LocalFuiPalette = staticCompositionLocalOf<FuiPalette> {
    error("FuiPalette is not provided")
}

// 控制台根作用域：深色纯黑，浅色改为模糊渐变底，内容仍保持黑色细线与等宽微标签
@Composable
fun FuiConsoleTheme(content: @Composable () -> Unit) {
    val isLight = MaterialTheme.colorScheme.background.luminance() >= 0.5f
    CompositionLocalProvider(
        LocalFuiPalette provides if (isLight) LightFuiPalette else DarkFuiPalette,
        content = content,
    )
}
