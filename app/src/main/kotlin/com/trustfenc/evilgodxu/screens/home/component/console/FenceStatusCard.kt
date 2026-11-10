package com.trustfenc.evilgodxu.screens.home.component.console

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.data.fence.FenceStatus
import com.trustfenc.evilgodxu.data.shizuku.ShizukuState
import com.trustfenc.evilgodxu.ui.component.fui.FuiActionButton
import com.trustfenc.evilgodxu.ui.component.fui.FuiPanel
import com.trustfenc.evilgodxu.ui.component.fui.FuiReticle
import com.trustfenc.evilgodxu.ui.component.fui.FuiSectionHeader
import com.trustfenc.evilgodxu.ui.component.fui.FuiTag
import com.trustfenc.evilgodxu.ui.component.fui.LocalFuiPalette
import com.trustfenc.evilgodxu.ui.icons.AppIcons

// 控制台主卡：状态标签、准星徽章、状态码与唯一主操作，整体保持 FUI 的黑白细线语言
@Composable
fun FenceStatusCard(
    status: FenceStatus,
    shizuku: ShizukuState,
    serviceRunning: Boolean,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalFuiPalette.current
    val guarding = status is FenceStatus.Guarding
    val busy = status is FenceStatus.Building || status is FenceStatus.Dismantling

    Column(modifier = modifier.fillMaxWidth()) {
        FuiSectionHeader(
            index = "01",
            title = stringResource(R.string.console_section_status),
            tag = stringResource(R.string.fui_tag_guard_status),
        )
        FuiPanel(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FuiTag(
                        text = stringResource(status.pillRes()),
                        inverted = guarding,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = stringResource(status.systemRes()),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        letterSpacing = 1.5.sp,
                        color = palette.inkMuted,
                    )
                }
                Spacer(Modifier.height(24.dp))
                FuiReticle(active = guarding, icon = AppIcons.Fence)
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(status.codeRes()),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    letterSpacing = 5.sp,
                    color = if (guarding) palette.ink else palette.inkMuted,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(status.titleRes()),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp,
                    color = palette.ink,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(status.subtitleRes()),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = palette.inkMuted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                FuiActionButton(
                    label = stringResource(status.actionRes()),
                    tag = stringResource(status.actionTagRes()),
                    filled = status is FenceStatus.Idle,
                    enabled = !busy,
                    busy = busy,
                    onClick = onPrimaryAction,
                )
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = palette.line)
                Spacer(Modifier.height(12.dp))
                ShizukuStatusRow(shizuku = shizuku)
                Spacer(Modifier.height(8.dp))
                WatchStatusRow(serviceRunning = serviceRunning)
            }
        }
    }
}

// 监听链路状态：服务心跳中断说明广播与兜底巡检都没有在执行
@Composable
private fun WatchStatusRow(serviceRunning: Boolean) {
    RuntimeStatusRow(
        label = stringResource(R.string.fui_watch_label),
        code = stringResource(
            if (serviceRunning) R.string.fui_watch_online else R.string.fui_watch_offline,
        ),
        active = serviceRunning,
    )
}

// 运行条件：Shizuku 的就绪状态直接决定修筑与卸载能否执行
@Composable
private fun ShizukuStatusRow(shizuku: ShizukuState) {
    RuntimeStatusRow(
        label = stringResource(R.string.fui_shizuku_label),
        code = stringResource(shizuku.codeRes()),
        active = shizuku == ShizukuState.Ready,
    )
}

@Composable
private fun RuntimeStatusRow(label: String, code: String, active: Boolean) {
    val palette = LocalFuiPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp,
            color = palette.inkMuted,
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(if (active) palette.ink else palette.inkMuted),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp,
            color = if (active) palette.ink else palette.inkMuted,
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

private fun FenceStatus.systemRes(): Int = when (this) {
    FenceStatus.Idle -> R.string.fui_system_standby
    FenceStatus.Building -> R.string.fui_system_building
    is FenceStatus.Guarding -> R.string.fui_system_online
    FenceStatus.Dismantling -> R.string.fui_system_dismantling
}

private fun FenceStatus.codeRes(): Int = when (this) {
    FenceStatus.Idle -> R.string.fui_code_standby
    FenceStatus.Building -> R.string.fui_code_build
    is FenceStatus.Guarding -> R.string.fui_code_active
    FenceStatus.Dismantling -> R.string.fui_code_dismantle
}

private fun FenceStatus.actionRes(): Int = when (this) {
    FenceStatus.Idle -> R.string.console_action_build
    FenceStatus.Building -> R.string.console_action_building
    is FenceStatus.Guarding -> R.string.console_action_dismantle
    FenceStatus.Dismantling -> R.string.console_action_dismantling
}

private fun FenceStatus.actionTagRes(): Int = when (this) {
    FenceStatus.Idle -> R.string.fui_action_build_tag
    FenceStatus.Building -> R.string.fui_action_build_tag
    is FenceStatus.Guarding -> R.string.fui_action_dismantle_tag
    FenceStatus.Dismantling -> R.string.fui_action_dismantle_tag
}

private fun ShizukuState.codeRes(): Int = when (this) {
    ShizukuState.NotInstalled -> R.string.fui_shizuku_missing
    ShizukuState.NotRunning -> R.string.fui_shizuku_offline
    ShizukuState.PermissionRequired -> R.string.fui_shizuku_unauthorized
    ShizukuState.PermissionDenied -> R.string.fui_shizuku_denied
    ShizukuState.Ready -> R.string.fui_shizuku_ready
}
