package com.clawdroid.android

import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient

class TerminalSessionManager(
    private val activity: MainActivity,
    private val sessionClient: TerminalSessionClient,
    private val eventBridge: EventBridge,
) {
    private val sessions = mutableListOf<TerminalSession>()
    private var activeSessionIndex = -1
    private val finishedSessionIds = mutableSetOf<String>()
    var onSessionsChanged: (() -> Unit)? = null

    val activeSession: TerminalSession?
        get() = sessions.getOrNull(activeSessionIndex)

    fun createSession(): TerminalSession {
        val env = EnvironmentBuilder.build(activity)
        val prefix = env["PREFIX"] ?: ""
        val homeDir = env["HOME"] ?: activity.filesDir.absolutePath
        val tmpDir = env["TMPDIR"]
        java.io.File(homeDir).mkdirs()
        tmpDir?.let { java.io.File(it).mkdirs() }

        val shell = when {
            java.io.File("$prefix/bin/bash").exists() -> "$prefix/bin/bash"
            java.io.File("$prefix/bin/sh").exists() -> "$prefix/bin/sh"
            else -> "/system/bin/sh"
        }

        val session = TerminalSession(
            shell, homeDir, arrayOf(), env.entries.map { "${it.key}=${it.value}" }.toTypedArray(),
            2000, sessionClient,
        )
        sessions.add(session)
        switchSession(sessions.size - 1)
        eventBridge.emit("session_changed", mapOf("id" to session.mHandle, "action" to "created"))
        activity.runOnUiThread { onSessionsChanged?.invoke() }
        return session
    }

    fun switchSession(index: Int) {
        if (index < 0 || index >= sessions.size) return
        activeSessionIndex = index
        val session = sessions[index]
        activity.runOnUiThread {
            val tv = activity.findViewById<com.termux.view.TerminalView>(com.clawdroid.android.R.id.terminalView)
            tv?.attachSession(session)
            tv?.invalidate()
        }
        eventBridge.emit("session_changed", mapOf("id" to session.mHandle, "action" to "switched"))
        activity.runOnUiThread { onSessionsChanged?.invoke() }
    }

    fun switchSessionById(handleId: String) {
        val index = sessions.indexOfFirst { it.mHandle == handleId }
        if (index >= 0) switchSession(index)
    }

    fun closeSession(handleId: String) {
        val index = sessions.indexOfFirst { it.mHandle == handleId }
        if (index < 0) return
        finishedSessionIds.remove(handleId)
        val session = sessions.removeAt(index)
        session.finishIfRunning()
        eventBridge.emit("session_changed", mapOf("id" to handleId, "action" to "closed"))
        if (sessions.isNotEmpty()) switchSession(index.coerceAtMost(sessions.size - 1))
        else activeSessionIndex = -1
        activity.runOnUiThread { onSessionsChanged?.invoke() }
    }

    fun getSessionsInfo(): List<Map<String, Any>> =
        sessions.mapIndexed { index, session ->
            mapOf("id" to session.mHandle, "name" to (session.title ?: "Session ${index + 1}"),
                "active" to (index == activeSessionIndex), "finished" to (session.mHandle in finishedSessionIds))
        }

    val sessionCount: Int get() = sessions.size
}
