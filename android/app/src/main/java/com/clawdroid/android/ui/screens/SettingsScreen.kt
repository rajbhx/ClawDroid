package com.clawdroid.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clawdroid.android.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val themeMode by viewModel.themeMode.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val providerKeys by viewModel.providerKeys.collectAsState()
    val systemPrompt by viewModel.systemPrompt.collectAsState()
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPromptDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
        ) {
            item {
                SettingsSection("Appearance") {
                    SettingsItem(
                        icon = Icons.Default.BrightnessMedium,
                        title = "Theme",
                        subtitle = if (themeMode == 0) "Material You" else "Tokyo Night",
                        onClick = { showThemeDialog = true },
                    )
                }
            }
            item {
                SettingsSection("Hardware Acceleration") {
                    SettingsItem(
                        icon = Icons.Default.PhoneAndroid,
                        title = "GPU",
                        subtitle = gpuInfo.method,
                        trailing = {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (gpuInfo.isAvailable) MaterialTheme.colorScheme.tertiaryContainer
                                        else MaterialTheme.colorScheme.errorContainer,
                            ) {
                                Text(
                                    if (gpuInfo.isAvailable) "Active" else "Software",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        },
                        onClick = {},
                    )
                }
            }
            item {
                SettingsSection("Providers") {
                    providerKeys.forEach { key ->
                        SettingsItem(
                            icon = Icons.Default.Security,
                            title = key.name,
                            subtitle = "${key.selectedModel} • ${key.providerId}",
                            trailing = {
                                IconButton(onClick = { viewModel.deleteProviderKey(key) }) {
                                    Icon(Icons.Default.Delete, "Delete")
                                }
                            },
                            onClick = {},
                        )
                    }
                    if (providerKeys.isEmpty()) {
                        Text(
                            "Free models available: Groq, Gemini, Ollama (local)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
            item {
                SettingsSection("AI") {
                    SettingsItem(
                        icon = Icons.Default.Code,
                        title = "System Prompt",
                        subtitle = systemPrompt.take(50) + if (systemPrompt.length > 50) "..." else "",
                        onClick = { showPromptDialog = true },
                    )
                }
            }
            item {
                SettingsSection("About") {
                    SettingsItem(icon = Icons.Default.Code, title = "ClawDroid", subtitle = "v1.0.0 — Android AI Agent Platform", onClick = {})
                }
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    ThemeOption("Material You", "Dynamic color (Android 12+)", 0, themeMode) {
                        viewModel.setThemeMode(0)
                        showThemeDialog = false
                    }
                    ThemeOption("Tokyo Night", "Dark theme with blue accents", 1, themeMode) {
                        viewModel.setThemeMode(1)
                        showThemeDialog = false
                    }
                }
            },
            confirmButton = {},
        )
    }

    if (showPromptDialog) {
        var promptText by remember { mutableStateOf(systemPrompt) }
        AlertDialog(
            onDismissRequest = { showPromptDialog = false },
            title = { Text("System Prompt") },
            text = {
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setSystemPrompt(promptText); showPromptDialog = false }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showPromptDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))
        Card(modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
            Column { content() }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String,
    trailing: @Composable (() -> Unit)? = null, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        trailing?.invoke()
    }
}

@Composable
fun ThemeOption(title: String, subtitle: String, value: Int, current: Int, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        if (current == value) Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
    }
}
