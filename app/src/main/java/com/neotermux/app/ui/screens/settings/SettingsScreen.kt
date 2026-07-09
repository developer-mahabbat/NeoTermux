package com.neotermux.app.ui.screens.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    "Appearance",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            item {
                SettingsCard {
                    SettingsSwitchItem(
                        icon = Icons.Outlined.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Use dark theme",
                        checked = settings.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Palette,
                        title = "Dynamic Colors",
                        subtitle = "Use Material You colors",
                        checked = settings.useDynamicColors,
                        onCheckedChange = { viewModel.toggleDynamicColors() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsSliderItem(
                        icon = Icons.Outlined.TextFields,
                        title = "Font Size",
                        subtitle = "${settings.fontSize.toInt()}sp",
                        value = settings.fontSize,
                        onValueChange = { viewModel.setFontSize(it) },
                        valueRange = 8f..72f
                    )
                }
            }

            item {
                Text(
                    "Terminal",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }
            item {
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.Terminal,
                        title = "Default Shell",
                        subtitle = settings.defaultShell.split("/").lastOrNull() ?: "bash",
                        onClick = {}
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsSliderItem(
                        icon = Icons.Outlined.History,
                        title = "Scrollback Lines",
                        subtitle = "${settings.scrollbackLines}",
                        value = settings.scrollbackLines.toFloat(),
                        onValueChange = { viewModel.setScrollbackLines(it.toInt()) },
                        valueRange = 100f..50000f,
                        steps = 49
                    )
                }
            }

            item {
                Text(
                    "Keyboard",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }
            item {
                SettingsCard {
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Keyboard,
                        title = "Extra Keys Row",
                        subtitle = "Show extra key row",
                        checked = settings.showExtraKeys,
                        onCheckedChange = { viewModel.toggleExtraKeys() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Vibration,
                        title = "Key Vibration",
                        subtitle = "Haptic feedback on key press",
                        checked = settings.keyVibration,
                        onCheckedChange = { viewModel.toggleKeyVibration() }
                    )
                }
            }

            item {
                Text(
                    "Security",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }
            item {
                SettingsCard {
                    SettingsSwitchItem(
                        icon = Icons.Outlined.Lock,
                        title = "App Lock",
                        subtitle = "Require authentication",
                        checked = settings.appLock,
                        onCheckedChange = { viewModel.toggleAppLock() }
                    )
                }
            }

            item {
                Spacer(Modifier.height(32.dp))
                Text(
                    "NeoTermux v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, title, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, "More", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsSwitchItem(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, title, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsSliderItem(icon: ImageVector, title: String, subtitle: String, value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>, steps: Int = 0) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, title, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(start = 40.dp)
        )
    }
}
