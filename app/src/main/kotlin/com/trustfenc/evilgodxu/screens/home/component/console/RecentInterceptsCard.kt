package com.trustfenc.evilgodxu.screens.home.component.console

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.data.fence.InterceptedApp
import com.trustfenc.evilgodxu.ui.component.fui.FuiPanel
import com.trustfenc.evilgodxu.ui.component.fui.FuiSectionHeader
import com.trustfenc.evilgodxu.ui.component.fui.FuiTag
import com.trustfenc.evilgodxu.ui.component.fui.LocalFuiPalette

private const val MAX_VISIBLE_INTERCEPTS = 4

// 最近拦截：按时间倒序的终端式记录，可视区最多容纳四条，其余在卡片内滚动；空列表时以「NO RECORDS」提示
@Composable
fun RecentInterceptsCard(
    intercepts: List<InterceptedApp>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FuiSectionHeader(
            index = "03",
            title = stringResource(R.string.console_section_recent),
            tag = stringResource(R.string.fui_tag_recent),
        )
        FuiPanel(modifier = Modifier.fillMaxWidth()) {
            if (intercepts.isEmpty()) {
                EmptyIntercepts()
            } else {
                CappedScrollColumn(maxVisibleItems = MAX_VISIBLE_INTERCEPTS) {
                    intercepts.forEachIndexed { index, app ->
                        InterceptEntry(index = index, app = app)
                    }
                }
            }
        }
    }
}

// 可视高度取前 maxVisibleItems 条的实际高度，超出部分靠滚动查看；按条目实测而非固定行高，字体缩放时仍能完整容纳四条
@Composable
private fun CappedScrollColumn(
    maxVisibleItems: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        content = content,
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(maxHeight = Constraints.Infinity)) }
        val height = minOf(
            placeables.take(maxVisibleItems).sumOf { it.height },
            placeables.sumOf { it.height },
        )
        layout(constraints.maxWidth, height) {
            var y = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(0, y)
                y += placeable.height
            }
        }
    }
}

@Composable
private fun InterceptEntry(
    index: Int,
    app: InterceptedApp,
) {
    Column {
        if (index > 0) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = LocalFuiPalette.current.line,
            )
        }
        InterceptRow(index = index, app = app)
    }
}

@Composable
private fun EmptyIntercepts() {
    val palette = LocalFuiPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.fui_empty_records),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            color = palette.inkMuted,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.console_intercept_empty_hint),
            fontSize = 11.sp,
            lineHeight = 17.sp,
            color = palette.inkMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun InterceptRow(
    index: Int,
    app: InterceptedApp,
) {
    val palette = LocalFuiPalette.current
    val relativeTime = remember(app.interceptedAtMillis) {
        DateUtils.getRelativeTimeSpanString(
            app.interceptedAtMillis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE,
        ).toString()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = (index + 1).toString().padStart(2, '0'),
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = palette.inkMuted,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.packageName,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = palette.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = relativeTime,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = palette.inkMuted,
            )
            Spacer(Modifier.height(4.dp))
            FuiTag(text = stringResource(R.string.fui_tag_removed))
        }
    }
}
