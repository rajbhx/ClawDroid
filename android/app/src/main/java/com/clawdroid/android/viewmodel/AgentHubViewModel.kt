package com.clawdroid.android.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.clawdroid.android.EnvironmentBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AgentInfo(
    val id: String,
    val name: String,
    val description: String,
    val stars: String,
    val language: String,
    val isInstalled: Boolean = false,
    val command: String,
)

data class AgentHubUiState(
    val agents: List<AgentInfo> = emptyList(),
    val selectedAgent: AgentInfo? = null,
)

@HiltViewModel
class AgentHubViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentHubUiState())
    val uiState: StateFlow<AgentHubUiState> = _uiState.asStateFlow()

    init { refreshAgents() }

    fun refreshAgents() {
        val env = EnvironmentBuilder.build(context)
        val prefix = env["PREFIX"] ?: ""
        _uiState.value = AgentHubUiState(
            agents = listOf(
                AgentInfo("openclaw", "OpenClaw", "Personal AI assistant", "★386K", "TypeScript", checkInstalled(prefix, "openclaw"), "openclaw"),
                AgentInfo("claw-code", "Claw Code", "Multi-agent coding harness with RAG", "★48K", "Python+Rust", checkInstalled(prefix, "claw"), "claw"),
                AgentInfo("opencode", "OpenCode", "Open source coding agent", "★198K", "TypeScript", checkInstalled(prefix, "opencode"), "opencode"),
                AgentInfo("codex", "Codex CLI", "OpenAI terminal coding agent", "—", "Rust", checkInstalled(prefix, "codex"), "codex"),
                AgentInfo("mempalace", "MemPalace", "AI memory system", "★58K", "Python", false, "mempalace"),
                AgentInfo("omniroute", "OmniRoute", "340+ provider AI gateway", "★49K", "TypeScript", checkInstalled(prefix, "omniroute"), "omniroute"),
            ),
        )
    }

    fun selectAgent(agent: AgentInfo) { _uiState.value = _uiState.value.copy(selectedAgent = agent) }

    private fun checkInstalled(prefix: String, name: String): Boolean {
        return context.filesDir.resolve("usr/bin/$name").exists() ||
               context.filesDir.resolve("home/.openclaw-android/bin/$name").exists()
    }
}
