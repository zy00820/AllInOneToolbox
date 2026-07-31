package com.allinone.toolbox.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import com.allinone.toolbox.App
import com.allinone.toolbox.data.HardwareInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.RandomAccessFile

object DeviceUtils {

    fun getDeviceCode(): String {
        val androidId = try {
            android.provider.Settings.Secure.getString(
                App.instance.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
        } catch (_: Exception) {
            "unknown"
        }
        return androidId?.take(8)?.uppercase() ?: "UNKNOWN"
    }

    suspend fun getHardwareInfo(): HardwareInfo = withContext(Dispatchers.IO) {
        val cpuName = getCpuName()
        val cpuCores = getCpuCores()
        val cpuMaxFreq = getCpuMaxFreq()
        val gpuInfo = getGpuInfo()
        val screenInfo = getScreenInfo()
        val batteryInfo = getBatteryInfo()

        HardwareInfo(
            cpuName = cpuName,
            cpuCores = cpuCores,
            cpuMaxFreq = cpuMaxFreq,
            gpuName = gpuInfo.first,
            gpuVendor = gpuInfo.second,
            screenResolution = screenInfo.first,
            screenDensity = screenInfo.second,
            screenSize = screenInfo.third,
            batteryLevel = batteryInfo.first,
            batteryStatus = batteryInfo.second,
            batteryTechnology = batteryInfo.third,
            systemVersion = Build.VERSION.RELEASE,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            buildNumber = Build.DISPLAY,
            deviceModel = Build.MODEL,
            deviceBrand = Build.BRAND,
            deviceCodeName = Build.DEVICE,
            hardwareReport = generateReport(
                cpuName, cpuCores, cpuMaxFreq,
                gpuInfo.first, gpuInfo.second,
                screenInfo.first, screenInfo.second, screenInfo.third,
                batteryInfo.first, batteryInfo.second, batteryInfo.third,
                Build.VERSION.RELEASE, Build.DISPLAY,
                Build.MODEL, Build.BRAND, Build.DEVICE
            )
        )
    }

    private fun getCpuName(): String {
        return try {
            val file = RandomAccessFile("/proc/cpuinfo", "r")
            var line: String?
            while (file.readLine().also { line = it } != null) {
                if (line!!.startsWith("model name")) {
                    file.close()
                    return line!!.substringAfter(": ").trim()
                }
            }
            file.close()
            Build.HARDWARE
        } catch (_: Exception) {
            Build.HARDWARE
        }
    }

    private fun getCpuCores(): Int {
        return try {
            Runtime.getRuntime().availableProcessors()
        } catch (_: Exception) {
            0
        }
    }

    private fun getCpuMaxFreq(): String {
        return try {
            val file = RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r")
            val freq = file.readLine().trim().toLongOrNull()
            file.close()
            if (freq != null) {
                "${freq / 1000} MHz"
            } else "N/A"
        } catch (_: Exception) {
            "N/A"
        }
    }

    private fun getGpuInfo(): Pair<String, String> {
        return try {
            val file = RandomAccessFile("/proc/mali/gpuinfo", "r")
            val info = file.readLine().trim()
            file.close()
            val parts = info.split(" ", limit = 2)
            Pair(parts.lastOrNull() ?: info, parts.firstOrNull() ?: "Unknown")
        } catch (_: Exception) {
            try {
                val file = RandomAccessFile("/sys/class/kgsl/kgsl-3d0/gpu_model", "r")
                val model = file.readLine().trim()
                file.close()
                Pair(model, "Qualcomm Adreno")
            } catch (_: Exception) {
                try {
                    val file = RandomAccessFile("/proc/gpi/gpu", "r")
                    val info = file.readLine().trim()
                    file.close()
                    Pair(info, "ARM Mali")
                } catch (_: Exception) {
                    Pair("Unknown GPU", "Unknown")
                }
            }
        }
    }

    private fun getScreenInfo(): Triple<String, String, String> {
        return try {
            val context = App.instance
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            val display = wm.defaultDisplay
            val metrics = android.util.DisplayMetrics()
            display.getMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val densityDpi = metrics.densityDpi
            val screenSize = kotlin.math.sqrt(
                (width * width + height * height).toDouble()
            ) / densityDpi
            Triple(
                "${width}x${height}",
                "${densityDpi} dpi",
                String.format("%.1f inches", screenSize)
            )
        } catch (_: Exception) {
            Triple("N/A", "N/A", "N/A")
        }
    }

    private fun getBatteryInfo(): Triple<Int, String, String> {
        return try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryIntent = App.instance.registerReceiver(null, filter)
            if (batteryIntent != null) {
                val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
                val technology = batteryIntent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

                val statusText = when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "充电中"
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> "放电中"
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "未充电"
                    BatteryManager.BATTERY_STATUS_FULL -> "已满"
                    else -> "未知"
                }
                Triple(
                    (level * 100 / scale),
                    statusText,
                    technology
                )
            } else Triple(0, "未知", "Unknown")
        } catch (_: Exception) {
            Triple(0, "未知", "Unknown")
        }
    }

    private fun generateReport(
        cpuName: String, cpuCores: Int, cpuMaxFreq: String,
        gpuName: String, gpuVendor: String,
        screenResolution: String, screenDensity: String, screenSize: String,
        batteryLevel: Int, batteryStatus: String, batteryTechnology: String,
        androidVersion: String, buildNumber: String,
        deviceModel: String, deviceBrand: String, deviceCodeName: String
    ): String {
        return buildString {
            appendLine("===== 全能工具箱 - 硬件检测报告 =====")
            appendLine("生成时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
            appendLine()
            appendLine("【设备信息】")
            appendLine("品牌: $deviceBrand")
            appendLine("型号: $deviceModel")
            appendLine("设备代号: $deviceCodeName")
            appendLine("设备代码: ${getDeviceCode()}")
            appendLine()
            appendLine("【CPU信息】")
            appendLine("型号: $cpuName")
            appendLine("核心数: $cpuCores")
            appendLine("最高频率: $cpuMaxFreq")
            appendLine()
            appendLine("【GPU信息】")
            appendLine("型号: $gpuName")
            appendLine("厂商: $gpuVendor")
            appendLine()
            appendLine("【屏幕参数】")
            appendLine("分辨率: $screenResolution")
            appendLine("密度: $screenDensity")
            appendLine("尺寸: $screenSize")
            appendLine()
            appendLine("【电池信息】")
            appendLine("电量: $batteryLevel%")
            appendLine("状态: $batteryStatus")
            appendLine("技术: $batteryTechnology")
            appendLine()
            appendLine("【系统版本】")
            appendLine("系统: $androidVersion")
            appendLine("构建号: $buildNumber")
            appendLine()
            appendLine("======================================")
        }
    }

    suspend fun exportHardwareReport(hwInfo: HardwareInfo): Boolean = withContext(Dispatchers.IO) {
        try {
            val context = App.instance
            val fileName = "硬件报告_${System.currentTimeMillis()}.txt"
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (dir != null) {
                val file: java.io.File = java.io.File(dir, fileName)
                java.io.FileWriter(file).use { writer ->
                    writer.write(hwInfo.hardwareReport)
                }
                true
            } else {
                val file: java.io.File = java.io.File(context.filesDir, fileName)
                java.io.FileWriter(file).use { writer ->
                    writer.write(hwInfo.hardwareReport)
                }
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    fun openSystemSettings(context: Context) {
        val intent = Intent(Settings.ACTION_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openDeveloperOptions(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            openSystemSettings(context)
        }
    }

    fun openDisplaySettings(context: Context) {
        val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun checkShizukuPermission(): Boolean {
        return try {
            val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
            prefs.getBoolean("shizuku_authorized", false)
        } catch (_: Exception) {
            false
        }
    }
}
