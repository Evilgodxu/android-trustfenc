package com.trustfenc.evilgodxu.screens.settings.component.info

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.trustfenc.evilgodxu.R
import com.trustfenc.evilgodxu.log.CrashLogManager
import com.trustfenc.evilgodxu.ui.component.fui.FuiPanel
import com.trustfenc.evilgodxu.ui.component.fui.FuiSectionHeader
import com.trustfenc.evilgodxu.ui.component.fui.FuiTag
import com.trustfenc.evilgodxu.ui.component.fui.LocalFuiPalette
import com.trustfenc.evilgodxu.ui.icons.AppIcons

// 关于：版本检查、今日日志分享与项目地址，统一为终端式信息行
@Composable
fun AppInfo(version: String, onCheckForUpdate: () -> Unit) {
    val context = LocalContext.current
    val palette = LocalFuiPalette.current

    Column(modifier = Modifier.fillMaxWidth()) {
        FuiSectionHeader(
            index = "03",
            title = stringResource(R.string.settings_section_about),
            tag = stringResource(R.string.fui_tag_about),
        )
        FuiPanel(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                InfoRow(
                    label = stringResource(R.string.fui_label_version),
                    value = version,
                    onClick = onCheckForUpdate,
                    trailing = { FuiTag(text = stringResource(R.string.fui_tag_check)) },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = palette.line,
                )
                InfoRow(
                    label = stringResource(R.string.fui_label_log),
                    value = stringResource(R.string.settings_log),
                    onClick = { shareTodayLog(context) },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = palette.line,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_about_brand),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp,
                        color = palette.inkMuted,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = GITHUB_URL,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = palette.ink,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val urlIntent = Intent(Intent.ACTION_VIEW, GITHUB_URL.toUri())
                                context.startActivity(urlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val palette = LocalFuiPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            color = palette.inkMuted,
            modifier = Modifier.width(76.dp),
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = palette.ink,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            trailing()
        }
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = AppIcons.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = palette.inkMuted,
        )
    }
}

// 唤起系统分享今日异常日志；今日无日志时提示用户
private fun shareTodayLog(context: Context) {
    val logFile = CrashLogManager.getTodayLogFile()
    if (logFile == null) {
        Toast.makeText(context, context.getString(R.string.settings_log_empty_today), Toast.LENGTH_SHORT).show()
        return
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", logFile)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    // 本地化上下文非 Activity，启动外部 Activity 需 NEW_TASK 标志
    val chooser = Intent.createChooser(shareIntent, context.getString(R.string.settings_share_log_title))
    context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private const val GITHUB_URL = "https://github.com/Evilgodxu/android-trustfenc"
