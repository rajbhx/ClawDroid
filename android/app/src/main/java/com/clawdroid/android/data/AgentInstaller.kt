package com.clawdroid.android.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentInstaller @Inject constructor(
    private val terminalChatBridge: TerminalChatBridge,
) {
    data class InstallStep(val description: String, val command: String)

    private val installScripts = mapOf(
        "openclaw" to listOf(
            InstallStep("Install Node.js", "pkg install -y nodejs"),
            InstallStep("Install OpenClaw", "npm install -g openclaw"),
        ),
        "claw-code" to listOf(
            InstallStep("Install Python", "pkg install -y python"),
            InstallStep("Install Pip", "pkg install -y python-pip"),
            InstallStep("Install Claw Code", "pip install claw-code"),
        ),
        "opencode" to listOf(
            InstallStep("Install Node.js", "pkg install -y nodejs"),
            InstallStep("Install OpenCode", "npm install -g @anthropic/opencode"),
        ),
        "codex" to listOf(
            InstallStep("Install Rust", "curl --proto =https --tlsv1.2 -sSf https://sh.rustup.rs | sh"),
            InstallStep("Install Codex CLI", "cargo install codex-cli"),
        ),
        "mempalace" to listOf(
            InstallStep("Install Python", "pkg install -y python"),
            InstallStep("Install MemPalace", "pip install mempalace"),
        ),
        "omniroute" to listOf(
            InstallStep("Install Node.js", "pkg install -y nodejs"),
            InstallStep("Install OmniRoute", "npm install -g omniroute"),
        ),
    )

    fun getInstallSteps(agentId: String): List<InstallStep> {
        return installScripts[agentId] ?: emptyList()
    }

    fun installAgent(agentId: String) {
        val steps = installScripts[agentId] ?: return
        val combined = steps.joinToString("\n") { it.command }
        terminalChatBridge.sendCommandToTerminal(combined)
    }

    fun runAgent(agentId: String, prompt: String) {
        terminalChatBridge.sendCommandToTerminal("$agentId $prompt")
    }
}
