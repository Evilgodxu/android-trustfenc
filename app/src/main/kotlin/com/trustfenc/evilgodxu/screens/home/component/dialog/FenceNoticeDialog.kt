package com.trustfenc.evilgodxu.screens.home.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.data.fence.FenceFailure
import com.trustfenc.evilgodxu.ui.component.fui.FuiPanel
import com.trustfenc.evilgodxu.ui.component.fui.FuiSmallButton
import com.trustfenc.evilgodxu.ui.component.fui.LocalFuiPalette

// 修筑失败提示：说明运行条件并给出可执行的下一步
@Composable
fun FenceNoticeDialog(
    failure: FenceFailure,
    onDismiss: () -> Unit,
    onOpenShizuku: () -> Unit,
) {
    val palette = LocalFuiPalette.current

    val actionLabelRes = when (failure) {
        FenceFailure.ShizukuNotRunning, FenceFailure.PermissionDenied ->
            R.string.fence_notice_open_shizuku
        FenceFailure.ShizukuNotInstalled, FenceFailure.ScanFailed, FenceFailure.GuardServiceFailed ->
            null
    }
    val actionTagRes = when (failure) {
        FenceFailure.ShizukuNotRunning, FenceFailure.PermissionDenied ->
            R.string.fui_action_shizuku_tag
        FenceFailure.ShizukuNotInstalled, FenceFailure.ScanFailed, FenceFailure.GuardServiceFailed ->
            R.string.fui_action_ok_tag
    }
    val action = when (failure) {
        FenceFailure.ShizukuNotRunning, FenceFailure.PermissionDenied -> onOpenShizuku
        FenceFailure.ShizukuNotInstalled, FenceFailure.ScanFailed, FenceFailure.GuardServiceFailed ->
            null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            FuiPanel(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth(),
                solid = true,
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(palette.ink),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.fence_notice_warn),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            color = palette.inkMuted,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "!",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.ink,
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = stringResource(failure.titleRes()),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = palette.ink,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(failure.messageRes()),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = palette.inkMuted,
                    )
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = palette.line)
                    Spacer(Modifier.height(16.dp))
                    if (actionLabelRes != null && action != null) {
                        Row {
                            FuiSmallButton(
                                label = stringResource(actionLabelRes),
                                tag = stringResource(actionTagRes),
                                filled = false,
                                onClick = action,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(10.dp))
                            FuiSmallButton(
                                label = stringResource(R.string.fence_notice_dismiss),
                                tag = stringResource(R.string.fui_action_ok_tag),
                                filled = true,
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        FuiSmallButton(
                            label = stringResource(R.string.fence_notice_dismiss),
                            tag = stringResource(R.string.fui_action_ok_tag),
                            filled = true,
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

private fun FenceFailure.titleRes(): Int = when (this) {
    FenceFailure.ShizukuNotInstalled -> R.string.fence_failure_shizuku_missing_title
    FenceFailure.ShizukuNotRunning -> R.string.fence_failure_shizuku_offline_title
    FenceFailure.PermissionDenied -> R.string.fence_failure_permission_title
    FenceFailure.ScanFailed -> R.string.fence_failure_scan_title
    FenceFailure.GuardServiceFailed -> R.string.fence_failure_service_title
}

private fun FenceFailure.messageRes(): Int = when (this) {
    FenceFailure.ShizukuNotInstalled -> R.string.fence_failure_shizuku_missing_message
    FenceFailure.ShizukuNotRunning -> R.string.fence_failure_shizuku_offline_message
    FenceFailure.PermissionDenied -> R.string.fence_failure_permission_message
    FenceFailure.ScanFailed -> R.string.fence_failure_scan_message
    FenceFailure.GuardServiceFailed -> R.string.fence_failure_service_message
}
