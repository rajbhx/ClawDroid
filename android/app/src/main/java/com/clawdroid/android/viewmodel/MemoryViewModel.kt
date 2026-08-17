package com.clawdroid.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clawdroid.android.data.local.entity.MemoryEntry
import com.clawdroid.android.data.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemoryUiState(
    val memories: List<MemoryEntry> = emptyList(),
    val selectedCategory: String = "all",
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val editingMemory: MemoryEntry? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    private val _category = MutableStateFlow("all")

    init {
        viewModelScope.launch {
            _category.flatMapLatest { cat ->
                if (cat == "all") memoryRepository.getAll()
                else memoryRepository.getByCategory(cat)
            }.collect { memories ->
                _uiState.value = _uiState.value.copy(memories = memories)
            }
        }
    }

    fun selectCategory(category: String) {
        _category.value = category
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, isSearching = query.isNotBlank())
        viewModelScope.launch {
            if (query.isBlank()) {
                _category.value = _category.value
            } else {
                memoryRepository.search(query).collect { results ->
                    _uiState.value = _uiState.value.copy(memories = results)
                }
            }
        }
    }

    fun addMemory(content: String, category: String, tags: String) {
        viewModelScope.launch {
            memoryRepository.insert(MemoryEntry(content = content, category = category, tags = tags))
        }
    }

    fun deleteMemory(memory: MemoryEntry) {
        viewModelScope.launch { memoryRepository.delete(memory) }
    }

    fun updateMemory(memory: MemoryEntry) {
        viewModelScope.launch { memoryRepository.update(memory) }
    }
}
