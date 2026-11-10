package com.trustfenc.evilgodxu.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test

// 持久化值到枚举的解析是反序列化唯一入口，未知值必须回落到跟随系统而不是抛错
class ThemeModeTest {

    @Test
    fun `已知持久化值可还原`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromValue("system"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromValue("dark"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromValue("light"))
    }

    @Test
    fun `未知或空持久化值回落为跟随系统`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromValue("sepia"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromValue(""))
    }

    @Test
    fun `枚举值与持久化值一一对应`() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromValue(mode.value))
        }
    }
}
