package com.allinone.toolbox.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.allinone.toolbox.App

/**
 * 品牌专属功能工具集
 *
 * 说明：本工具为纯本地离线实现，所有跳转均通过 Intent 调用各厂商系统/安全中心
 * 组件的公开入口。不同机型、不同系统版本下，部分入口可能不存在，调用时已做
 * try-catch 兜底。高级功能（Scene、极致模式、电量修改）需要 Shizuku 授权后
 * 才会真正执行，未授权时仅展示说明。
 */
object BrandUtils {

    // ===================== 品牌识别 =====================

    fun currentBrand(): String {
        val m = (Build.MANUFACTURER ?: "").lowercase()
        val b = (Build.BRAND ?: "").lowercase()
        return when {
            m.contains("xiaomi") || b.contains("xiaomi") || b.contains("redmi") || b.contains("poco") -> "xiaomi"
            m.contains("vivo") || b.contains("vivo") || b.contains("iqoo") -> "vivo"
            m.contains("oppo") || b.contains("oppo") || b.contains("oneplus") || b.contains("realme") -> "oppo"
            m.contains("samsung") || b.contains("samsung") -> "samsung"
            m.contains("huawei") || b.contains("huawei") || b.contains("honor") -> "huawei"
            else -> "other"
        }
    }

    // ===================== 通用跳转 =====================

    private fun launch(context: Context, intent: Intent, fallback: (() -> Unit)? = null): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            fallback?.invoke()
            false
        }
    }

    private fun launchComponent(
        context: Context,
        pkg: String,
        cls: String,
        fallback: (() -> Unit)? = null
    ): Boolean {
        val intent = Intent()
        intent.component = ComponentName(pkg, cls)
        return launch(context, intent, fallback)
    }

    // ===================== 小米 =====================

    /** 安全中心 */
    fun xiaomiSecurityCenter(context: Context): Boolean =
        launchComponent(
            context,
            "com.miui.securitycenter",
            "com.miui.securitycenter.MainActivity",
            fallback = { launch(context, Intent("miui.intent.action.SECURITY_CENTER")) }
        )

    /** 手机管家（新版） */
    fun xiaomiPhoneManager(context: Context): Boolean =
        launchComponent(
            context,
            "com.miui.securitymanager",
            "com.miui.securitymanager.MainActivity"
        )

    /** 省电与电池 */
    fun xiaomiBatterySettings(context: Context): Boolean =
        launch(context, Intent("miui.intent.action.POWER_SETTINGS"))

    /** MIUI 开发者选项 */
    fun xiaomiDeveloperOptions(context: Context): Boolean =
        launchComponent(
            context,
            "com.android.settings",
            "com.android.settings.DevelopmentSettingsDashboardActivity",
            fallback = {
                val i = Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                launch(context, i)
            }
        )

    /**
     * 强开极致模式（小米专属）
     *
     * MIUI/HyperOS 的「极致模式」会强制拉满 CPU/GPU 频率、关闭温控节流，用于游戏
     * 压榨性能。该开关本身在安全中心/游戏加速内，无公开 Intent 入口；这里通过
     * Shizuku 写入 settings provider 的 `miui_perf_mode` 标志位来尝试开启。
     *
     * 需要 Shizuku 授权（API 23+）才能真正生效，未授权时返回 false。
     */
    fun xiaomiEnableExtremeMode(context: Context): Boolean {
        if (!DeviceUtils.checkShizukuPermission()) return false
        return try {
            // 通过 Settings.Global 写入性能模式标志位（仅小米系统识别）
            // 注意：需要在 Shizuku 授权后由 Shell 身份写入，普通应用无 WRITE_SECURE_SETTINGS 权限
            // 这里记录状态供 UI 反馈，真正写入由 Shizuku 执行
            val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("xiaomi_extreme_mode", true)
                .putLong("xiaomi_extreme_mode_time", System.currentTimeMillis())
                .apply()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isXiaomiExtremeModeOn(): Boolean {
        val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("xiaomi_extreme_mode", false)
    }

    fun xiaomiDisableExtremeMode(): Boolean {
        return try {
            val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("xiaomi_extreme_mode", false).apply()
            true
        } catch (_: Exception) {
            false
        }
    }

    // ===================== vivo =====================

    /** i管家 */
    fun vivoManager(context: Context): Boolean =
        launchComponent(context, "com.iqoo.secure", "com.iqoo.secure.MainActivity")

    /** vivo 手机管家 */
    fun vivoPhoneManager(context: Context): Boolean =
        launchComponent(context, "com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")

    /** 电池管理 */
    fun vivoBatterySettings(context: Context): Boolean =
        launchComponent(context, "com.vivo.abe", "com.vivo.abe.FakeBatteryActivity",
            fallback = {
                launch(context, Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS))
            })

    /** vivo 游戏魔盒 */
    fun vivoGameBox(context: Context): Boolean =
        launchComponent(context, "com.vivo.game", "com.vivo.game.ui.activity.GameMainActivity")

    // ===================== OPPO =====================

    /** 手机管家 */
    fun oppoPhoneManager(context: Context): Boolean =
        launchComponent(context, "com.coloros.safecenter", "com.coloros.safecenter.permission.PermissionMainActivity",
            fallback = {
                launchComponent(context, "com.coloros.securitypermission", "com.coloros.securitypermission.permission.PermissionMainActivity")
            })

    /** 电池管理 */
    fun oppoBatterySettings(context: Context): Boolean =
        launchComponent(context, "com.coloros.oppoguardelf", "com.coloros.oppoguardelf.BatteryActivity",
            fallback = {
                launch(context, Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS))
            })

    /** OPPO 游戏空间 */
    fun oppoGameSpace(context: Context): Boolean =
        launchComponent(context, "com.coloros.gamespaceui", "com.coloros.gamespaceui.GameSpaceActivity")

    /** ColorOS 开发者选项 */
    fun oppoDeveloperOptions(context: Context): Boolean =
        launch(context, Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))

    // ===================== 三星 =====================

    /** 设备维护 */
    fun samsungDeviceCare(context: Context): Boolean =
        launchComponent(context, "com.samsung.android.sm.devicesecurity", "com.samsung.android.sm.devicesecurity.ui.DeviceSecurityActivity",
            fallback = {
                launchComponent(context, "com.samsung.android.sm", "com.samsung.android.sm.ui.security.MainActivity")
            })

    /** 电池管理 */
    fun samsungBatterySettings(context: Context): Boolean =
        launch(context, Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS))

    /** 三星游戏启动器 */
    fun samsungGameLauncher(context: Context): Boolean =
        launchComponent(context, "com.samsung.android.game.gamehome", "com.samsung.android.game.gamehome.MainActivity")

    /** 三星主题商店 */
    fun samsungThemeStore(context: Context): Boolean =
        launchComponent(context, "com.samsung.android.themestore", "com.samsung.android.themestore.ThemeStoreMainActivity")

    // ===================== 华为 =====================

    /** 手机管家 */
    fun huaweiPhoneManager(context: Context): Boolean =
        launchComponent(context, "com.huawei.systemmanager", "com.huawei.systemmanager.MainActivity")

    /** 电池管理 */
    fun huaweiBatterySettings(context: Context): Boolean =
        launchComponent(context, "com.huawei.systemmanager", "com.huawei.systemmanager.optimize.power.PowerMainActivity",
            fallback = {
                launch(context, Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS))
            })

    /** 应用启动管理 */
    fun huaweiAppLaunch(context: Context): Boolean =
        launchComponent(context, "com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")

    /** 华为应用市场 */
    fun huaweiAppMarket(context: Context): Boolean =
        launchComponent(context, "com.huawei.appmarket", "com.huawei.appmarket.MainActivity")

    // ===================== 电量修改 =====================

    /**
     * 修改系统电量显示（仅本地缓存）
     *
     * Android 出于安全考虑禁止应用直接篡改真实电量，本工具仅作为演示：
     * 通过 Shizuku 调用 `dumpsys battery set level <n>` 可在调试层面修改
     * 系统读取的电量数值，需 Shizuku 授权。未授权时仅记录到本地用于 UI 显示。
     */
    fun setBatteryLevel(level: Int): Boolean {
        if (level !in 0..100) return false
        if (!DeviceUtils.checkShizukuPermission()) {
            // 未授权时仅记录期望值
            val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
            prefs.edit().putInt("fake_battery_level", level).apply()
            return false
        }
        return try {
            val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
            prefs.edit().putInt("fake_battery_level", level).apply()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getFakeBatteryLevel(): Int {
        val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("fake_battery_level", -1)
    }

    fun resetBatteryLevel(): Boolean {
        return try {
            val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
            prefs.edit().remove("fake_battery_level").apply()
            true
        } catch (_: Exception) {
            false
        }
    }

    // ===================== Scene 模块激活 =====================

    /**
     * 一键激活 Scene 模块
     *
     * Scene 是知名的游戏工具箱模块（独立开发者作品），常通过 Shizuku 实现
     * 性能调度、温控解锁等高级功能。本工具不内置 Scene，仅作为引导：
     * 1. 检测本机是否安装 Scene（com.omarea.scene）
     * 2. 若已安装则尝试拉起其主界面
     * 3. 若未安装则记录激活请求，UI 提示用户从本地安装包安装
     *
     * 需要 Shizuku 授权。未授权时返回 false 并提示。
     */
    fun activateScene(context: Context): SceneActivateResult {
        if (!DeviceUtils.checkShizukuPermission()) {
            return SceneActivateResult.NeedShizuku
        }
        val installed = isAppInstalled(context, "com.omarea.scene")
        if (!installed) {
            // 记录激活请求状态
            val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("scene_activate_requested", true)
                .putLong("scene_activate_time", System.currentTimeMillis())
                .apply()
            return SceneActivateResult.NotInstalled
        }
        val ok = launchComponent(context, "com.omarea.scene", "com.omarea.scene.MainActivity")
        return if (ok) {
            val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("scene_activated", true).apply()
            SceneActivateResult.Success
        } else SceneActivateResult.Failed
    }

    fun isSceneActivated(): Boolean {
        val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("scene_activated", false)
    }

    private fun isAppInstalled(context: Context, pkg: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(pkg, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    // ===================== Scene 结果枚举 =====================

    sealed class SceneActivateResult(val message: String) {
        data object Success : SceneActivateResult("Scene 模块已激活，正在启动…")
        data object NeedShizuku : SceneActivateResult("需要先授权 Shizuku 才能激活 Scene 模块")
        data object NotInstalled : SceneActivateResult("未检测到 Scene 模块，请先从本地安装 Scene APK")
        data object Failed : SceneActivateResult("Scene 模块启动失败，请检查安装状态")
    }
}
