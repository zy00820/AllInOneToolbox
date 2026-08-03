package com.allinone.toolbox.ui.tools

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatteryAlert
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
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allinone.toolbox.utils.ActivationUtils
import com.allinone.toolbox.utils.BrandUtils
import com.allinone.toolbox.utils.DeviceUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryModifyScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var sliderValue by remember { mutableStateOf(BrandUtils.getFakeBatteryLevel().coerceAtLeast(50).toFloat()) }
    var appliedLevel by remember { mutableStateOf(BrandUtils.getFakeBatteryLevel()) }
    var showApplyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("电量修改") },
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
            // 状态展示卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (sliderValue >= 20) Icons.Default.BatteryFull
                        else Icons.Default.BatteryAlert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.height(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${sliderValue.toInt()}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (appliedLevel >= 0) "已应用：$appliedLevel%" else "未应用修改",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // 滑块调节
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "目标电量",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0f..100f,
                        steps = 0
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0%", style = MaterialTheme.typography.bodySmall)
                        Text("100%", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // 快速档位
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "快速档位",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 5, 15, 50, 100).forEach { level ->
                            OutlinedButton(
                                onClick = { sliderValue = level.toFloat() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("$level%")
                            }
                        }
                    }
                }
            }

            // 应用按钮
            Button(
                onClick = {
                    if (!ActivationUtils.isMember()) {
                        Toast.makeText(context, "请先激活会员使用此功能", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    showApplyDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("应用电量修改")
            }

            OutlinedButton(
                onClick = {
                    if (BrandUtils.resetBatteryLevel()) {
                        appliedLevel = -1
                        Toast.makeText(context, "已重置电量修改", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = appliedLevel >= 0
            ) {
                Text("重置")
            }

            // 说明卡片
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "功能说明",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val shizukuOk = DeviceUtils.checkShizukuPermission()
                    Text(
                        text = "• Android 系统禁止应用直接篡改真实电量\n" +
                            "• 本工具需 Shizuku 授权后通过 dumpsys battery set level 修改系统读取的电量值\n" +
                            "• 仅用于调试与测试场景，重启或重新插拔电源后恢复\n" +
                            "• 当前 Shizuku 状态：${if (shizukuOk) "已授权" else "未授权"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("确认修改电量") },
            text = {
                Text("将把系统读取的电量修改为 ${sliderValue.toInt()}%，需 Shizuku 授权才能生效。是否继续？")
            },
            confirmButton = {
                TextButton(onClick = {
                    val ok = BrandUtils.setBatteryLevel(sliderValue.toInt())
                    appliedLevel = BrandUtils.getFakeBatteryLevel()
                    showApplyDialog = false
                    Toast.makeText(
                        context,
                        if (ok) "电量已修改为 ${sliderValue.toInt()}%" else "已记录，请授权 Shizuku 后生效",
                        Toast.LENGTH_LONG
                    ).show()
                }) { Text("确认修改") }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDialog = false }) { Text("取消") }
            }
        )
    }
}
