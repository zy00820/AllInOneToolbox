package com.allinone.toolbox.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allinone.toolbox.utils.ActivationUtils
import com.allinone.toolbox.utils.DeviceUtils
import com.allinone.toolbox.utils.ShizukuManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Shizuku 授权引导界面（V1.1.0 新增，修复伪授权问题）
 *
 * 真正做了三件事：
 *  1. 检测 Shizuku App 是否安装（moe.shizuku.privileged.api）
 *  2. 检测 Shizuku 服务进程是否在运行
 *  3. 执行 `id` 命令验证当前 uid=0/2000（真正能跑 shell）
 *
 * 替代旧版在 ProfileScreen 里直接 Toggle Switch 的伪授权。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShizukuPermissionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 实时状态：App安装、服务运行、uid等级（真正授权）
    var appInstalled by remember { mutableStateOf(ShizukuManager.isShizukuAppInstalled()) }
    var permissionStatus by remember { mutableStateOf<ShizukuManager.PermissionStatus>(ShizukuManager.checkPermission()) }
    // 兼容旧版 UI 开关（仅当用户是会员才能改）
    var prefsToggle by remember {
        mutableStateOf(
            try {
                context.getSharedPreferences("all_in_one_prefs", android.content.Context.MODE_PRIVATE)
                    .getBoolean("shizuku_authorized", false)
            } catch (_: Exception) { false }
        )
    }
    var refreshing by remember { mutableStateOf(false) }
    var hintLogs by remember { mutableStateOf(listOf<String>()) }

    // 进入页面 + 每2s 自动刷新一次（等用户切去 Shizuku App 启动服务回来能看到变化）
    LaunchedEffect(Unit) {
        while (true) {
            refresh(
                onApp = { appInstalled = it },
                onStatus = { permissionStatus = it },
                onPrefs = { prefsToggle = it }
            )
            delay(2000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shizuku 授权管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (refreshing) return@IconButton
                            refreshing = true
                            scope.launch {
                                refresh(
                                    onApp = { appInstalled = it },
                                    onStatus = { permissionStatus = it },
                                    onPrefs = { prefsToggle = it }
                                )
                                refreshing = false
                            }
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新状态")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1) 总览卡片
            item {
                val (icon, color, title, subtitle) = when (permissionStatus) {
                    is ShizukuManager.PermissionStatus.Authorized ->
                        StatusTriple(
                            Icons.Default.CheckCircle,
                            MaterialTheme.colorScheme.primary,
                            "已真正授权 ✓",
                            "当前权限等级：${(permissionStatus as ShizukuManager.PermissionStatus.Authorized).level}"
                        )
                    ShizukuManager.PermissionStatus.ServiceRunning ->
                        StatusTriple(
                            Icons.Default.Warning,
                            Color(0xFFF9A825),
                            "服务已运行，但命令权限未就绪",
                            "请在 Shizuku App 内确认授权本应用；或重启本 APP 后重测"
                        )
                    ShizukuManager.PermissionStatus.PrefOnly ->
                        StatusTriple(
                            Icons.Default.Warning,
                            Color(0xFFEF6C00),
                            "仅 UI 伪授权（V1.0.x 旧状态）",
                            "SharedPreferences 开关为 true，但 Shizuku 未真正生效，请按下方步骤授权"
                        )
                    ShizukuManager.PermissionStatus.Denied ->
                        StatusTriple(
                            Icons.Default.Error,
                            MaterialTheme.colorScheme.error,
                            "未授权",
                            "Shizuku App 未安装 / 未启动服务，高级功能无法真正执行"
                        )
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = color.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(icon, contentDescription = null, tint = color)
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 2) 状态清单
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusRow(
                        label = "Shizuku App 已安装",
                        ok = appInstalled,
                        hint = "包名：moe.shizuku.privileged.api"
                    )
                    StatusRow(
                        label = "Shizuku 服务已运行",
                        ok = permissionStatus != ShizukuManager.PermissionStatus.Denied,
                        hint = "若未运行，请在 Shizuku App 内点「启动」"
                    )
                    StatusRow(
                        label = "本应用已真正获得 Shell 权限",
                        ok = permissionStatus is ShizukuManager.PermissionStatus.Authorized,
                        hint = "执行 `id` 命令验证 uid=0/2000"
                    )
                }
            }

            // 3) 兼容旧版 UI 开关（仅作 UI 显示用途，真正执行命令看上面三项）
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "本地授权开关（V1.0.x 兼容）",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "仅用于旧 UI 提示；功能真正可用看上方「已真正获得 Shell 权限」",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = prefsToggle,
                                onCheckedChange = { v ->
                                    if (!ActivationUtils.isMember()) {
                                        Toast.makeText(context, "请先激活会员", Toast.LENGTH_SHORT).show()
                                        return@Switch
                                    }
                                    try {
                                        context.getSharedPreferences("all_in_one_prefs", android.content.Context.MODE_PRIVATE)
                                            .edit().putBoolean("shizuku_authorized", v).apply()
                                        prefsToggle = v
                                        Toast.makeText(
                                            context,
                                            if (v) "已记录授权状态" else "已关闭授权开关",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (_: Exception) {
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 4) 操作按钮
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!appInstalled) {
                        Button(
                            onClick = { ShizukuManager.openShizukuDownload(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.height(0.dp))
                            Text("  下载 / 安装 Shizuku")
                        }
                    }
                    if (appInstalled) {
                        Button(
                            onClick = {
                                val ok = ShizukuManager.launchShizukuApp(context)
                                if (!ok) Toast.makeText(context, "启动失败", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Launch, contentDescription = null)
                            Spacer(modifier = Modifier.height(0.dp))
                            Text("  打开 Shizuku App（启动服务）")
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                refreshing = true
                                val (ok, logs) = ShizukuManager.execute(listOf("id", "echo ---", "ls /system/bin/sh"))
                                refreshing = false
                                hintLogs = listOf("返回码：${if (ok) "0（成功）" else "非0"}") + logs
                                Toast.makeText(
                                    context,
                                    if (ok) "测试命令执行成功 ✓" else "无法执行命令（未授权）",
                                    Toast.LENGTH_SHORT
                                ).show()
                                refresh(
                                    onApp = { appInstalled = it },
                                    onStatus = { permissionStatus = it },
                                    onPrefs = { prefsToggle = it }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null)
                        Spacer(modifier = Modifier.height(0.dp))
                        Text("  测试：执行 id 命令验证")
                    }
                }
            }

            // 5) 测试日志
            if (hintLogs.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "测试日志",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            hintLogs.forEach { line ->
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 6) 使用教程
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "授权教程（四步）",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val steps = listOf(
                            "① 点击上方按钮，前往 GitHub 下载并安装 Shizuku App",
                            "② 在 Shizuku App 内，通过「无线调试」或「adb 启动」启动服务",
                            "③ 服务启动后，在 Shizuku「已授权应用」中允许本应用",
                            "④ 回到本页，点击「测试：执行 id 命令验证」，看到 uid=0 或 uid=2000 即成功"
                        )
                        steps.forEach { step ->
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 7) 提示：当前功能真正可用性
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (DeviceUtils.canShizukuReallyExecute())
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (DeviceUtils.canShizukuReallyExecute())
                                    Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (DeviceUtils.canShizukuReallyExecute())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(0.dp))
                            Text(
                                text = if (DeviceUtils.canShizukuReallyExecute())
                                    "  高级功能（极致模式 / 电量修改 / Scene）真正可用 ✓"
                                else
                                    "  高级功能将失败（未真正授权）",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class StatusTriple(
    val icon: ImageVector,
    val color: Color,
    val title: String,
    val subtitle: String
)

@Composable
private fun StatusRow(label: String, ok: Boolean, hint: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = if (ok) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

/** 统一的刷新回调 */
private fun refresh(
    onApp: (Boolean) -> Unit,
    onStatus: (ShizukuManager.PermissionStatus) -> Unit,
    onPrefs: (Boolean) -> Unit
) {
    onApp(ShizukuManager.isShizukuAppInstalled())
    onStatus(ShizukuManager.checkPermission())
    onPrefs(
        try {
            val prefs = AppSafe.context().getSharedPreferences("all_in_one_prefs", android.content.Context.MODE_PRIVATE)
            prefs.getBoolean("shizuku_authorized", false)
        } catch (_: Exception) { false }
    )
}

/** 避免在 Composable 顶层直接拿 App.instance 造成初始化崩溃 */
private object AppSafe {
    fun context(): android.content.Context = com.allinone.toolbox.App.instance
}
