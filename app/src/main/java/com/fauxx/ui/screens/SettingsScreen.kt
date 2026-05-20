package com.fauxx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fauxx.BuildConfig
import com.fauxx.R
import com.fauxx.data.model.IntensityLevel
import com.fauxx.locale.SupportedLocale
import com.fauxx.ui.theme.ThemeMode
import com.fauxx.ui.viewmodels.SettingsViewModel

/**
 * Global settings screen: intensity, wifi-only, battery threshold, active hours, clear data.
 */
@Composable
fun SettingsScreen(
    onNavigateToAbout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val languageState by viewModel.languageState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showIntensityMenu by remember { mutableStateOf(false) }
    var showLogExportSheet by remember { mutableStateOf(false) }
    var exportedLogs by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Intensity
        SettingsCard {
            Text("强度", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IntensityLevel.values().forEach { level ->
                    Button(
                        onClick = { viewModel.setIntensity(level) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.intensity == level)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = level.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.intensity == level)
                                MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Text(
                text = "${uiState.intensity.actionsPerHour} 次/小时",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // App language
        SettingsCard {
            Text(
                stringResource(R.string.settings_language_title),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(8.dp))
            LanguagePickerOption(
                label = stringResource(R.string.settings_language_system_default),
                selected = languageState.userOverride == null,
                enabled = true,
                onClick = { viewModel.setLanguage(null) }
            )
            SupportedLocale.values().forEach { locale ->
                val shipped = locale in languageState.shippedLocales
                LanguagePickerOption(
                    label = locale.displayName,
                    selected = languageState.userOverride == locale,
                    enabled = shipped,
                    suffix = if (!shipped) stringResource(R.string.settings_language_coming_soon) else null,
                    onClick = { viewModel.setLanguage(locale) }
                )
            }
            Text(
                text = if (languageState.userOverride == null) {
                    stringResource(R.string.settings_language_subtitle_system)
                } else {
                    stringResource(R.string.settings_language_subtitle_explicit)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Theme
        SettingsCard {
            Text("主题", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.values().forEach { mode ->
                    Button(
                        onClick = { viewModel.setThemeMode(mode) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.themeMode == mode)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = mode.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.themeMode == mode)
                                MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Text(
                text = when (uiState.themeMode) {
                    ThemeMode.SYSTEM -> "跟随设备主题"
                    ThemeMode.LIGHT -> "始终浅色"
                    ThemeMode.DARK -> "始终深色"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Wi-Fi only toggle
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("仅 Wi-Fi", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "使用移动数据时暂停",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.wifiOnly,
                    onCheckedChange = { viewModel.setWifiOnly(it) }
                )
            }
        }

        // Resume after reboot
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("重启后恢复", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "重启后显示通知以恢复保护。Android 不允许应用在后台自行启动，因此需要点击操作。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = uiState.resumeOnBoot,
                    onCheckedChange = { viewModel.setResumeOnBoot(it) }
                )
            }
        }

        // Battery threshold
        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("电量低于以下值时暂停", style = MaterialTheme.typography.titleSmall)
                Text(
                    "${uiState.batteryThreshold}%",
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Slider(
                value = uiState.batteryThreshold.toFloat(),
                onValueChange = { viewModel.setBatteryThreshold(it.toInt()) },
                valueRange = 10f..50f,
                steps = 7
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "充电时忽略电量阈值",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "设备连接电源时，即使低于阈值也继续运行。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = uiState.ignoreBatteryThresholdWhileCharging,
                    onCheckedChange = { viewModel.setIgnoreBatteryThresholdWhileCharging(it) }
                )
            }
        }

        // Active hours
        SettingsCard {
            Text("活跃时段", style = MaterialTheme.typography.titleSmall)
            Text(
                "${uiState.allowedHoursStart}:00 – ${uiState.allowedHoursEnd}:00",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "开始",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = uiState.allowedHoursStart.toFloat(),
                onValueChange = { viewModel.setAllowedHoursStart(it.toInt()) },
                valueRange = 0f..23f,
                steps = 22
            )
            Text(
                "结束",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = uiState.allowedHoursEnd.toFloat(),
                onValueChange = { viewModel.setAllowedHoursEnd(it.toInt()) },
                valueRange = 0f..23f,
                steps = 22
            )
            Text(
                "在此时段之外活动将暂停",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val windowHours = (uiState.allowedHoursEnd - uiState.allowedHoursStart).let {
                if (it < 0) it + 24 else it
            }
            if (windowHours in 1..8) {
                Text(
                    "过窄的活动窗口（${windowHours}小时）本身可能成为可追踪的信号。较宽的时间窗（12小时以上）使追踪器更难以区分真实使用情况。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Custom User-Agent (issue #7)
        SettingsCard {
            Text(
                "匹配我的浏览器",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                "默认情况下，Fauxx 在多种浏览器标识之间轮换——但真实用户通常只使用一个浏览器，因此多样性本身可能看起来像机器人流量。点击下方使用此设备的内置浏览器标识，使噪声与您的真实活动融合。留空则保持默认轮换。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            val ctx = LocalContext.current
            OutlinedButton(
                onClick = {
                    val deviceUa = runCatching {
                        // System WebView UA — what most Chromium-based browsers
                        // (Chrome, Edge, Brave, etc.) and any in-app browser send.
                        // Close enough for "match my browser" without asking the user
                        // to know what a User-Agent string is.
                        android.webkit.WebSettings.getDefaultUserAgent(ctx)
                    }.getOrNull()
                    if (!deviceUa.isNullOrBlank()) viewModel.setCustomUserAgent(deviceUa)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("使用此设备的浏览器标识") }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.customUserAgent,
                onValueChange = { viewModel.setCustomUserAgent(it) },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        "浏览器标识（高级 — 留空以自动轮换）",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                placeholder = {
                    Text(
                        "Mozilla/5.0 (...)",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                singleLine = true
            )
            if (uiState.customUserAgent.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = { viewModel.setCustomUserAgent("") }
                ) { Text("清除并恢复轮换") }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Clear all data
        Button(
            onClick = { showClearDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("清除所有数据")
        }

        // Export debug logs
        Button(
            onClick = {
                val logs = viewModel.getScrubbedLogs()
                if (logs.isNotBlank()) {
                    exportedLogs = logs
                    showLogExportSheet = true
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("导出调试日志", color = MaterialTheme.colorScheme.onSurface)
        }

        // About & Privacy
        Button(
            onClick = onNavigateToAbout,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("关于与隐私政策", color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(Modifier.height(16.dp))

        // Version info
        Text(
            text = "Fauxx v${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("确认清除所有数据？") },
            text = {
                Text(
                    "这将永久删除：\n" +
                    "\u2022 所有操作日志\n" +
                    "\u2022 您的人口统计画像\n" +
                    "\u2022 广告平台画像缓存\n" +
                    "\u2022 人格生成历史\n\n" +
                    "所有设置将恢复为默认值。" +
                    "引擎将停止并回到第 0 层（均匀噪声）。\n\n" +
                    "此操作不可撤销。"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetToDefaults()
                    showClearDialog = false
                }) { Text("全部清除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }

    if (showLogExportSheet) {
        LogExportSheet(
            title = "导出调试日志",
            content = exportedLogs,
            fileName = "fauxx_debug_logs.txt",
            onDismiss = { showLogExportSheet = false }
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun LanguagePickerOption(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    suffix: String? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    selected -> MaterialTheme.colorScheme.onPrimary
                    !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (suffix != null) {
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
