package com.trustfenc.evilgodxu.screens.settings.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trustfenc.evilgodxu.ui.component.fui.LocalFuiPalette
import com.trustfenc.evilgodxu.ui.icons.AppIcons

// 设置项：点击位置用于主题切换的圆形扩散起点。
// 位置在 Initial 阶段旁路记录，点击仍交给 clickable，涟漪、焦点与无障碍语义保持原生行为
@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (Offset) -> Unit,
) {
    val palette = LocalFuiPalette.current
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var downPosition by remember { mutableStateOf<Offset?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates = it }
            .pointerInput(Unit) {
                awaitEachGesture {
                    downPosition = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial,
                    ).position
                }
            }
            .clickable {
                val origin = coordinates?.let { current ->
                    // 非指针触发（键盘 / 无障碍）没有按下坐标，退回组件几何中心
                    val local = downPosition ?: Offset(current.size.width / 2f, current.size.height / 2f)
                    current.localToRoot(local)
                } ?: Offset.Zero
                downPosition = null
                onClick(origin)
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .border(1.dp, palette.line, CutCornerShape(2.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = palette.ink,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = palette.ink,
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = palette.inkMuted,
            )
        }
        Icon(
            imageVector = AppIcons.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = palette.inkMuted,
        )
    }
}
