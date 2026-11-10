package com.trustfenc.evilgodxu.screens.home.component.console

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.data.fence.FenceStatus
import com.trustfenc.evilgodxu.ui.component.SectionCard
import com.trustfenc.evilgodxu.ui.icons.AppIcons
import kotlinx.coroutines.delay

// 守护概况：基线规模、累计拦截与本次守护时长三栏并排
@Composable
fun FenceOverviewCard(
    status: FenceStatus,
    baselineCount: Int,
    interceptedCount: Int,
    modifier: Modifier = Modifier,
) {
    val guardingSince = (status as? FenceStatus.Guarding)?.sinceMillis

    SectionCard(title = stringResource(R.string.console_section_overview), modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatItem(
                icon = AppIcons.Apps,
                tint = MaterialTheme.colorScheme.primary,
                value = baselineCount.toString(),
                label = stringResource(R.string.console_stat_baseline),
            )
            StatDivider()
            StatItem(
                icon = AppIcons.Block,
                tint = MaterialTheme.colorScheme.error,
                value = interceptedCount.toString(),
                label = stringResource(R.string.console_stat_intercepted),
            )
            StatDivider()
            StatItem(
                icon = AppIcons.Schedule,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                value = guardingUptime(guardingSince),
                label = stringResource(R.string.console_stat_uptime),
            )
        }
    }
}

@Composable
private fun RowScope.StatItem(
    icon: ImageVector,
    tint: Color,
    value: String,
    label: String,
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = tint,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatDivider() {
    VerticalDivider(
        modifier = Modifier.height(52.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
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
