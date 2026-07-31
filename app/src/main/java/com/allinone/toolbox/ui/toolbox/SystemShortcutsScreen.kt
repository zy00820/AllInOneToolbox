package com.allinone.toolbox.ui.toolbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allinone.toolbox.utils.DeviceUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemShortcutsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("系统快捷跳转") },
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
                Text(
                    text = "系统设置",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                ShortcutItem(
                    icon = Icons.Default.Settings,
                    title = "原生设置页",
                    description = "打开系统完整设置",
                    onClick = { DeviceUtils.openSystemSettings(context) }
                )
            }

            item {
                ShortcutItem(
                    icon = Icons.Default.DeveloperBoard,
                    title = "开发者选项",
                    description = "USB调试、性能监控等",
                    onClick = { DeviceUtils.openDeveloperOptions(context) }
                )
            }

            item {
                ShortcutItem(
                    icon = Icons.Default.DisplaySettings,
                    title = "显示设置",
                    description = "亮度、字体、屏幕超时",
                    onClick = { DeviceUtils.openDisplaySettings(context) }
                )
            }

            item {
                Text(
                    text = "硬件检测",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ShortcutItem(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "电池检测",
                    description = "查看详细电池信息",
                    onClick = { DeviceUtils.openDisplaySettings(context) }
                )
            }

            item {
                ShortcutItem(
                    icon = Icons.Default.Storage,
                    title = "存储设置",
                    description = "查看存储空间使用情况",
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )
            }

            item {
                Text(
                    text = "其他设置",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ShortcutItem(
                    icon = Icons.Default.Wifi,
                    title = "WiFi设置",
                    description = "连接和管理WiFi网络",
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )
            }

            item {
                ShortcutItem(
                    icon = Icons.Default.Bluetooth,
                    title = "蓝牙设置",
                    description = "管理蓝牙设备连接",
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )
            }

            item {
                ShortcutItem(
                    icon = Icons.Default.Security,
                    title = "安全设置",
                    description = "锁屏、加密、权限",
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )
            }

            item {
                ShortcutItem(
                    icon = Icons.Default.Notifications,
                    title = "通知设置",
                    description = "管理应用通知",
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )
            }

            item {
                ShortcutItem(
                    icon = Icons.Default.LocationOn,
                    title = "位置设置",
                    description = "管理定位服务",
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShortcutItem(
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
