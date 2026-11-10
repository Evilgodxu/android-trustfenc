package com.trustfenc.evilgodxu.screens.home.component.console

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.screens.home.component.console.fui.LocalFuiPalette
import com.trustfenc.evilgodxu.ui.icons.AppIcons

// 控制台顶栏：品牌等宽字 + 切角设置按钮，下方一条细线收口
@Composable
fun ConsoleTopBar(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalFuiPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .border(1.dp, palette.ink),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.fui_brand),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 4.sp,
                color = palette.ink,
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .border(1.dp, palette.line, CutCornerShape(2.dp))
                    .clip(CutCornerShape(2.dp))
                    .clickable(role = Role.Button, onClick = onOpenSettings),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Settings,
                    contentDescription = stringResource(R.string.settings_title),
                    modifier = Modifier.size(18.dp),
                    tint = palette.ink,
                )
            }
        }
        HorizontalDivider(color = palette.line)
    }
}
