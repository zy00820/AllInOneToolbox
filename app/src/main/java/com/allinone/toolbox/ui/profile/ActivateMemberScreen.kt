package com.allinone.toolbox.ui.profile

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allinone.toolbox.utils.ActivationUtils
import com.allinone.toolbox.utils.ActivationUtils.ActivationResult
import com.allinone.toolbox.utils.ActivationUtils.MemberLevel
import com.allinone.toolbox.utils.DeviceUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivateMemberScreen(
    onBack: () -> Unit,
    onActivated: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var level by remember { mutableStateOf(ActivationUtils.memberLevel()) }
    var liteInput by remember { mutableStateOf("") }
    var proInput  by remember { mutableStateOf("") }
    var resultCard: Pair<Boolean, String>? by remember { mutableStateOf(null) } // true=ok

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("激活会员") },
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
            // 会员状态卡
            MemberStatusCard(level = level)

            // 设备代码
            DeviceCodeCard()

            // 第一层：LITE 激活
            ActivationStepCard(
                stepNum = 1,
                title = "第一码 · 激活 LITE",
                description = "使用 50 个 A 类激活码（AT2024-Axxx）",
                input = liteInput,
                onInputChange = { liteInput = it },
                activated = level.level >= MemberLevel.LITE.level,
                activatedLabel = "LITE 已激活",
                buttonLabel = "验证 LITE 码",
                buttonEnabled = level.level < MemberLevel.LITE.level,
                onVerify = {
                    if (liteInput.isBlank()) {
                        Toast.makeText(context, "请输入第一码", Toast.LENGTH_SHORT).show()
                        return@ActivationStepCard
                    }
                    when (val r = ActivationUtils.verifyAndActivateLite(liteInput)) {
                        is ActivationResult.LiteSuccess -> {
                            level = ActivationUtils.memberLevel()
                            resultCard = true to r.message
                            Toast.makeText(context, r.message, Toast.LENGTH_SHORT).show()
                            onActivated?.invoke()
                        }
                        is ActivationResult.ProSuccess -> {
                            level = ActivationUtils.memberLevel()
                            resultCard = true to r.message
                        }
                        is ActivationResult.Failed -> {
                            resultCard = false to r.message
                            Toast.makeText(context, r.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            // 第二层：PRO 激活
            ActivationStepCard(
                stepNum = 2,
                title = "第二码 · 升级 PRO",
                description = "使用 50 个 P 类激活码（AT2024-Pxxx），必须先激活 LITE",
                input = proInput,
                onInputChange = { proInput = it },
                activated = level.level >= MemberLevel.PRO.level,
                activatedLabel = "PRO 已激活",
                buttonLabel = "验证 PRO 码",
                buttonEnabled = level.level == MemberLevel.LITE.level,
                disabledWhenLocked = level.level < MemberLevel.LITE.level,
                onVerify = {
                    if (proInput.isBlank()) {
                        Toast.makeText(context, "请输入第二码", Toast.LENGTH_SHORT).show()
                        return@ActivationStepCard
                    }
                    when (val r = ActivationUtils.verifyAndActivatePro(proInput)) {
                        is ActivationResult.ProSuccess -> {
                            level = ActivationUtils.memberLevel()
                            resultCard = true to r.message
                            Toast.makeText(context, r.message, Toast.LENGTH_SHORT).show()
                            onActivated?.invoke()
                        }
                        is ActivationResult.LiteSuccess -> {
                            level = ActivationUtils.memberLevel()
                            resultCard = true to r.message
                        }
                        is ActivationResult.Failed -> {
                            resultCard = false to r.message
                            Toast.makeText(context, r.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            // 结果提示卡
            resultCard?.let { (ok, msg) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ok) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(16.dp),
                        color = if (ok) MaterialTheme.colorScheme.onTertiaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // 会员权益说明
            BenefitsCard(level = level)
        }
    }
}

@Composable
private fun MemberStatusCard(level: MemberLevel) {
    val (container, onContainer) = when (level) {
        MemberLevel.PRO ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        MemberLevel.LITE ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        MemberLevel.NONE ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (level.level >= MemberLevel.LITE.level) Icons.Default.Verified else Icons.Default.Lock,
                contentDescription = level.displayName,
                tint = onContainer,
                modifier = Modifier.height(48.dp)
            )
            Text(
                text = level.displayName,
                style = MaterialTheme.typography.headlineSmall,
                color = onContainer,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = ActivationUtils.getActivationInfo(),
                style = MaterialTheme.typography.bodyMedium,
                color = onContainer.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceCodeCard() {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("设备代码", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = DeviceUtils.getDeviceCode(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val cb = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    cb.setPrimaryClip(android.content.ClipData.newPlainText("设备代码", DeviceUtils.getDeviceCode()))
                    Toast.makeText(context, "设备代码已复制", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivationStepCard(
    stepNum: Int,
    title: String,
    description: String,
    input: String,
    onInputChange: (String) -> Unit,
    activated: Boolean,
    activatedLabel: String,
    buttonLabel: String,
    buttonEnabled: Boolean,
    disabledWhenLocked: Boolean = false,
    onVerify: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (activated) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activated,
                    onClick = {},
                    label = { Text("第 $stepNum 层") }
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (activated) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (activated) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "✓ $activatedLabel",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    label = { Text("第 $stepNum 码") },
                    placeholder = {
                        Text(
                            text = if (stepNum == 1) "AT2024-Axxx-xxxx-xxxx"
                            else "AT2024-Pxxx-xxxx-xxxx",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = buttonEnabled || !disabledWhenLocked
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onVerify,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = buttonEnabled
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(buttonLabel)
                }
                if (disabledWhenLocked) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "请先完成第 1 层验证",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BenefitsCard(level: MemberLevel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("会员权益", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            val liteChecked = level.level >= MemberLevel.LITE.level
            val proChecked  = level.level >= MemberLevel.PRO.level
            BenefitRow(liteChecked, "APK 提取功能")
            BenefitRow(liteChecked, "应用信息查看")
            BenefitRow(liteChecked, "Shizuku 权限授权")
            BenefitRow(liteChecked, "电量修改工具")
            BenefitRow(liteChecked, "一键激活 Scene 模块")
            BenefitRow(proChecked, "小米极致模式（强开）")
            BenefitRow(proChecked, "后续新增 PRO 专属功能（优先）")
            BenefitRow(true, "永久有效 · 本地离线")
        }
    }
}

@Composable
private fun BenefitRow(checked: Boolean, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (checked) "✓" else "○",
            color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (checked) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
