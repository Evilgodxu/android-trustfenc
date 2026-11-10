package com.trustfenc.evilgodxu.data.fence

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.trustfenc.evilgodxu.data.shizuku.ShizukuShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 基线扫描：先用 PackageManager 读取可见的非系统应用，
// 再用 Shizuku 的 shell 枚举补齐包可见性之外的应用，二者并集即完整基线
class InstalledAppScanner(
    context: Context,
    private val shell: ShizukuShell,
) {
    private val appContext = context.applicationContext

    suspend fun scanBaseline(): Set<String> {
        val visible = withContext(Dispatchers.IO) { scanVisiblePackages() }
        // 基线必须完整：shell 枚举失败或为空时直接失败，避免遗漏应用导致误卸载
        val shellOutput = shell.execute("pm list packages -3").getOrElse {
            throw IllegalStateException("shell 枚举应用失败", it)
        }
        check(!shellOutput.startsWith("Error") && !shellOutput.startsWith("Exception")) {
            "shell 枚举应用失败：${shellOutput.trim()}"
        }
        val shellPackages = parseThirdPartyPackages(shellOutput)
        check(shellPackages.isNotEmpty()) { "未扫描到任何应用" }
        return visible + shellPackages
    }

    // API 30+ 只返回可见应用；这里刻意接受部分结果，包可见性之外的应用由随后的 shell 枚举补齐
    @SuppressLint("QueryPermissionsNeeded")
    private fun scanVisiblePackages(): Set<String> {
        val pm = appContext.packageManager
        val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }
        return apps
            .asSequence()
            .filter { info ->
                info.flags and ApplicationInfo.FLAG_SYSTEM == 0 &&
                    info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0
            }
            .map { it.packageName }
            .toSet()
    }
}

// 解析 `pm list packages -3` 的输出，忽略错误行
internal fun parseThirdPartyPackages(output: String): Set<String> =
    output.lineSequence()
        .map { it.trim() }
        .filter { it.startsWith(PACKAGE_PREFIX) }
        .map { it.removePrefix(PACKAGE_PREFIX) }
        .filter { it.isNotBlank() }
        .toSet()

private const val PACKAGE_PREFIX = "package:"
