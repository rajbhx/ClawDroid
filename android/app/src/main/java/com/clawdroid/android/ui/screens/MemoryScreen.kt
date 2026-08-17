package com.clawdroid.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clawdroid.android.data.local.entity.MemoryEntry
import com.clawdroid.android.viewmodel.MemoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val categories = listOf("all", "general", "wings", "rooms", "drawers", "code", "notes")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(viewModel: MemoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newContent by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("general") }
    var newTags by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Memory", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add memory")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.search(it)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search memories...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
            )

            // Category chips
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = state.selectedCategory == cat,
                        onClick = { viewModel.selectCategory(cat) },
                        label = { Text(cat.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Memory list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            ) {
                items(state.memories) { memory ->
                    MemoryCard(memory = memory, onDelete = { viewModel.deleteMemory(it) })
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Memory") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Memory content...") },
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Category (general, code, notes...)") },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTags,
                        onValueChange = { newTags = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tags (comma-separated)") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newContent.isNotBlank()) {
                        viewModel.addMemory(newContent, newCategory, newTags)
                        newContent = ""
                        newCategory = "general"
                        newTags = ""
                        showAddDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
fun MemoryCard(memory: MemoryEntry, onDelete: (MemoryEntry) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(memory.content, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${memory.category} • ${dateFormat.format(Date(memory.createdAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                IconButton(onClick = { onDelete(memory) }) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            if (memory.tags.isNotBlank()) {
                Text(
                    "Tags: ${memory.tags}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
