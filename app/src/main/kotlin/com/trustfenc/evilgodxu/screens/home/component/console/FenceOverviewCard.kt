package com.trustfenc.evilgodxu.screens.home.component.console

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.data.fence.FenceStatus
import com.trustfenc.evilgodxu.screens.home.component.console.fui.FuiPanel
import com.trustfenc.evilgodxu.screens.home.component.console.fui.FuiSectionHeader
import com.trustfenc.evilgodxu.screens.home.component.console.fui.LocalFuiPalette
import com.trustfenc.evilgodxu.ui.icons.AppIcons
import kotlinx.coroutines.delay

// 守护概况：基线规模、累计拦截与按天计的守护时长，三栏等宽仪表
@Composable
fun FenceOverviewCard(
    status: FenceStatus,
    baselineCount: Int,
    interceptedCount: Int,
    modifier: Modifier = Modifier,
) {
    val palette = LocalFuiPalette.current
    val guardingSince = (status as? FenceStatus.Guarding)?.sinceMillis

    Column(modifier = modifier.fillMaxWidth()) {
        FuiSectionHeader(
            index = "02",
            title = stringResource(R.string.console_section_overview),
            tag = stringResource(R.string.fui_tag_overview),
        )
        FuiPanel(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatCell(
                    icon = AppIcons.Apps,
                    code = stringResource(R.string.fui_stat_baseline),
                    value = baselineCount.toString(),
                    label = stringResource(R.string.console_stat_baseline),
                )
                CellDivider()
                StatCell(
                    icon = AppIcons.Block,
                    code = stringResource(R.string.fui_stat_blocked),
                    value = interceptedCount.toString(),
                    label = stringResource(R.string.console_stat_intercepted),
                )
                CellDivider()
                StatCell(
                    icon = AppIcons.Schedule,
                    code = stringResource(R.string.fui_stat_uptime),
                    value = guardingUptime(guardingSince),
                    label = stringResource(R.string.console_stat_uptime),
                )
            }
        }
    }
}

@Composable
private fun RowScope.StatCell(
    icon: ImageVector,
    code: String,
    value: String,
    label: String,
) {
    val palette = LocalFuiPalette.current
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = palette.inkMuted,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = palette.ink,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp,
            color = palette.inkMuted,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = palette.inkMuted,
        )
    }
}

@Composable
private fun CellDivider() {
    val palette = LocalFuiPalette.current
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(62.dp)
            .background(palette.line),
    )
}

// 守护时长按天计：满一天显示天数，不足一天给出明确的「不足1天」，每分钟刷新一次即可覆盖跨天
@Composable
private fun guardingUptime(guardingSince: Long?): String {
    if (guardingSince == null) return "—"
    var nowMillis by remember(guardingSince) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(guardingSince) {
        while (true) {
            delay(60_000)
            nowMillis = System.currentTimeMillis()
        }
    }
    val days = (nowMillis - guardingSince).coerceAtLeast(0) / MILLIS_PER_DAY
    return if (days == 0L) {
        stringResource(R.string.console_uptime_less_than_day)
    } else {
        stringResource(R.string.console_uptime_days, days)
    }
}

private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1_000L
