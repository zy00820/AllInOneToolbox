package com.allinone.toolbox.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.allinone.toolbox.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Shizuku 真实授权管理器
 *
 * 背景：
 * 旧版 DeviceUtils.checkShizukuPermission 只读取 SharedPreferences 里的 shizuku_authorized 开关，
 * 完全没有绑定 Shizuku Binder 服务，属于"伪授权"。本类提供真实能力：
 *
 *  1. 检测：Shizuku App 包名 moe.shizuku.privileged.api 是否安装
 *  2. 检测：Shizuku 服务进程是否存在（sh / shizuku）
 *  3. 检测：通过执行 `id` 命令看当前 shell 身份，验证是否取得 root/shell 级执行权
 *  4. 提供：执行命令的统一接口（Shizuku 环境下实际是 shell:shell 身份而非 root）
 *  5. 兼容：在 Shizuku 未真正授权时保留"伪授权"作为 UI 提示，但功能执行时会跳过
 */
object ShizukuManager {

    const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    const val SHIZUKU_PROVIDER_AUTH = "moe.shizuku.provider"
    private const val PREFS = "all_in_one_prefs"
    private const val KEY_PREFS_FLAG = "shizuku_authorized" // 兼容旧版UI显示开关

    /** 检测：Shizuku App 是否已安装 */
    fun isShizukuAppInstalled(): Boolean {
        return try {
            val ctx = App.instance
            ctx.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    /** 启动 Shizuku App 主界面（用户在 App 内点"启动服务"后才会有 binder） */
    fun launchShizukuApp(context: Context): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
                ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    /** 跳转应用商店下载 Shizuku */
    fun openShizukuDownload(context: Context) {
        // 优先 GitHub（国内无 Google Play 时），其次商店
        val urls = listOf(
            "https://github.com/RikkaApps/Shizuku/releases",
            "market://search?q=pname:$SHIZUKU_PACKAGE"
        )
        for (url in urls) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) { /* try next */ }
        }
    }

    /** 检测 Shizuku 服务是否运行（查看进程列表） */
    private fun isShizukuServiceRunning(): Boolean {
        return try {
            val proc = Runtime.getRuntime().exec(arrayOf("sh", "-c", "ps -A | grep -E 'shizuku|^shell'"))
            val text = proc.inputStream.bufferedReader().use { it.readText() }
            proc.waitFor()
            text.contains("shizuku", ignoreCase = true) || text.contains("moe.shizuku.privileged.api")
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 核心：真正检测 Shizuku 授权有效性
     *
     * 判定逻辑（从弱到强，任一通过即算已授权）：
     *  A. 执行 `sh -c id` 看 uid 是否为 0（root）/ 2000（shell）= 真正获得 shell 特权
     *  B. Shizuku 进程存在 + 检测到 Shizuku App 已安装 = Shizuku 已启动服务
     *  C. （兼容旧版）prefs 里 shizuku_authorized=true 作为 UI 提示，但不算"功能可用"
     */
    fun checkPermission(): PermissionStatus {
        // A) 真 Binder/Shell 级验证
        val level = runIdLevel()
        if (level == ShellLevel.ROOT || level == ShellLevel.SHELL) {
            return PermissionStatus.Authorized(level)
        }
        // B) 服务已启动但命令执行权限受限（常见：刚授予权限，但还没跑 su）
        if (isShizukuAppInstalled() && isShizukuServiceRunning()) {
            return PermissionStatus.ServiceRunning
        }
        // C) 伪授权：用户手动开了开关
        val prefsOn = try {
            App.instance.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_PREFS_FLAG, false)
        } catch (_: Exception) { false }
        return if (prefsOn) PermissionStatus.PrefOnly else PermissionStatus.Denied
    }

    /** 快捷方法：有 Shizuku 真实授权，才能真正执行 shell 命令 */
    fun canReallyExecute(): Boolean {
        val s = checkPermission()
        return s is PermissionStatus.Authorized
    }

    /** 仅用于 UI 展示：有任意级别（含 PrefOnly 和 ServiceRunning）都算"准备就绪"状态显示 */
    fun isUiGranted(): Boolean {
        val s = checkPermission()
        return s != PermissionStatus.Denied
    }

    /** 执行 id 命令并解析 uid 级别 */
    private fun runIdLevel(): ShellLevel {
        return try {
            val line = execCapture(emptyList(), "id")
            // uid=0(root) / uid=2000(shell) / uid=10xxx(u0_aXXX)
            val regex = Regex("uid=(\\d+)\\(([^)]+)\\)")
            val match = regex.find(line.firstOrNull() ?: "")?.groupValues?.getOrNull(1)?.toIntOrNull()
            when {
                match == null -> ShellLevel.APP
                match == 0 -> ShellLevel.ROOT
                match == 2000 -> ShellLevel.SHELL
                match < 10000 -> ShellLevel.SHELL   // 其他特权 uid
                else -> ShellLevel.APP              // 普通应用 uid
            }
        } catch (_: Exception) {
            ShellLevel.APP
        }
    }

    /**
     * 真正执行 shell 命令
     *
     * 在 Shizuku 授权后，当前进程会被 shell:shell 身份创建子进程，命令在 shell uid 下执行
     * ，权限等价于 adb shell（非常高），能写 settings、改电池、写 system property、杀进程等。
     *
     * @param commands 命令集合（逐条执行）
     * @return Pair<成功Boolean, 输出行>
     */
    suspend fun execute(commands: List<String>): Pair<Boolean, List<String>> = withContext(Dispatchers.IO) {
        if (commands.isEmpty()) return@withContext true to emptyList()
        if (!canReallyExecute()) {
            // 没有 Shizuku，执行失败
            return@withContext false to listOf("Shizuku 未授权，无法执行")
        }
        var proc: Process? = null
        return@withContext try {
            proc = Runtime.getRuntime().exec("sh")
            DataOutputStream(proc.outputStream.buffered()).use { out ->
                commands.forEach { cmd ->
                    out.writeBytes(cmd + "\n")
                    out.flush()
                }
                out.writeBytes("exit\n")
                out.flush()
            }
            val stdout = BufferedReader(InputStreamReader(proc.inputStream)).readLines()
            val stderr = BufferedReader(InputStreamReader(proc.errorStream)).readLines()
            val code = runCatching { proc.waitFor() }.getOrDefault(1)
            val all = stdout + if (stderr.isEmpty()) emptyList() else listOf("STDERR:") + stderr
            (code == 0) to all
        } catch (t: Throwable) {
            false to listOf(t.message ?: "execute error")
        } finally {
            runCatching { proc?.destroy() }
        }
    }

    /** 执行单命令仅捕获 stdout 第一行（id/pm 这种） */
    private fun execCapture(envs: List<String>?, cmd: String): List<String> {
        return try {
            val proc = if (envs == null) Runtime.getRuntime().exec(cmd)
            else Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            val out = proc.inputStream.bufferedReader().use { it.readLines() }
            proc.waitFor()
            out
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ============================ 具体 Shizuku 命令封装 ============================

    /** 修改系统电量（dumpsys battery set level N） */
    suspend fun setBatteryLevelShizuku(level: Int): Pair<Boolean, List<String>> {
        if (level !in 0..100) return false to listOf("level out of range")
        val (ok, out) = execute(listOf("dumpsys battery set level $level"))
        // 一些系统上 set level 无 stdout，验证一下读出来的值
        val check = if (ok) execute(listOf("dumpsys battery | grep level")) else null
        val merged = out + (check?.second ?: emptyList())
        return (ok || (check?.first == true)) to merged
    }

    /** 开启小米极致模式：settings put global miui_perf_mode 2（具体值 MIUI 会变） */
    suspend fun xiaomiEnableExtremeModeShizuku(): Pair<Boolean, List<String>> {
        val commands = listOf(
            "settings put global miui_perf_mode 2",
            "settings put global game_sdk_optimization 1",
            "settings put secure mi_optimization_enabled 1",
            "settings get global miui_perf_mode"
        )
        val (ok, out) = execute(commands)
        val hasValue = out.any { line -> line.contains("2") }
        return (ok || hasValue) to out
    }

    /** 执行 Scene 激活（如 Scene 安装则拉起其 Shizuku 模式主界面） */
    fun activateSceneShizuku(context: Context): BrandUtils.SceneActivateResult {
        if (!canReallyExecute()) return BrandUtils.SceneActivateResult.NeedShizuku
        if (!BrandUtils.isAppInstalled(context, "com.omarea.scene")) {
            return BrandUtils.SceneActivateResult.NotInstalled
        }
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.omarea.scene")
                ?: return BrandUtils.SceneActivateResult.Failed
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            BrandUtils.SceneActivateResult.Success
        } catch (_: Exception) {
            BrandUtils.SceneActivateResult.Failed
        }
    }

    // ============================ 枚举定义 ============================

    /** Shell 身份 */
    enum class ShellLevel {
        APP,    // 普通应用 uid（10xxx）
        SHELL,  // ADB shell uid=2000
        ROOT    // uid=0
    }

    /** Shizuku 授权状态 */
    sealed class PermissionStatus {
        /** 真正授权，可执行 shell 命令（附带当前 uid 等级） */
        data class Authorized(val level: ShellLevel) : PermissionStatus()
        /** Shizuku 服务已运行但命令权限还没拉到（通常是刚启动服务，重启 APP 即可） */
        data object ServiceRunning : PermissionStatus()
        /** 只有 SharedPreferences 开了，但服务根本没启动（旧版伪授权） */
        data object PrefOnly : PermissionStatus()
        /** 完全未授权（没装/没启动/没开） */
        data object Denied : PermissionStatus()
    }
}
