package com.allinone.toolbox.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于软件") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "全能工具箱",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.height(48.dp)
                    )
                    Text(
                        text = "全能工具箱",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Version 1.0.11",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "应用简介",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "全能工具箱是一款纯本地离线安卓工具应用，提供硬件检测、应用管理、系统设置跳转等实用功能。严格遵循Material 3设计规范，支持全品牌安卓机型。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "无广告",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "无任何广告",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = "无追踪",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "无数据追踪",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "隐私保护",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "隐私本地保护",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "版本日志",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    VersionLogItem("V1.0.11", "工具箱新增五大品牌专属板块（小米/vivo/OPPO/三星/华为）\n小米板块新增「强开极致模式」\n新增电量修改工具（需 Shizuku）\n新增一键激活 Scene 模块")
                    Spacer(modifier = Modifier.height(8.dp))
                    VersionLogItem("V1.0.10", "适配Android 15/16 (API 36)\n支持16KB内存页对齐\n升级Gradle/Kotlin/Compose工具链\n优化Edge-to-Edge全面屏体验")
                    Spacer(modifier = Modifier.height(8.dp))
                    VersionLogItem("V1.0.9", "新增会员激活系统\n新增了解了开发者页面\n新增检查更新功能\n优化硬件检测准确性")
                    Spacer(modifier = Modifier.height(8.dp))
                    VersionLogItem("V1.0.8", "新增系统快捷跳转功能\n新增深色模式支持\n优化用户界面")
                    Spacer(modifier = Modifier.height(8.dp))
                    VersionLogItem("V1.0.7", "新增固件查询页面\n优化性能和兼容性")
                    Spacer(modifier = Modifier.height(8.dp))
                    VersionLogItem("V1.0.5", "首个稳定版本发布\n提供硬件检测基础功能\n提供工具箱核心功能")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "技术信息",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 最低支持: Android 7.0 (API 24)\n• 目标版本: Android 16 (API 36)\n• 开发语言: Kotlin\n• UI框架: Jetpack Compose + Material 3\n• 构建工具: Gradle 8.11.1",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "© 2024-2026 全能工具箱团队",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun VersionLogItem(version: String, content: String) {
    Column {
        Text(
            text = version,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
