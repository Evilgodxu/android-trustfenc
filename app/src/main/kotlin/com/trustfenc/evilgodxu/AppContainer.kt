package com.trustfenc.evilgodxu

import android.content.Context
import android.content.pm.PackageManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.trustfenc.evilgodxu.data.repository.DataStoreSettingsRepository
import com.trustfenc.evilgodxu.data.repository.DataStoreUpdateCheckRepository
import com.trustfenc.evilgodxu.data.repository.SettingsRepository
import com.trustfenc.evilgodxu.data.repository.UpdateCheckRepository
import com.trustfenc.evilgodxu.localization.LocalizationManager
import com.trustfenc.evilgodxu.update.ApkInstaller
import com.trustfenc.evilgodxu.update.UpdateDownloadCoordinator

// 手动 DI 容器：Application 启动时构造并持有全部依赖
// 数据源经构造注入仓库，便于单元测试替换
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val settingsRepository: SettingsRepository by lazy {
        DataStoreSettingsRepository(preferencesDataStore)
    }

    val updateCheckRepository: UpdateCheckRepository by lazy {
        DataStoreUpdateCheckRepository(preferencesDataStore)
    }

    val localizationManager: LocalizationManager by lazy {
        LocalizationManager(appContext)
    }

    // 更新下载：协调器持有下载状态并启动前台服务，安装器负责拉起系统安装流程
    val updateDownloadCoordinator: UpdateDownloadCoordinator by lazy {
        UpdateDownloadCoordinator(appContext)
    }

    val apkInstaller: ApkInstaller by lazy {
        ApkInstaller(appContext)
    }

    // 应用版本号：冷启动读取一次
    val appVersion: String = readAppVersion()

    private val preferencesDataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create {
            appContext.preferencesDataStoreFile("settings")
        }
    }

    private fun readAppVersion(): String =
        appContext.packageManager
            .getPackageInfo(appContext.packageName, PackageManager.PackageInfoFlags.of(0L))
            .versionName.orEmpty()
}