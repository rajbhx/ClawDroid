package com.clawdroid.android.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clawdroid.android.GpuDetector
import com.clawdroid.android.data.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val gpuMethod: String = "Unknown",
    val gpuActive: Boolean = false,
    val bootstrapInstalled: Boolean = false,
    val agentsInstalled: Int = 0,
    val memoryCount: Int = 0,
    val recentActivity: List<String> = emptyList(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val memoryRepository: MemoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val method = GpuDetector.getAccelerationMethod()
        val bootstrapDir = context.filesDir.resolve("usr/bin/bash")
        val agentBinaries = listOf("openclaw", "claw", "opencode", "codex", "mempalace", "omniroute")
        val installed = agentBinaries.count { context.filesDir.resolve("usr/bin/$it").exists() ||
                context.filesDir.resolve("home/.openclaw-android/bin/$it").exists() }

        _uiState.value = DashboardUiState(
            gpuMethod = method,
            gpuActive = method != "Software",
            bootstrapInstalled = bootstrapDir.exists(),
            agentsInstalled = installed,
        )

        viewModelScope.launch {
            memoryRepository.getAll().collect { memories ->
                _uiState.value = _uiState.value.copy(memoryCount = memories.size)
            }
        }
    }
}
