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
import com.trustfenc.evilgodxu.ui.component.fui.FuiPanel
import com.trustfenc.evilgodxu.ui.component.fui.FuiSmallButton
import com.trustfenc.evilgodxu.ui.component.fui.LocalFuiPalette

// 拆除确认：清空基线不可撤销，用 FUI 警示面板承接破坏性操作
@Composable
fun DismantleFenceDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalFuiPalette.current
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
                            text = stringResource(R.string.fui_dialog_warn),
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
                        text = stringResource(R.string.console_dismantle_dialog_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = palette.ink,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.console_dismantle_dialog_message),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = palette.inkMuted,
                    )
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = palette.line)
                    Spacer(Modifier.height(16.dp))
                    Row {
                        FuiSmallButton(
                            label = stringResource(R.string.console_action_cancel),
                            tag = stringResource(R.string.fui_action_cancel_tag),
                            filled = false,
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(10.dp))
                        FuiSmallButton(
                            label = stringResource(R.string.console_action_dismantle),
                            tag = stringResource(R.string.fui_action_dismantle_tag),
                            filled = true,
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

