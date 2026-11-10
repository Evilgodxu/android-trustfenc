package com.trustfenc.evilgodxu.screens.home.component.console

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.data.fence.FenceStatus
import com.trustfenc.evilgodxu.ui.component.SectionLabel
import com.trustfenc.evilgodxu.ui.icons.AppIcons

// 控制台主卡：状态徽标、篱笆徽章、状态文案与唯一主操作按钮
@Composable
fun FenceStatusCard(
    status: FenceStatus,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val guarding = status is FenceStatus.Guarding
    val busy = status is FenceStatus.Building || status is FenceStatus.Dismantling

    Column(modifier = modifier.fillMaxWidth()) {
        SectionLabel(stringResource(R.string.console_section_status))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                MaterialTheme.colorScheme.surface,
                            ),
                        ),
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StatusPill(status = status)
                Spacer(Modifier.height(20.dp))
                FenceEmblem(active = guarding)
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(status.titleRes()),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.5.sp,
                    ),
                    color = if (guarding) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(status.subtitleRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onPrimaryAction,
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = if (guarding) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = LocalContentColor.current,
                        )
                    } else {
                        Icon(
                            imageVector = AppIcons.Fence,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(status.actionRes()),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: FenceStatus) {
    val color = when (status) {
        FenceStatus.Idle -> MaterialTheme.colorScheme.onSurfaceVariant
        FenceStatus.Building, FenceStatus.Dismantling -> MaterialTheme.colorScheme.tertiary
        is FenceStatus.Guarding -> MaterialTheme.colorScheme.primary
    }
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
        Text(
            text = stringResource(status.pillRes()),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}

// 篱笆徽章：守护中以外圈脉冲表达「正在生效」，静止态只留淡底与细描边
@Composable
private fun FenceEmblem(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val haloAlpha = if (active) {
        val transition = rememberInfiniteTransition(label = "fenceHalo")
        transition.animateFloat(
            initialValue = 0.10f,
            targetValue = 0.26f,
            animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
            label = "fenceHaloAlpha",
        ).value
    } else {
        0.10f
    }
    val accent = if (active) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .size(112.dp)
            .drawBehind {
                drawCircle(color = accent.copy(alpha = haloAlpha))
                drawCircle(
                    color = accent.copy(alpha = 0.18f),
                    radius = size.minDimension / 2f * 0.82f,
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.Fence,
            contentDescription = null,
            modifier = Modifier.size(52.dp),
            tint = accent,
        )
    }
}

private fun FenceStatus.titleRes(): Int = when (this) {
    FenceStatus.Idle -> R.string.console_title_idle
    FenceStatus.Building -> R.string.console_title_building
    is FenceStatus.Guarding -> R.string.console_title_guarding
    FenceStatus.Dismantling -> R.string.console_title_dismantling
}

private fun FenceStatus.subtitleRes(): Int = when (this) {
    FenceStatus.Idle -> R.string.console_subtitle_idle
    FenceStatus.Building -> R.string.console_subtitle_building
    is FenceStatus.Guarding -> R.string.console_subtitle_guarding
    FenceStatus.Dismantling -> R.string.console_subtitle_dismantling
}

private fun FenceStatus.pillRes(): Int = when (this) {
    FenceStatus.Idle -> R.string.console_status_idle
    FenceStatus.Building -> R.string.console_status_building
    is FenceStatus.Guarding -> R.string.console_status_guarding
    FenceStatus.Dismantling -> R.string.console_status_dismantling
}

private fun FenceStatus.actionRes(): Int = when (this) {
    FenceStatus.Idle -> R.string.console_action_build
    FenceStatus.Building -> R.string.console_action_building
    is FenceStatus.Guarding -> R.string.console_action_dismantle
    FenceStatus.Dismantling -> R.string.console_action_dismantling
}
