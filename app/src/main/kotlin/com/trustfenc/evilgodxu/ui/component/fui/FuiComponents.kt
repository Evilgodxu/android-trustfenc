package com.trustfenc.evilgodxu.ui.component.fui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// FUI 背景层：远景同心弧 + 中景分层线框 + 底层坐标点阵，用线条层级而非模糊渐变制造纵深
@Composable
fun FuiBackdrop(modifier: Modifier = Modifier) {
    val palette = LocalFuiPalette.current
    Box(modifier = modifier.background(palette.background)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ink = palette.ink
            val baseAlpha = if (palette.isLight) 0.10f else 0.07f
            val stroke = 1.dp.toPx()

            // 远景：自右上角向外扩散的同心弧
            val arcCenter = Offset(size.width * 1.04f, size.height * 0.02f)
            listOf(0.30f, 0.46f, 0.62f, 0.78f).forEachIndexed { index, ratio ->
                drawCircle(
                    color = ink.copy(alpha = baseAlpha * (1f - index * 0.18f)),
                    radius = size.width * ratio,
                    center = arcCenter,
                    style = Stroke(width = stroke),
                )
            }

            // 中景：右下双层线框，半出画形成前后景深
            val frameTopLeft = Offset(size.width * 0.58f, size.height * 0.72f)
            val frameSize = Size(size.width * 0.55f, size.height * 0.36f)
            drawRect(
                color = ink.copy(alpha = baseAlpha * 0.8f),
                topLeft = frameTopLeft,
                size = frameSize,
                style = Stroke(width = stroke),
            )
            val inset = 16.dp.toPx()
            drawRect(
                color = ink.copy(alpha = baseAlpha * 0.45f),
                topLeft = Offset(frameTopLeft.x + inset, frameTopLeft.y + inset),
                size = Size(frameSize.width - inset * 2, frameSize.height - inset * 2),
                style = Stroke(width = stroke),
            )

            // 近景：左下十字准线
            val cross = Offset(size.width * 0.08f, size.height * 0.86f)
            val arm = 20.dp.toPx()
            val crossColor = ink.copy(alpha = baseAlpha * 0.9f)
            drawLine(crossColor, Offset(cross.x - arm, cross.y), Offset(cross.x + arm, cross.y), stroke)
            drawLine(crossColor, Offset(cross.x, cross.y - arm), Offset(cross.x, cross.y + arm), stroke)

            // 底层：坐标点阵
            val step = 26.dp.toPx()
            val radius = 0.7.dp.toPx()
            val dotColor = ink.copy(alpha = if (palette.isLight) 0.10f else 0.08f)
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

// 切角细线面板：浅色以投影浮起，深色保持纯黑平面，四角带短刻线
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
            .shadow(
                elevation = if (palette.isLight) 12.dp else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = palette.ink.copy(alpha = 0.06f),
                spotColor = palette.ink.copy(alpha = 0.12f),
            )
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

// 方形图标按钮：顶栏的返回与设置共用，保持切角描边样式
@Composable
fun FuiIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalFuiPalette.current
    val shape = CutCornerShape(2.dp)
    Box(
        modifier = modifier
            .size(34.dp)
            .border(1.dp, palette.line, shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = palette.ink,
        )
    }
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

// 弹窗底部的小号动作按钮，与主操作按钮同一套反白/描边语言
@Composable
fun FuiSmallButton(
    label: String,
    tag: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalFuiPalette.current
    val shape = CutCornerShape(6.dp)
    val contentColor = if (filled) palette.onInverse else palette.ink
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .background(color = if (filled) palette.inverse else Color.Transparent, shape = shape)
            .border(width = 1.dp, color = if (filled) palette.inverse else palette.ink, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = contentColor,
            maxLines = 1,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = tag,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            letterSpacing = 1.sp,
            color = contentColor.copy(alpha = 0.6f),
            maxLines = 1,
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
