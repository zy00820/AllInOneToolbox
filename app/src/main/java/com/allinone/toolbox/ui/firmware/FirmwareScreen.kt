package com.allinone.toolbox.ui.firmware

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class FirmwareItem(
    val brand: String,
    val model: String,
    val version: String,
    val description: String,
    val size: String,
    val releaseDate: String
)

val sampleFirmwareData = listOf(
    FirmwareItem("小米", "Mi 13", "V1.0.9.0", "稳定版更新", "4.2 GB", "2024-03"),
    FirmwareItem("华为", "Pura 70", "HarmonyOS 4.2", "系统优化", "5.1 GB", "2024-03"),
    FirmwareItem("OPPO", "Find X7", "ColorOS 14.1", "功能更新", "3.8 GB", "2024-02"),
    FirmwareItem("vivo", "X100", "OriginOS 4.0", "稳定性修复", "3.5 GB", "2024-02"),
    FirmwareItem("三星", "S24 Ultra", "One UI 6.1", "新功能", "5.8 GB", "2024-03"),
    FirmwareItem("一加", "Ace 3", "ColorOS 14", "性能优化", "3.2 GB", "2024-01"),
    FirmwareItem("真我", "GT5", "realme UI 5.0", "系统更新", "3.0 GB", "2024-02"),
    FirmwareItem("红米", "K70 Pro", "HyperOS 1.1", "全新系统", "4.5 GB", "2024-03"),
    FirmwareItem("iQOO", "12", "OriginOS 4", "游戏优化", "4.0 GB", "2024-02"),
    FirmwareItem("荣耀", "Magic 6", "MagicOS 8", "AI功能", "5.2 GB", "2024-03")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirmwareScreen(
    paddingValues: androidx.compose.foundation.layout.PaddingValues
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedBrand by remember { mutableStateOf("全部") }

    val brands = listOf("全部", "小米", "华为", "OPPO", "vivo", "三星", "一加", "荣耀")

    val filteredData = sampleFirmwareData.filter { item ->
        val matchesSearch = searchQuery.isEmpty() ||
            item.model.contains(searchQuery, ignoreCase = true) ||
            item.brand.contains(searchQuery, ignoreCase = true) ||
            item.version.contains(searchQuery, ignoreCase = true)
        val matchesBrand = selectedBrand == "全部" || item.brand == selectedBrand
        matchesSearch && matchesBrand
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "固件查询",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "V1.0 本地UI预览（暂不支持联网查询）",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                placeholder = { Text("搜索机型、版本号") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "品牌筛选",
                    style = MaterialTheme.typography.titleSmall
                )
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(48.dp)
                ) {
                    items(brands) { brand ->
                        FilterChip(
                            selected = brand == selectedBrand,
                            onClick = { selectedBrand = brand },
                            label = { Text(brand) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "搜索结果 (${filteredData.size})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(filteredData) { firmware ->
            FirmwareItemCard(firmware)
        }

        if (filteredData.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "无结果",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "暂无匹配的固件信息",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💡 提示",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "V1.0 版本固件查询为本地UI预览，后续版本将支持联网查询官方固件。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FirmwareItemCard(firmware: FirmwareItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${firmware.brand} ${firmware.model}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = firmware.version,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "下载",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = firmware.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "大小: ${firmware.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "发布: ${firmware.releaseDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
