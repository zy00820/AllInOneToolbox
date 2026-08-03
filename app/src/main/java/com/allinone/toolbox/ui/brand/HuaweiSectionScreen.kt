package com.allinone.toolbox.ui.brand

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Launch
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
fun HuaweiSectionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("华为安卓板块") },
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
                    title = "HUAWEI / HONOR",
                    subtitle = "HarmonyOS / EMUI 专属功能"
                )
            }

            item { SectionTitle("华为系统工具") }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.Security,
                    title = "手机管家",
                    description = "清理加速、流量管理、骚扰拦截",
                    onClick = {
                        val ok = BrandUtils.huaweiPhoneManager(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "电池管理",
                    description = "华为省电模式、启动管理",
                    onClick = {
                        val ok = BrandUtils.huaweiBatterySettings(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.Launch,
                    title = "应用启动管理",
                    description = "管理应用自启动与关联启动",
                    onClick = {
                        val ok = BrandUtils.huaweiAppLaunch(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.Apps,
                    title = "应用市场",
                    description = "华为应用市场",
                    onClick = {
                        val ok = BrandUtils.huaweiAppMarket(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
