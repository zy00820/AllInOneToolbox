package com.allinone.toolbox.ui.brand

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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allinone.toolbox.utils.ActivationUtils
import com.allinone.toolbox.utils.BrandUtils
import com.allinone.toolbox.utils.DeviceUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XiaomiSectionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var extremeOn by remember { mutableStateOf(BrandUtils.isXiaomiExtremeModeOn()) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("小米板块") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
            item {
                BrandHeader(
                    title = "Xiaomi / Redmi / POCO",
                    subtitle = "MIUI / HyperOS 专属功能"
                )
            }

            // 极致模式卡片（小米板块核心特色）
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "强开极致模式",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "强制拉满 CPU/GPU 频率，关闭温控节流，用于游戏压榨性能。\n本功能需 Shizuku 授权后由系统层写入性能标志位，仅 MIUI/HyperOS 机型生效。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (extremeOn) "当前状态：已开启" else "当前状态：未开启",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!ActivationUtils.isProMember()) {
                                        Toast.makeText(context, "此功能为 PRO 专属，请先激活会员 PRO", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    showConfirmDialog = true
                                }
                            ) {
                                Text(if (extremeOn) "重新开启" else "一键开启（PRO）")
                            }
                            OutlinedButton(
                                onClick = {
                                    if (BrandUtils.xiaomiDisableExtremeMode()) {
                                        extremeOn = false
                                        Toast.makeText(context, "已关闭极致模式", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = extremeOn
                            ) {
                                Text("关闭")
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle("小米系统工具")
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.Security,
                    title = "安全中心",
                    description = "打开 MIUI 安全中心主界面",
                    onClick = {
                        val ok = BrandUtils.xiaomiSecurityCenter(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.Security,
                    title = "手机管家",
                    description = "清理加速、病毒扫描、权限管理",
                    onClick = {
                        val ok = BrandUtils.xiaomiPhoneManager(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "省电与电池",
                    description = "查看 MIUI 电池管理、省电模式",
                    onClick = {
                        val ok = BrandUtils.xiaomiBatterySettings(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.DeveloperBoard,
                    title = "开发者选项",
                    description = "MIUI 开发者选项入口",
                    onClick = { BrandUtils.xiaomiDeveloperOptions(context) }
                )
            }
        }
    }

    // 极致模式开启确认弹窗
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("开启极致模式？") },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text("开启后将关闭温控节流，可能造成发热与耗电增加。")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val shizukuOk = DeviceUtils.checkShizukuPermission()
                    Text(
                        text = if (shizukuOk) "Shizuku：已授权 ✓" else "Shizuku：未授权 ✗（开启后仅记录状态）",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (shizukuOk) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ok = BrandUtils.xiaomiEnableExtremeMode(context)
                        extremeOn = BrandUtils.isXiaomiExtremeModeOn()
                        showConfirmDialog = false
                        Toast.makeText(
                            context,
                            if (ok) "极致模式已开启" else "开启失败（请先授权 Shizuku）",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                ) { Text("确认开启") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("取消") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BrandHeader(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BrandShortcutItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
