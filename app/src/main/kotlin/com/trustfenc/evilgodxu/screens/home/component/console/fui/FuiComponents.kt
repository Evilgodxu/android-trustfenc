package com.trustfenc.evilgodxu.screens.home.component.console.fui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// FUI 背景层：深色为纯黑，浅色用模糊渐变托底，再叠一层低对比坐标点阵
@Composable
fun FuiBackdrop(modifier: Modifier = Modifier) {
    val palette = LocalFuiPalette.current
    Box(modifier = modifier.background(palette.background)) {
        if (palette.isLight) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(90.dp),
            ) {
                GlassOrb(
                    alignment = Alignment.TopEnd,
                    offsetX = 80.dp,
                    offsetY = (-110).dp,
                    size = 320.dp,
                    color = Color(0xFFB6C2D6).copy(alpha = 0.75f),
                )
                GlassOrb(
                    alignment = Alignment.BottomStart,
                    offsetX = (-100).dp,
                    offsetY = 70.dp,
                    size = 300.dp,
                    color = Color(0xFFD3CADF).copy(alpha = 0.70f),
                )
                GlassOrb(
                    alignment = Alignment.CenterEnd,
                    offsetX = 60.dp,
                    offsetY = 0.dp,
                    size = 220.dp,
                    color = Color(0xFFC6D8D1).copy(alpha = 0.55f),
                )
            }
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 26.dp.toPx()
            val radius = 0.7.dp.toPx()
            val dotColor = palette.ink.copy(alpha = if (palette.isLight) 0.10f else 0.08f)
            var y = step
            while (y < size.height) {
                var x = step
                while (x < size.width) {
                    drawCircle(color = dotColor, radius = radius, center = Offset(x, y))
                    x += step
                }
                y += step
            }
        }
    }
}

@Composable
private fun BoxScope.GlassOrb(
    alignment: Alignment,
    offsetX: Dp,
    offsetY: Dp,
    size: Dp,
    color: Color,
) {
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(x = offsetX, y = offsetY)
            .size(size)
            .background(
                brush = Brush.radialGradient(listOf(color, Color.Transparent)),
                shape = CircleShape,
            ),
    )
}

// 切角细线面板：四角带短刻线，模拟 HUD 框体
@Composable
fun FuiPanel(
    modifier: Modifier = Modifier,
    solid: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalFuiPalette.current
    val shape = CutCornerShape(6.dp)
    Box(
        modifier = modifier
            .background(color = if (solid) palette.solidPanel else palette.panel, shape = shape)
            .border(width = 1.dp, color = palette.line, shape = shape)
            .cornerTicks(palette.ink.copy(alpha = 0.5f)),
        content = content,
    )
}

private fun Modifier.cornerTicks(color: Color): Modifier = drawBehind {
    val length = 10.dp.toPx()
    val stroke = 1.dp.toPx()
    val w = size.width
    val h = size.height
    drawLine(color, Offset(0f, 0f), Offset(length, 0f), stroke)
    drawLine(color, Offset(0f, 0f), Offset(0f, length), stroke)
    drawLine(color, Offset(w, 0f), Offset(w - length, 0f), stroke)
    drawLine(color, Offset(w, 0f), Offset(w, length), stroke)
    drawLine(color, Offset(0f, h), Offset(length, h), stroke)
    drawLine(color, Offset(0f, h), Offset(0f, h - length), stroke)
    drawLine(color, Offset(w, h), Offset(w - length, h), stroke)
    drawLine(color, Offset(w, h), Offset(w, h - length), stroke)
}

// 区块头：序号 + 中文标题 + 细分隔线 + 英文微标签
@Composable
fun FuiSectionHeader(
    index: String,
    title: String,
    tag: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalFuiPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(palette.ink),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = index,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = palette.inkMuted,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            color = palette.ink,
        )
        Spacer(Modifier.width(10.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = palette.line,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = tag,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp,
            color = palette.inkMuted,
        )
    }
}

// 等宽小标签：反白态用于强状态，普通态只描边
@Composable
fun FuiTag(
    text: String,
    modifier: Modifier = Modifier,
    inverted: Boolean = false,
) {
    val palette = LocalFuiPalette.current
    val shape = CutCornerShape(2.dp)
    Text(
        text = text,
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp,
        letterSpacing = 1.2.sp,
        color = if (inverted) palette.onInverse else palette.ink,
        modifier = modifier
            .background(color = if (inverted) palette.inverse else Color.Transparent, shape = shape)
            .border(width = 1.dp, color = if (inverted) palette.inverse else palette.line, shape = shape)
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

// 主操作按钮：修篱时为反白实心，拆除时为细线描边，过渡态禁用并显示进度
@Composable
fun FuiActionButton(
    label: String,
    tag: String,
    filled: Boolean,
    enabled: Boolean,
    busy: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalFuiPalette.current
    val shape = CutCornerShape(8.dp)
    val contentColor = if (filled) palette.onInverse else palette.ink
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(color = if (filled) palette.inverse else Color.Transparent, shape = shape)
            .border(
                width = 1.dp,
                color = if (enabled) (if (filled) palette.inverse else palette.ink) else palette.line,
                shape = shape,
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 1.5.dp,
                color = contentColor,
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            color = if (enabled) contentColor else palette.inkMuted,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = tag,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            letterSpacing = 2.sp,
            color = (if (enabled) contentColor else palette.inkMuted).copy(alpha = 0.6f),
        )
    }
}

// 篱笆准星：双环 + 四向刻线，守护中内环呼吸
@Composable
fun FuiReticle(
    active: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val palette = LocalFuiPalette.current
    val haloAlpha = if (active) {
        val transition = rememberInfiniteTransition(label = "fuiReticle")
        transition.animateFloat(
            initialValue = 0.06f,
            targetValue = 0.18f,
            animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
            label = "fuiReticleHalo",
        ).value
    } else {
        0.06f
    }

    Box(
        modifier = modifier.size(136.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val tick = 9.dp.toPx()
            val stroke = 1.dp.toPx()
            drawCircle(
                color = palette.ink.copy(alpha = 0.35f),
                radius = radius - stroke / 2,
                style = Stroke(width = stroke),
            )
            drawCircle(
                color = palette.ink.copy(alpha = 0.16f),
                radius = radius * 0.78f,
                style = Stroke(width = stroke),
            )
            drawCircle(
                color = palette.ink.copy(alpha = haloAlpha),
                radius = radius * 0.6f,
            )
            listOf(
                Offset(radius, 0f) to Offset(radius, tick),
                Offset(radius * 2f, radius) to Offset(radius * 2f - tick, radius),
                Offset(radius, radius * 2f) to Offset(radius, radius * 2f - tick),
                Offset(0f, radius) to Offset(tick, radius),
            ).forEach { (start, end) ->
                drawLine(
                    color = palette.ink.copy(alpha = 0.55f),
                    start = start,
                    end = end,
                    strokeWidth = stroke,
                )
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(46.dp),
            tint = palette.ink,
        )
    }
}
