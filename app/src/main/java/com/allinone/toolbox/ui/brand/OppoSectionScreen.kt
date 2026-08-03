package com.allinone.toolbox.ui.brand

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DeveloperBoard
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
fun OppoSectionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OPPO 板块") },
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
                    title = "OPPO / OnePlus / realme",
                    subtitle = "ColorOS / OxygenOS / realme UI 专属功能"
                )
            }

            item { SectionTitle("OPPO 系统工具") }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.Security,
                    title = "手机管家",
                    description = "权限管理、支付保护、骚扰拦截",
                    onClick = {
                        val ok = BrandUtils.oppoPhoneManager(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "电池管理",
                    description = "ColorOS 电池与省电设置",
                    onClick = {
                        val ok = BrandUtils.oppoBatterySettings(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.VideogameAsset,
                    title = "游戏空间",
                    description = "OPPO 游戏空间、游戏助手",
                    onClick = {
                        val ok = BrandUtils.oppoGameSpace(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.DeveloperBoard,
                    title = "开发者选项",
                    description = "ColorOS 开发者选项入口",
                    onClick = { BrandUtils.oppoDeveloperOptions(context) }
                )
            }
        }
    }
}
