package com.clawdroid.android

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JsBridge(
    private val activity: MainActivity,
    private val sessionManager: TerminalSessionManager,
    private val bootstrapManager: BootstrapManager,
    private val eventBridge: EventBridge,
) {
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    @JavascriptInterface
    fun showTerminal() {
        if (sessionManager.activeSession == null) {
            val session = sessionManager.createSession()
            if (bootstrapManager.needsPostSetup()) {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    session.write("bash ${bootstrapManager.postSetupScript.absolutePath}\n")
                }, 500L)
            }
        }
        activity.showTerminal()
    }

    @JavascriptInterface
    fun showWebView() = activity.showWebView()

    @JavascriptInterface
    fun createSession(): String {
        val session = sessionManager.createSession()
        return gson.toJson(mapOf("id" to session.mHandle, "name" to (session.title ?: "Terminal")))
    }

    @JavascriptInterface
    fun switchSession(id: String) = activity.runOnUiThread { sessionManager.switchSessionById(id) }

    @JavascriptInterface
    fun closeSession(id: String) { sessionManager.closeSession(id) }

    @JavascriptInterface
    fun getTerminalSessions(): String = gson.toJson(sessionManager.getSessionsInfo())

    @JavascriptInterface
    fun writeToTerminal(id: String, data: String) {
        val session = if (id.isBlank()) {
            sessionManager.activeSession
        } else {
            sessionManager.getSessionsInfo()
                .indexOfFirst { it["id"] == id }
                .takeIf { it >= 0 }
                ?.let { sessionManager.activeSession }
                ?: sessionManager.activeSession
        }
        session?.write(data)
    }

    @JavascriptInterface
    fun isBootstrapInstalled(): Boolean = bootstrapManager.isInstalled()

    @JavascriptInterface
    fun startBootstrap() {
        scope.launch {
            try {
                bootstrapManager.startSetup { progress, message ->
                    eventBridge.emit("bootstrap_progress", mapOf("progress" to progress, "message" to message))
                }
                eventBridge.emit("bootstrap_complete", mapOf("success" to true))
            } catch (e: Exception) {
                eventBridge.emit("bootstrap_error", mapOf("error" to e.message))
            }
        }
    }

    @JavascriptInterface
    fun getGpuInfo(): String = gson.toJson(mapOf("type" to GpuDetector.detect().name, "method" to GpuDetector.getAccelerationMethod()))

    @JavascriptInterface
    fun getAppInfo(): String {
        val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
        return gson.toJson(mapOf("versionName" to (pInfo.versionName ?: "unknown"), "versionCode" to pInfo.versionCode, "packageName" to activity.packageName))
    }

    @JavascriptInterface
    fun copyToClipboard(text: String) {
        activity.runOnUiThread {
            val clipboard = activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("ClawDroid", text))
        }
    }
}
