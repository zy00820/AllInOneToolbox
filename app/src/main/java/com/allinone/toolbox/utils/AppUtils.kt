package com.allinone.toolbox.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.allinone.toolbox.App
import com.allinone.toolbox.data.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppUtils {

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val context = App.instance
        val pm = context.packageManager
        val apps = mutableListOf<AppInfo>()

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA
        } else {
            PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA
        }

        val packageInfos: List<PackageInfo> = try {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(flags)
        } catch (_: Exception) {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(0)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        for (packageInfo in packageInfos) {
            try {
                val appInfo = packageInfo.applicationInfo ?: continue
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val targetSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    appInfo.targetSdkVersion
                } else {
                    0
                }

                val apkPath = try {
                    appInfo.sourceDir
                } catch (_: Exception) {
                    ""
                }

                val app = AppInfo(
                    packageName = packageInfo.packageName,
                    appName = try { appInfo.loadLabel(pm).toString() } catch (_: Exception) { packageInfo.packageName },
                    versionName = packageInfo.versionName ?: "N/A",
                    versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.versionCode.toLong()
                    },
                    targetSdk = targetSdk,
                    firstInstallTime = dateFormat.format(Date(packageInfo.firstInstallTime)),
                    lastUpdateTime = dateFormat.format(Date(packageInfo.lastUpdateTime)),
                    apkPath = apkPath,
                    permissions = packageInfo.requestedPermissions?.toList() ?: emptyList(),
                    isSystemApp = isSystemApp,
                    appSize = getAppSize(apkPath)
                )
                apps.add(app)
            } catch (_: Exception) {
                continue
            }
        }
        apps.sortedBy { it.appName.lowercase() }
    }

    private fun getAppSize(path: String): String {
        return try {
            val file = File(path)
            val sizeBytes = file.length()
            when {
                sizeBytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", sizeBytes / (1024.0 * 1024.0 * 1024.0))
                sizeBytes >= 1024 * 1024 -> String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0))
                sizeBytes >= 1024 -> String.format("%.2f KB", sizeBytes / 1024.0)
                else -> "$sizeBytes B"
            }
        } catch (_: Exception) {
            "N/A"
        }
    }

    fun extractApk(context: Context, packageName: String, sourceDir: String): Boolean {
        return try {
            val sourceFile = File(sourceDir)
            if (!sourceFile.exists()) return false

            val destDir = File(context.getExternalFilesDir(null), "extracted_apks")
            if (!destDir.exists()) destDir.mkdirs()

            val destFile = File(destDir, "$packageName.apk")
            sourceFile.copyTo(destFile, overwrite = true)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun shareApk(context: Context, packageName: String, apkPath: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File(apkPath)
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/vnd.android.package-archive"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(intent, "分享APK"))
        } catch (_: Exception) {
        }
    }

    fun openAppInfo(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = android.net.Uri.fromParts("package", packageName, null)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
