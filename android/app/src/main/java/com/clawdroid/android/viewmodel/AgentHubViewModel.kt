package com.clawdroid.android.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clawdroid.android.data.AgentInstaller
import com.clawdroid.android.data.TerminalChatBridge
import com.clawdroid.android.EnvironmentBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgentInfo(
    val id: String,
    val name: String,
    val description: String,
    val stars: String,
    val language: String,
    val isInstalled: Boolean = false,
    val command: String,
    val installSteps: List<String> = emptyList(),
)

data class AgentHubUiState(
    val agents: List<AgentInfo> = emptyList(),
    val selectedAgent: AgentInfo? = null,
    val isInstalling: Boolean = false,
    val installProgress: String = "",
    val isRunning: Boolean = false,
    val runOutput: String = "",
)

@HiltViewModel
class AgentHubViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val agentInstaller: AgentInstaller,
    private val terminalChatBridge: TerminalChatBridge,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentHubUiState())
    val uiState: StateFlow<AgentHubUiState> = _uiState.asStateFlow()

    init {
        refreshAgents()
        viewModelScope.launch {
            terminalChatBridge.terminalOutput.collect { output ->
                _uiState.value = _uiState.value.copy(
                    runOutput = _uiState.value.runOutput + output,
                )
            }
        }
    }

    fun refreshAgents() {
        val env = EnvironmentBuilder.build(context)
        val prefix = env["PREFIX"] ?: ""
        _uiState.value = AgentHubUiState(
            agents = listOf(
                AgentInfo("openclaw", "OpenClaw", "Personal AI assistant", "★386K", "TypeScript",
                    checkInstalled(prefix, "openclaw"), "openclaw",
                    agentInstaller.getInstallSteps("openclaw").map { it.description }),
                AgentInfo("claw-code", "Claw Code", "Multi-agent coding harness with RAG", "★48K", "Python+Rust",
                    checkInstalled(prefix, "claw"), "claw",
                    agentInstaller.getInstallSteps("claw-code").map { it.description }),
                AgentInfo("opencode", "OpenCode", "Open source coding agent", "★198K", "TypeScript",
                    checkInstalled(prefix, "opencode"), "opencode",
                    agentInstaller.getInstallSteps("opencode").map { it.description }),
                AgentInfo("codex", "Codex CLI", "OpenAI terminal coding agent", "—", "Rust",
                    checkInstalled(prefix, "codex"), "codex",
                    agentInstaller.getInstallSteps("codex").map { it.description }),
                AgentInfo("mempalace", "MemPalace", "AI memory system", "★58K", "Python",
                    false, "mempalace",
                    agentInstaller.getInstallSteps("mempalace").map { it.description }),
                AgentInfo("omniroute", "OmniRoute", "340+ provider AI gateway", "★49K", "TypeScript",
                    checkInstalled(prefix, "omniroute"), "omniroute",
                    agentInstaller.getInstallSteps("omniroute").map { it.description }),
            ),
        )
    }

    fun selectAgent(agent: AgentInfo) {
        _uiState.value = _uiState.value.copy(selectedAgent = agent, runOutput = "")
    }

    fun installAgent(agent: AgentInfo) {
        _uiState.value = _uiState.value.copy(isInstalling = true, installProgress = "Starting ${agent.name} install...")
        viewModelScope.launch {
            agentInstaller.installAgent(agent.id)
            _uiState.value = _uiState.value.copy(isInstalling = false, installProgress = "Install command sent to terminal")
            refreshAgents()
        }
    }

    fun runAgent(agent: AgentInfo, prompt: String) {
        _uiState.value = _uiState.value.copy(isRunning = true, runOutput = "")
        viewModelScope.launch {
            agentInstaller.runAgent(agent.id, prompt)
            _uiState.value = _uiState.value.copy(isRunning = false)
        }
    }

    private fun checkInstalled(prefix: String, name: String): Boolean {
        return context.filesDir.resolve("usr/bin/$name").exists() ||
               context.filesDir.resolve("home/.openclaw-android/bin/$name").exists()
    }
}
