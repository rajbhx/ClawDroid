package com.clawdroid.android

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AgentBridge(
    private val activity: MainActivity,
    private val sessionManager: TerminalSessionManager,
    private val bootstrapManager: BootstrapManager,
    private val eventBridge: EventBridge,
) {
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "AgentBridge"
    }

    @JavascriptInterface
    fun getAgentStatus(): String {
        return gson.toJson(mapOf(
            "openclaw" to isAgentInstalled("openclaw"),
            "clawCode" to isAgentInstalled("claw"),
            "clawAnalog" to isAgentInstalled("claw-analog"),
            "opencode" to isAgentInstalled("opencode"),
            "codex" to isAgentInstalled("codex"),
        ))
    }

    @JavascriptInterface
    fun runAgent(agent: String, prompt: String) {
        scope.launch {
            try {
                eventBridge.emit("agent_start", mapOf("agent" to agent, "prompt" to prompt))
                val session = sessionManager.activeSession ?: sessionManager.createSession()
                val command = buildCommand(agent, prompt)
                session.write("$command\n")
                eventBridge.emit("agent_sent", mapOf("agent" to agent))
            } catch (e: Exception) {
                AppLogger.e(TAG, "Agent run failed", e)
                eventBridge.emit("agent_error", mapOf("agent" to agent, "error" to e.message))
            }
        }
    }

    @JavascriptInterface
    fun installAgent(agent: String) {
        scope.launch {
            try {
                eventBridge.emit("agent_install_start", mapOf("agent" to agent))
                val session = sessionManager.activeSession ?: sessionManager.createSession()
                val installCmd = getInstallCommand(agent)
                session.write("$installCmd\n")
                eventBridge.emit("agent_install_sent", mapOf("agent" to agent))
            } catch (e: Exception) {
                AppLogger.e(TAG, "Agent install failed", e)
                eventBridge.emit("agent_error", mapOf("agent" to agent, "error" to e.message))
            }
        }
    }

    @JavascriptInterface
    fun runTool(agent: String, tool: String, args: String) {
        scope.launch {
            try {
                eventBridge.emit("tool_start", mapOf("agent" to agent, "tool" to tool, "args" to args))
                val session = sessionManager.activeSession ?: return@launch
                val command = buildToolCommand(agent, tool, args)
                session.write("$command\n")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Tool run failed", e)
                eventBridge.emit("tool_error", mapOf("agent" to agent, "tool" to tool, "error" to e.message))
            }
        }
    }

    @JavascriptInterface
    fun searchMemory(query: String) {
        scope.launch {
            try {
                eventBridge.emit("memory_search_start", mapOf("query" to query))
                val session = sessionManager.activeSession ?: return@launch
                session.write("claw-rag-service query '$query'\n")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Memory search failed", e)
            }
        }
    }

    @JavascriptInterface
    fun getProviders(): String {
        return gson.toJson(listOf(
            mapOf("id" to "openai", "name" to "OpenAI", "models" to listOf("gpt-4o", "gpt-4o-mini", "o1")),
            mapOf("id" to "anthropic", "name" to "Anthropic", "models" to listOf("claude-sonnet-4-6", "claude-haiku-4-5")),
            mapOf("id" to "google", "name" to "Google", "models" to listOf("gemini-2.0-flash", "gemini-2.5-pro")),
            mapOf("id" to "deepseek", "name" to "DeepSeek", "models" to listOf("deepseek-chat", "deepseek-coder")),
        ))
    }

    private fun isAgentInstalled(agent: String): Boolean {
        val env = EnvironmentBuilder.build(activity)
        val prefix = env["PREFIX"] ?: return false
        return when (agent) {
            "openclaw" -> java.io.File("$prefix/bin/openclaw").exists()
            "claw", "claw-analog" -> java.io.File("$prefix/bin/claw").exists()
            "opencode" -> java.io.File("$prefix/bin/opencode").exists()
            "codex" -> java.io.File("$prefix/bin/codex").exists()
            else -> false
        }
    }

    private fun buildCommand(agent: String, prompt: String): String {
        val env = EnvironmentBuilder.build(activity)
        val homeDir = env["HOME"] ?: ""
        return when (agent) {
            "openclaw" -> "cd $homeDir && openclaw chat '$prompt'"
            "claw" -> "cd $homeDir && claw prompt '$prompt'"
            "claw-analog" -> "cd $homeDir && claw-analog prompt '$prompt'"
            "opencode" -> "cd $homeDir && opencode run '$prompt'"
            "codex" -> "cd $homeDir && codex exec '$prompt'"
            else -> "echo 'Unknown agent: $agent'"
        }
    }

    private fun buildToolCommand(agent: String, tool: String, args: String): String {
        val env = EnvironmentBuilder.build(activity)
        val homeDir = env["HOME"] ?: ""
        return when (agent) {
            "openclaw" -> "cd $homeDir && openclaw tool $tool $args"
            "claw" -> "cd $homeDir && claw tool $tool $args"
            "opencode" -> "cd $homeDir && opencode tool $tool $args"
            else -> "echo 'Unknown agent: $agent'"
        }
    }

    private fun getInstallCommand(agent: String): String {
        return when (agent) {
            "openclaw" -> "npm install -g openclaw 2>&1"
            "claw" -> "pip install claw-code 2>&1"
            "opencode" -> "npm install -g opencode-ai 2>&1"
            "codex" -> "npm install -g @openai/codex 2>&1"
            else -> "echo 'Unknown agent: $agent'"
        }
    }
}
