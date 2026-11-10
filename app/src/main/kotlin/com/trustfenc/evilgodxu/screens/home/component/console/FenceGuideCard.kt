package com.trustfenc.evilgodxu.screens.home.component.console

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.screens.home.component.console.fui.FuiPanel
import com.trustfenc.evilgodxu.screens.home.component.console.fui.FuiSectionHeader
import com.trustfenc.evilgodxu.screens.home.component.console.fui.LocalFuiPalette

// 工作方式：扫描 → 建立基线 → 监听安装 → 清除越界应用的四步流程
@Composable
fun FenceGuideCard(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        FuiSectionHeader(
            index = "04",
            title = stringResource(R.string.console_section_guide),
            tag = stringResource(R.string.fui_tag_guide),
        )
        FuiPanel(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                GuideStepRow(
                    number = "01",
                    title = stringResource(R.string.console_guide_scan_title),
                    description = stringResource(R.string.console_guide_scan_desc),
                )
                StepDivider()
                GuideStepRow(
                    number = "02",
                    title = stringResource(R.string.console_guide_baseline_title),
                    description = stringResource(R.string.console_guide_baseline_desc),
                )
                StepDivider()
                GuideStepRow(
                    number = "03",
                    title = stringResource(R.string.console_guide_listen_title),
                    description = stringResource(R.string.console_guide_listen_desc),
                )
                StepDivider()
                GuideStepRow(
                    number = "04",
                    title = stringResource(R.string.console_guide_remove_title),
                    description = stringResource(R.string.console_guide_remove_desc),
                )
            }
        }
    }
}

@Composable
private fun GuideStepRow(
    number: String,
    title: String,
    description: String,
) {
    val palette = LocalFuiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .border(1.dp, palette.line, CutCornerShape(2.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                color = palette.ink,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = palette.ink,
            )
            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = palette.inkMuted,
            )
        }
    }
}

@Composable
private fun StepDivider() {
    val palette = LocalFuiPalette.current
    HorizontalDivider(
        modifier = Modifier.padding(start = 40.dp),
        color = palette.line,
    )
}
