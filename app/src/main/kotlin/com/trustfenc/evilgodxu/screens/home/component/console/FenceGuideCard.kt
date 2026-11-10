package com.trustfenc.evilgodxu.screens.home.component.console

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.ui.component.SectionCard

// 工作方式：把修筑 → 建立基线 → 监听安装 → 清除越界应用的四步流程讲清楚
@Composable
fun FenceGuideCard(modifier: Modifier = Modifier) {
    SectionCard(title = stringResource(R.string.console_section_guide), modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            GuideStepRow(
                number = 1,
                title = stringResource(R.string.console_guide_scan_title),
                description = stringResource(R.string.console_guide_scan_desc),
            )
            GuideStepRow(
                number = 2,
                title = stringResource(R.string.console_guide_baseline_title),
                description = stringResource(R.string.console_guide_baseline_desc),
            )
            GuideStepRow(
                number = 3,
                title = stringResource(R.string.console_guide_listen_title),
                description = stringResource(R.string.console_guide_listen_desc),
            )
            GuideStepRow(
                number = 4,
                title = stringResource(R.string.console_guide_remove_title),
                description = stringResource(R.string.console_guide_remove_desc),
            )
        }
    }
}

@Composable
private fun GuideStepRow(
    number: Int,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
