package com.trustfenc.evilgodxu.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 分组标题：卡片之外的统一节标题样式，供 SectionCard 与自定义面板共用
@Composable
fun SectionLabel(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 4.dp, top = 20.dp, bottom = 10.dp),
    )
}
