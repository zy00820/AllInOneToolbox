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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.allinone.toolbox.BuildConfig
import com.allinone.toolbox.ui.theme.ThemeViewModel
import com.allinone.toolbox.utils.ActivationUtils
import com.allinone.toolbox.utils.DeviceUtils
import com.allinone.toolbox.utils.ShizukuManager
import com.allinone.toolbox.utils.UpdateChecker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    themeViewModel: ThemeViewModel,
    onNavigateToActivate: () -> Unit,
    onNavigateToAboutDeveloper: () -> Unit,
    onNavigateToAboutApp: () -> Unit,
    onNavigateToShizuku: () -> Unit  // V1.1.0 新增：跳转到真正的 Shizuku 授权页
) {
    val context = LocalContext.current
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val isMember by themeViewModel.isMember.collectAsState()
    // V1.1.0：真正显示 Shizuku 状态（不再是伪开关）
    var shizukuRealStatus by remember { mutableStateOf(ShizukuManager.checkPermission()) }
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateChecker.UpdateResult?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ProfileHeader(isMember = isMember)
        }

        item {
            SectionTitle("外观设置")
        }

        item {
            SettingsCard(
                icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                title = "深色模式",
                trailing = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { themeViewModel.setDarkMode(it) }
                    )
                }
            )
        }

        item {
            SectionTitle("权限与安全")
        }

        item {
            // V1.1.0：真正的Shizuku状态文字（不再是伪开关）
            val (statusText, statusColor) = when (shizukuRealStatus) {
                is ShizukuManager.PermissionStatus.Authorized ->
                    "已真正授权（uid " + (shizukuRealStatus as ShizukuManager.PermissionStatus.Authorized).level + "）" to
                        MaterialTheme.colorScheme.primary
                ShizukuManager.PermissionStatus.ServiceRunning ->
                    "服务运行中，待授权..." to Color(0xFFF9A825)
                ShizukuManager.PermissionStatus.PrefOnly ->
                    "仅UI伪授权（旧版）" to Color(0xFFEF6C00)
                ShizukuManager.PermissionStatus.Denied ->
                    "未授权" to MaterialTheme.colorScheme.error
            }
            SettingsCard(
                icon = Icons.Default.DeveloperBoard,
                title = "Shizuku权限",
                subtitle = statusText,
                onClick = {
                    // V1.1.0：跳转到专用授权管理页（不再Toggle伪开关）
                    shizukuRealStatus = ShizukuManager.checkPermission()
                    onNavigateToShizuku()
                },
                trailing = {
                    FilterChip(
                        selected = shizukuRealStatus is ShizukuManager.PermissionStatus.Authorized,
                        onClick = {
                            shizukuRealStatus = ShizukuManager.checkPermission()
                            onNavigateToShizuku()
                        },
                        label = {
                            Text(
                                text = when (shizukuRealStatus) {
                                    is ShizukuManager.PermissionStatus.Authorized -> "已授权"
                                    ShizukuManager.PermissionStatus.ServiceRunning -> "待授权"
                                    ShizukuManager.PermissionStatus.PrefOnly -> "伪授权"
                                    ShizukuManager.PermissionStatus.Denied -> "未授权"
                                },
                                color = statusColor
                            )
                        }
                    )
                }
            )
        }

        item {
            SectionTitle("会员")
        }

        item {
            SettingsCard(
                icon = Icons.Default.Verified,
                title = "激活会员（LITE / PRO 双重验证）",
                subtitle = when {
                    ActivationUtils.isProMember() -> "会员 PRO · 已激活 · 全部权益解锁"
                    ActivationUtils.isMember() -> "会员 LITE · 已激活 · 可继续激活 PRO"
                    else -> "第一码激活 LITE，第二码升级 PRO"
                },
                onClick = onNavigateToActivate
            )
        }

        item {
            SectionTitle("其他")
        }

        item {
            SettingsCard(
                icon = Icons.Default.Update,
                title = "检查更新",
                subtitle = if (checking) "正在检查…" else "当前版本 V${BuildConfig.VERSION_NAME}",
                onClick = {
                    if (checking) return@SettingsCard
                    checking = true
                    scope.launch {
                        val result = UpdateChecker.checkLatestVersion()
                        checking = false
                        when (result) {
                            is UpdateChecker.UpdateResult.Success -> {
                                if (result.hasUpdate) {
                                    updateResult = result
                                } else {
                                    Toast.makeText(
                                        context,
                                        "当前已是最新版本 (V${result.latestVersion})",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            is UpdateChecker.UpdateResult.Failed -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            )
        }

        item {
            SettingsCard(
                icon = Icons.Default.Info,
                title = "关于软件",
                subtitle = "全能工具箱 V${BuildConfig.VERSION_NAME}",
                onClick = onNavigateToAboutApp
            )
        }

        item {
            SettingsCard(
                icon = Icons.Default.Person,
                title = "了解开发者",
                subtitle = "张岳 · 黄松",
                onClick = onNavigateToAboutDeveloper
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "全能工具箱 V${BuildConfig.VERSION_NAME}\n纯本地离线 · 检查更新联网查询 GitHub",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // 发现新版本对话框
    updateResult?.let { result ->
        if (result is UpdateChecker.UpdateResult.Success) {
            val mirrors = remember(result) { UpdateChecker.getMirrorUrls(result.apkUrl) }
            AlertDialog(
                onDismissRequest = { updateResult = null },
                title = { Text("发现新版本 V${result.latestVersion}") },
                text = {
                    Column {
                        Text("当前版本：V${BuildConfig.VERSION_NAME}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "选择下载源（推荐 gh-proxy.com 节点，国内最快）：",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        mirrors.forEach { (url, label) ->
                            TextButton(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse(url)
                                        ).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "无法打开浏览器", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("下载：$label")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { updateResult = null }) { Text("稍后") }
                }
            )
        }
    }
}

@Composable
private fun ProfileHeader(isMember: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isMember) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = if (isMember) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
            Column {
                Text(
                    text = if (isMember) "会员用户" else "普通用户",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isMember) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
                Text(
                    text = "设备代码: ${DeviceUtils.getDeviceCode()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isMember) {
                        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        onClick = onClick ?: {},
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailing != null) {
                trailing()
            }
        }
    }
}
