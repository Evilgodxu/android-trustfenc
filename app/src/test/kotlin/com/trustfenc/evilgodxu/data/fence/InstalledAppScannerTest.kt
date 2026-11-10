package com.trustfenc.evilgodxu.data.fence

import org.junit.Assert.assertEquals
import org.junit.Test

// shell 输出的解析：只保留 package: 行，忽略错误与空行
class InstalledAppScannerTest {

    @Test
    fun `解析 shell 包列表并忽略错误行`() {
        val output = """
            package:com.example.alpha
            package:com.example.beta
            Error (exit code 1): permission denied
            package:com.example.gamma

        """.trimIndent()

        assertEquals(
            setOf("com.example.alpha", "com.example.beta", "com.example.gamma"),
            parseThirdPartyPackages(output),
        )
    }

    @Test
    fun `无有效包时返回空集合`() {
        assertEquals(emptySet<String>(), parseThirdPartyPackages("Error: not connected"))
    }
}
