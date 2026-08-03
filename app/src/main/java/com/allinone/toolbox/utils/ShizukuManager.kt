package com.allinone.toolbox.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.allinone.toolbox.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Shizuku 授权管理器（V1.1.1 升级为「官方 Shizuku-API Binder 方案」）
 *
 * 1.1.0 的问题：通过 Runtime.exec("id") 判断身份并不可靠——如果 Shizuku 服务虽然启动了，
 * 但我们 APP 没拿到 Binder，exec 的子进程依然是 uid=10xxx，根本不是 shell。
 *
 * 1.1.1 升级为官方 Shizuku-API 13.1.5（dev.rikka.shizuku:api + provider）：
 *  - AndroidManifest 注册 rikka.shizuku.ShizukuProvider（ContentProvider#call 接收 Binder）
 *  - App.onCreate 注册 BinderReceivedListener / BinderDeadListener 生命周期
 *  - 优先走 `Shizuku.checkSelfPermission() == PERMISSION_GRANTED` 判断授权
 *  - 优先走 `Shizuku.getUid()` 判断 uid=0/2000（真正代表 Shizuku 身份）
 *  - 优先走 `Shizuku.newProcess()` 执行真正 shell 身份的命令
 *  - 保留 Runtime.exec fallback（用于未接入 Shizuku 时的 UI 提示）
 */
object ShizukuManager {

    const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    private const val PREFS = "all_in_one_prefs"
    private const val KEY_PREFS_FLAG = "shizuku_authorized"

    // ================== Binder 生命周期（App.onCreate 调一次） ==================

    private var binderReceived = false
    private var binderDeadCount = 0
    private val binderReceivedListeners = mutableListOf<Runnable>()

    fun initBinderLifecycle(app: Application) {
        runCatching {
            // 注册 Binder 接收到的回调（ContentProvider#call("sendBinder") 后触发）
            rikka.shizuku.Shizuku.addBinderReceivedListener {
                binderReceived = true
                binderDeadCount = 0
                binderReceivedListeners.toList().forEach { it.run() }
            }
            // Binder 死掉的回调（用户杀掉 Shizuku 时触发）
            rikka.shizuku.Shizuku.addBinderDeadListener {
                binderReceived = false
                binderDeadCount++
            }
        }
    }

    /** 外部可订阅 binder 收到事件 */
    fun addBinderReceivedListener(r: Runnable) {
        binderReceivedListeners.add(r)
    }

    // ================== 安装 / 启动 / 下载 ==================

    fun isShizukuAppInstalled(): Boolean = try {
        App.instance.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
        true
    } catch (_: Exception) {
        false
    }

    fun launchShizukuApp(context: Context): Boolean = try {
        val intent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
            ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    } catch (_: Exception) {
        false
    }

    fun openShizukuDownload(context: Context) {
        val urls = listOf(
            "https://shizuku.rikka.app/download/",
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

    // ================== 核心：官方 Binder 级权限检测 ==================

    /** Shizuku Binder 是否已交付（未收到 Binder 时所有 Shizuku.* 调用抛 IllegalStateException） */
    private fun isBinderAlive(): Boolean = runCatching {
        rikka.shizuku.Shizuku.pingBinder()
    }.getOrDefault(false) || binderReceived

    /** 拿到当前通过 Shizuku 运行时的真实 uid：0=root / 2000=shell / -1=没权限 */
    private fun getShizukuUid(): Int = runCatching {
        if (isBinderAlive()) rikka.shizuku.Shizuku.getUid() else -1
    }.getOrDefault(-1)

    /** 是否通过 Shizuku.checkSelfPermission 拿到了正式授权弹窗后的 GRANTED */
    private fun isShizukuPermissionGranted(): Boolean = runCatching {
        if (rikka.shizuku.Shizuku.isPreV11()) return false
        rikka.shizuku.Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    /** 弹出 Shizuku 官方授权弹窗（V1.1.1 UI 可以调这个） */
    fun requestOfficialPermission(requestCode: Int = 10086): Boolean = runCatching {
        if (!isBinderAlive()) return false
        if (rikka.shizuku.Shizuku.isPreV11()) return false
        if (isShizukuPermissionGranted()) return true
        rikka.shizuku.Shizuku.requestPermission(requestCode)
        true
    }.getOrDefault(false)

    private fun isShizukuServiceRunning(): Boolean = try {
        val proc = Runtime.getRuntime().exec(arrayOf("sh", "-c", "ps -A | grep -E 'shizuku|^shell'"))
        val text = proc.inputStream.bufferedReader().use { it.readText() }
        proc.waitFor()
        text.contains("shizuku", ignoreCase = true) || text.contains(SHIZUKU_PACKAGE)
    } catch (_: Exception) {
        false
    }

    /**
     * V1.1.1 核心：真正通过「官方 Shizuku API Binder 级」判定授权
     * 优先级：
     *  A. Shizuku 官方 Binder 已拿到 + checkSelfPermission=GRANTED + getUid=0/2000 → Authorized
     *  B. Shizuku Binder 已拿到，但 checkSelfPermission=DENIED → ServiceRunning（用户还没点允许）
     *  C. 没 Binder，但 App 已安装且服务进程存在 → ServiceRunning
     *  D. 只剩 SharedPreferences 开关=PrefOnly（兼容旧版）
     *  E. 其他 Denied
     */
    fun checkPermission(): PermissionStatus {
        val alive = isBinderAlive()
        val granted = isShizukuPermissionGranted()
        val uid = getShizukuUid()

        // A) 官方 Binder 级真授权
        if (alive && granted) {
            val level = when (uid) {
                0 -> ShellLevel.ROOT
                2000 -> ShellLevel.SHELL
                in 10000 until 99999 -> ShellLevel.APP
                else -> ShellLevel.SHELL  // 其他低于 10000 的 uid 都当 shell 级处理
            }
            // 兼容：uid 返回 -1 但 granted=true（旧 Shizuku），再用 id 命令兜底
            if (uid == -1) {
                val level2 = runIdLevelFallback()
                if (level2 == ShellLevel.ROOT || level2 == ShellLevel.SHELL) {
                    return PermissionStatus.Authorized(level2)
                }
            } else {
                return PermissionStatus.Authorized(level)
            }
        }

        // B) Binder 收到，但还没弹窗允许
        if (alive) return PermissionStatus.ServiceRunning

        // C) 没Binder，但App和服务都在
        if (isShizukuAppInstalled() && isShizukuServiceRunning()) {
            return PermissionStatus.ServiceRunning
        }

        // D) PrefOnly 兼容旧版
        val prefsOn = try {
            App.instance.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_PREFS_FLAG, false)
        } catch (_: Exception) { false }
        return if (prefsOn) PermissionStatus.PrefOnly else PermissionStatus.Denied
    }

    fun canReallyExecute(): Boolean = checkPermission() is PermissionStatus.Authorized
    fun isUiGranted(): Boolean = checkPermission() != PermissionStatus.Denied

    // ================== 命令执行（优先官方 newProcess，fallback Runtime.exec） ==================

    suspend fun execute(commands: List<String>): Pair<Boolean, List<String>> = withContext(Dispatchers.IO) {
        if (commands.isEmpty()) return@withContext true to emptyList()
        val st = checkPermission()
        if (st !is PermissionStatus.Authorized) {
            return@withContext false to listOf("Shizuku 未真正授权（当前=${st::class.simpleName}）")
        }
        // 优先官方 Shizuku.newProcess（真正 shell 身份执行）
        val alive = runCatching { rikka.shizuku.Shizuku.pingBinder() }.getOrDefault(false)
        val result = if (alive) runCatching {
            executeOfficial(commands)
        }.getOrNull() else null
        result ?: executeFallback(commands)
    }

    /**
     * 走「官方 Shizuku 机制」真正创建 shell 身份进程。
     *
     * 注意：从 Shizuku-API 13.x 开始，Shizuku.newProcess(cmd, env, dir) 被标记为
     * internal/private（官方推荐使用 UserService + AIDL）。但对于我们"执行几条命令"的
     * 轻量场景，反射直接调用更合适。当反射失败时，会自动 fallback 到 Runtime.exec 兜底。
     */
    private fun executeOfficial(commands: List<String>): Pair<Boolean, List<String>> {
        var proc: Process? = null
        return try {
            proc = newProcessOfficial(arrayOf("sh"), null, null)
            val script = commands.joinToString("\n", postfix = "\nexit\n")
            DataOutputStream(proc.outputStream.buffered()).use { out ->
                out.writeBytes(script)
                out.flush()
            }
            val stdout = BufferedReader(InputStreamReader(proc.inputStream)).readLines()
            val stderr = BufferedReader(InputStreamReader(proc.errorStream)).readLines()
            val code = runCatching { proc.waitFor() }.getOrDefault(1)
            val merged = stdout + (if (stderr.isEmpty()) emptyList() else listOf("STDERR:") + stderr)
            (code == 0) to merged
        } catch (t: Throwable) {
            false to listOf("[official-newProcess fallback]", t.message ?: t.javaClass.simpleName)
        } finally {
            runCatching { proc?.destroy() }
        }
    }

    /**
     * 反射调用 rikka.shizuku.Shizuku#newProcess(String[], String[], String)
     * 该方法签名在 Shizuku-API 12/13 上都是 stable 的，只改了可见性 internal。
     * 成功返回 Process（实为 ShizukuRemoteProcess），失败抛异常由上层 fallback。
     */
    private fun newProcessOfficial(cmd: Array<String>, env: Array<String>?, dir: String?): Process {
        val shizukuCls = rikka.shizuku.Shizuku::class.java
        val method = shizukuCls.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        method.isAccessible = true
        return method.invoke(null, cmd, env, dir) as Process
    }

    /** Fallback（老版本 Shizuku 没 newProcess 权限时兜底） */
    private fun executeFallback(commands: List<String>): Pair<Boolean, List<String>> {
        var proc: Process? = null
        return try {
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

    private fun runIdLevelFallback(): ShellLevel = try {
        val proc = Runtime.getRuntime().exec(arrayOf("sh", "-c", "id"))
        val firstLine = proc.inputStream.bufferedReader().use { it.readLine() ?: "" }
        proc.waitFor()
        val match = Regex("uid=(\\d+)\\(([^)]+)\\)").find(firstLine)
            ?.groupValues?.getOrNull(1)?.toIntOrNull()
        when (match) {
            0 -> ShellLevel.ROOT
            2000 -> ShellLevel.SHELL
            null -> ShellLevel.APP
            in 1..9999 -> ShellLevel.SHELL
            else -> ShellLevel.APP
        }
    } catch (_: Exception) {
        ShellLevel.APP
    }

    // ============================ 具体命令封装 ============================

    suspend fun setBatteryLevelShizuku(level: Int): Pair<Boolean, List<String>> {
        if (level !in 0..100) return false to listOf("level out of range")
        val (ok, out) = execute(listOf("dumpsys battery set level $level"))
        val check = if (ok) execute(listOf("dumpsys battery | grep -E 'level|LEVEL'")) else null
        return (ok || (check?.first == true)) to (out + (check?.second ?: emptyList()))
    }

    suspend fun xiaomiEnableExtremeModeShizuku(): Pair<Boolean, List<String>> {
        val commands = listOf(
            "settings put global miui_perf_mode 2",
            "settings put global game_sdk_optimization 1",
            "settings put secure mi_optimization_enabled 1",
            "settings get global miui_perf_mode"
        )
        val (ok, out) = execute(commands)
        val hasValue = out.any { line -> "2" in line }
        return (ok || hasValue) to out
    }

    fun activateSceneShizuku(context: Context): BrandUtils.SceneActivateResult {
        if (checkPermission() !is PermissionStatus.Authorized) return BrandUtils.SceneActivateResult.NeedShizuku
        if (!BrandUtils.isAppInstalled(context, "com.omarea.scene")) return BrandUtils.SceneActivateResult.NotInstalled
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

    // ============================ 枚举 ============================

    enum class ShellLevel { APP, SHELL, ROOT }

    sealed class PermissionStatus {
        data class Authorized(val level: ShellLevel) : PermissionStatus()
        object ServiceRunning : PermissionStatus()
        object PrefOnly : PermissionStatus()
        object Denied : PermissionStatus()
    }
}
