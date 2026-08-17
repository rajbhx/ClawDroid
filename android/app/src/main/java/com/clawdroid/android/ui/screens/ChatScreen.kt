package com.clawdroid.android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clawdroid.android.data.local.entity.Message
import com.clawdroid.android.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    var showModelPicker by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.messages.size, state.streamingContent) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ClawDroid", fontWeight = FontWeight.Bold)
                        Text(
                            "${state.selectedProvider} / ${state.selectedModel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showHistory = true }) {
                        Icon(Icons.Default.History, "History")
                    }
                    IconButton(onClick = { showModelPicker = true }) {
                        Icon(Icons.Default.Language, "Model")
                    }
                    IconButton(onClick = { viewModel.newConversation() }) {
                        Icon(Icons.Default.Add, "New chat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {},
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Messages
            Box(modifier = Modifier.weight(1f)) {
                if (state.messages.isEmpty() && state.streamingContent.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("🦞", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("ClawDroid", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "AI assistant with 340+ providers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Powered by OmniRoute",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    ) {
                        items(state.messages, key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                onCopy = { clipboardManager.setText(AnnotatedString(message.content)) },
                            )
                        }
                        if (state.isStreaming && state.streamingContent.isNotEmpty()) {
                            item {
                                StreamingBubble(content = state.streamingContent)
                            }
                        }
                        if (state.isStreaming && state.streamingContent.isEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Thinking...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }

            // Error display
            AnimatedVisibility(visible = state.error != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = state.error ?: "",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .imePadding(),
                verticalAlignment = Alignment.Bottom,
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message...") },
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText.trim())
                            inputText = ""
                            keyboardController?.hide()
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    ),
                    shape = RoundedCornerShape(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText.trim())
                            inputText = ""
                            keyboardController?.hide()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Send")
                }
            }
        }
    }

    // History drawer
    if (showHistory) {
        AlertDialog(
            onDismissRequest = { showHistory = false },
            title = { Text("Conversations") },
            text = {
                LazyColumn {
                    items(state.conversations) { conv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectConversation(conv.id)
                                    showHistory = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(conv.title, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${conv.modelName} • ${conv.providerId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showHistory = false }) { Text("Close") } },
        )
    }

    // Model picker
    if (showModelPicker) {
        AlertDialog(
            onDismissRequest = { showModelPicker = false },
            title = { Text("Select Provider & Model") },
            text = {
                LazyColumn {
                    state.providers.forEach { provider ->
                        item {
                            Text(
                                provider.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            )
                        }
                        provider.models.forEach { model ->
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectProvider(provider.id)
                                            viewModel.selectModel(model)
                                            showModelPicker = false
                                        }
                                        .padding(vertical = 8.dp, horizontal = 12.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .then(
                                            if (provider.id == state.selectedProvider && model == state.selectedModel)
                                                Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                                            else Modifier
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(model, modifier = Modifier.weight(1f))
                                    if (provider.id == state.selectedProvider && model == state.selectedModel) {
                                        Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showModelPicker = false }) { Text("Cancel") } },
        )
    }
}

@Composable
fun MessageBubble(message: Message, onCopy: () -> Unit) {
    val isUser = message.role == "user"
    val clipboardManager = LocalClipboardManager.current
    var showActions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clickable { showActions = !showActions },
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp,
            ),
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Text(
                        "Assistant",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (showActions) {
                    Spacer(Modifier.height(4.dp))
                    Row {
                        IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.ContentCopy, "Copy", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StreamingBubble(content: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Assistant", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(content + "▌", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
