package com.allinone.toolbox.ui.brand

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.PhoneIphone
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
fun VivoSectionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("vivo 板块") },
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
                    title = "vivo / iQOO",
                    subtitle = "OriginOS / Funtouch OS 专属功能"
                )
            }

            item { SectionTitle("vivo 系统工具") }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.Security,
                    title = "i管家",
                    description = "病毒扫描、骚扰拦截、隐私保护",
                    onClick = {
                        val ok = BrandUtils.vivoManager(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.PhoneIphone,
                    title = "手机管家",
                    description = "后台启动管理、权限管理",
                    onClick = {
                        val ok = BrandUtils.vivoPhoneManager(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "电池管理",
                    description = "查看 vivo 电池管理与省电模式",
                    onClick = {
                        val ok = BrandUtils.vivoBatterySettings(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                BrandShortcutItem(
                    icon = Icons.Default.VideogameAsset,
                    title = "游戏魔盒",
                    description = "vivo 游戏加速、游戏助手",
                    onClick = {
                        val ok = BrandUtils.vivoGameBox(context)
                        if (!ok) Toast.makeText(context, "当前机型不支持该入口", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
