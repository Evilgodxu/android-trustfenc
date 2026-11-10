package com.trustfenc.evilgodxu.screens.settings.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

/** 设置页专属：FUI 单选面板，选中项反白，底部取消返回 */
@Composable
fun <T> SingleChoiceDialog(
    title: String,
    options: List<T>,
    selectedOption: T,
    optionLabel: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(palette.ink),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.fui_label_select),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            color = palette.inkMuted,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = options.size.toString().padStart(2, '0'),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = palette.inkMuted,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = palette.ink,
                    )
                    Spacer(Modifier.height(14.dp))
                    Column {
                        options.forEach { option ->
                            val selected = option == selectedOption
                            val shape = CutCornerShape(2.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (selected) palette.inverse else Color.Transparent,
                                        shape = shape,
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) palette.inverse else palette.line,
                                        shape = shape,
                                    )
                                    .clickable { onOptionSelected(option) }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = optionLabel(option),
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) palette.onInverse else palette.ink,
                                    modifier = Modifier.weight(1f),
                                )
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(palette.onInverse),
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = palette.line)
                    Spacer(Modifier.height(14.dp))
                    FuiSmallButton(
                        label = stringResource(R.string.console_action_cancel),
                        tag = stringResource(R.string.fui_action_cancel_tag),
                        filled = false,
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
