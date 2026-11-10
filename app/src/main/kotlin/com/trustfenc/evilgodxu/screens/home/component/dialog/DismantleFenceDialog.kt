package com.trustfenc.evilgodxu.screens.home.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.ui.icons.AppIcons

// 拆除确认：清空基线不可撤销，破坏性操作前必须显式确认
@Composable
fun DismantleFenceDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = AppIcons.Fence,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = {
            Text(text = stringResource(R.string.console_dismantle_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.console_dismantle_dialog_message))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.console_action_dismantle),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.console_action_cancel))
            }
        },
    )
}
