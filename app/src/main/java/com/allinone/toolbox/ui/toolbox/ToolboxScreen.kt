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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxScreen(
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onNavigateToAppExtract: () -> Unit,
    onNavigateToAppInfo: () -> Unit,
    onNavigateToSystemShortcuts: () -> Unit,
    onNavigateToXiaomi: () -> Unit,
    onNavigateToVivo: () -> Unit,
    onNavigateToOppo: () -> Unit,
    onNavigateToSamsung: () -> Unit,
    onNavigateToHuawei: () -> Unit,
    onNavigateToBatteryModify: () -> Unit,
    onNavigateToSceneActivate: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "工具箱",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "纯本地功能合集",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 品牌专属板块
        item { SectionHeader("品牌专属") }

        item {
            ToolItem(
                icon = Icons.Default.PhoneAndroid,
                title = "小米板块",
                description = "MIUI/HyperOS · 含强开极致模式",
                onClick = onNavigateToXiaomi
            )
        }

        item {
            ToolItem(
                icon = Icons.Default.PhoneAndroid,
                title = "vivo 板块",
                description = "OriginOS · i管家、游戏魔盒",
                onClick = onNavigateToVivo
            )
        }

        item {
            ToolItem(
                icon = Icons.Default.PhoneAndroid,
                title = "OPPO 板块",
                description = "ColorOS · 手机管家、游戏空间",
                onClick = onNavigateToOppo
            )
        }

        item {
            ToolItem(
                icon = Icons.Default.PhoneAndroid,
                title = "三星板块",
                description = "One UI · 设备维护、游戏启动器",
                onClick = onNavigateToSamsung
            )
        }

        item {
            ToolItem(
                icon = Icons.Default.PhoneAndroid,
                title = "华为安卓板块",
                description = "HarmonyOS/EMUI · 手机管家、启动管理",
                onClick = onNavigateToHuawei
            )
        }

        // 高级工具
        item { SectionHeader("高级工具") }

        item {
            ToolItem(
                icon = Icons.Default.BatteryChargingFull,
                title = "电量修改",
                description = "修改系统读取的电量值（需 Shizuku）",
                onClick = onNavigateToBatteryModify
            )
        }

        item {
            ToolItem(
                icon = Icons.Default.Bolt,
                title = "一键激活 Scene 模块",
                description = "性能调度、温控解锁工具箱",
                onClick = onNavigateToSceneActivate
            )
        }

        // 应用工具
        item { SectionHeader("应用工具") }

        item {
            ToolItem(
                icon = Icons.Default.Archive,
                title = "APK提取",
                description = "提取已安装应用的APK文件",
                onClick = onNavigateToAppExtract
            )
        }

        item {
            ToolItem(
                icon = Icons.Default.Apps,
                title = "应用信息",
                description = "查看已安装应用的详细信息",
                onClick = onNavigateToAppInfo
            )
        }

        // 系统工具
        item { SectionHeader("系统工具") }

        item {
            ToolItem(
                icon = Icons.Default.Settings,
                title = "系统快捷跳转",
                description = "快速跳转系统设置、开发者选项等",
                onClick = onNavigateToSystemShortcuts
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
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
}
