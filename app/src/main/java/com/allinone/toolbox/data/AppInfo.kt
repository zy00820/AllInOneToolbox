package com.allinone.toolbox.data

data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val targetSdk: Int,
    val firstInstallTime: String,
    val lastUpdateTime: String,
    val apkPath: String,
    val permissions: List<String>,
    val isSystemApp: Boolean,
    val appSize: String
)
