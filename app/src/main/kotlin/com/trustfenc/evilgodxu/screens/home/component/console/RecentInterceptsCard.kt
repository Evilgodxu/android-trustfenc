package com.trustfenc.evilgodxu.screens.home.component.console

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.data.fence.InterceptedApp
import com.trustfenc.evilgodxu.ui.component.SectionCard
import com.trustfenc.evilgodxu.ui.icons.AppIcons

// 最近拦截：按时间倒序列出被卸载的应用，空列表时给出守护中的空状态
@Composable
fun RecentInterceptsCard(
    intercepts: List<InterceptedApp>,
    modifier: Modifier = Modifier,
) {
    SectionCard(title = stringResource(R.string.console_section_recent), modifier = modifier) {
        if (intercepts.isEmpty()) {
            EmptyIntercepts()
        } else {
            intercepts.forEachIndexed { index, app ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
                InterceptRow(app)
            }
        }
    }
}

@Composable
private fun EmptyIntercepts() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = AppIcons.ShieldCheck,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.console_intercept_empty_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.console_intercept_empty_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun InterceptRow(app: InterceptedApp) {
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Block,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.packageName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = relativeTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.console_intercept_tag),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}
