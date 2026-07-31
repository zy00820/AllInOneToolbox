package com.allinone.toolbox.ui.hardware

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allinone.toolbox.data.HardwareInfo
import com.allinone.toolbox.utils.DeviceUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hardwareInfo by remember { mutableStateOf<HardwareInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            hardwareInfo = DeviceUtils.getHardwareInfo()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("硬件检测") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (hardwareInfo != null) {
                            scope.launch {
                                val success = DeviceUtils.exportHardwareReport(hardwareInfo!!)
                                Toast.makeText(
                                    context,
                                    if (success) "报告已导出" else "导出失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "导出报告")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            hardwareInfo?.let { info ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoGroupCard(
                        title = "CPU信息",
                        icon = Icons.Default.Memory,
                        items = listOf(
                            "型号" to info.cpuName,
                            "核心数" to "${info.cpuCores} 核",
                            "最高频率" to info.cpuMaxFreq
                        )
                    )

                    InfoGroupCard(
                        title = "GPU信息",
                        icon = Icons.Default.Memory,
                        items = listOf(
                            "型号" to info.gpuName,
                            "厂商" to info.gpuVendor
                        )
                    )

                    InfoGroupCard(
                        title = "屏幕参数",
                        icon = Icons.Default.DisplaySettings,
                        items = listOf(
                            "分辨率" to info.screenResolution,
                            "密度" to info.screenDensity,
                            "尺寸" to info.screenSize
                        )
                    )

                    InfoGroupCard(
                        title = "电池信息",
                        icon = Icons.Default.BatteryChargingFull,
                        items = listOf(
                            "电量" to "${info.batteryLevel}%",
                            "状态" to info.batteryStatus,
                            "技术" to info.batteryTechnology
                        )
                    )

                    InfoGroupCard(
                        title = "系统信息",
                        icon = Icons.Default.SystemUpdate,
                        items = listOf(
                            "系统版本" to info.androidVersion,
                            "构建号" to info.buildNumber
                        )
                    )

                    InfoGroupCard(
                        title = "设备信息",
                        icon = Icons.Default.Smartphone,
                        items = listOf(
                            "品牌" to info.deviceBrand,
                            "型号" to info.deviceModel,
                            "设备代号" to info.deviceCodeName,
                            "设备代码" to DeviceUtils.getDeviceCode()
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoGroupCard(
    title: String,
    icon: ImageVector,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (label == "电量" && value.contains("%")) {
                    val level = value.replace("%", "").toIntOrNull() ?: 0
                    LinearProgressIndicator(
                        progress = { level / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                    )
                }
            }
        }
    }
}
