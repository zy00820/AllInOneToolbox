package com.allinone.toolbox.ui.brand

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.allinone.toolbox.utils.BrandUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamsungSectionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("三星板块") },
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
                    title = "Samsung Galaxy",
                    subtitle = "One UI 专属功能"
                )
            }

            item { SectionTitle("三星系统工具") }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.Security,
                    title = "设备维护",
                    description = "电池、存储、内存、安全一键优化",
                    onClick = {
                        val ok = BrandUtils.samsungDeviceCare(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "电池管理",
                    description = "One UI 电池与省电模式",
                    onClick = {
                        val ok = BrandUtils.samsungBatterySettings(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.VideogameAsset,
                    title = "游戏启动器",
                    description = "三星游戏启动器、游戏插件",
                    onClick = {
                        val ok = BrandUtils.samsungGameLauncher(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.Palette,
                    title = "主题商店",
                    description = "Galaxy Themes 主题与壁纸",
                    onClick = {
                        val ok = BrandUtils.samsungThemeStore(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
