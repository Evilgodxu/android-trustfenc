package com.trustfenc.evilgodxu.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// 版本比较决定「是否有更新」这一唯一判据，边界覆盖到缺省位补齐与非数字段
class AppUpdateCheckerTest {

    @Test
    fun `更高版本判定为有更新`() {
        assertTrue(AppUpdateChecker.hasNewVersion("1.2.0", "1.1.9"))
        assertTrue(AppUpdateChecker.hasNewVersion("2.0.0", "1.99.99"))
        assertTrue(AppUpdateChecker.hasNewVersion("1.0.1", "1.0.0"))
    }

    @Test
    fun `相同版本判定为无更新`() {
        assertFalse(AppUpdateChecker.hasNewVersion("1.0.0", "1.0.0"))
        assertFalse(AppUpdateChecker.hasNewVersion("v1.2.3", "1.2.3"))
    }

    @Test
    fun `缺省位按零补齐比较`() {
        assertFalse(AppUpdateChecker.hasNewVersion("1.0", "1.0.0"))
        assertTrue(AppUpdateChecker.hasNewVersion("1.0.1", "1.0"))
        assertTrue(AppUpdateChecker.hasNewVersion("2", "1.9.9"))
    }

    @Test
    fun `非数字段按零处理`() {
        assertFalse(AppUpdateChecker.hasNewVersion("1.0.0-beta", "1.0.0"))
        assertTrue(AppUpdateChecker.hasNewVersion("1.1.0", "1.0.x"))
    }
}
