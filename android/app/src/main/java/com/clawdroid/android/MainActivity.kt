package com.clawdroid.android

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.clawdroid.android.databinding.ActivityMainBinding
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalViewClient

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    lateinit var sessionManager: TerminalSessionManager
    lateinit var bootstrapManager: BootstrapManager
    lateinit var eventBridge: EventBridge
    private lateinit var agentBridge: AgentBridge

    private val terminalSessionClient = ClawdroidSessionClient()
    private val terminalViewClient = ClawdroidViewClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bootstrapManager = BootstrapManager(this)
        eventBridge = EventBridge(binding.webView)
        sessionManager = TerminalSessionManager(this, terminalSessionClient, eventBridge)
        agentBridge = AgentBridge(this, sessionManager, bootstrapManager, eventBridge)

        setupTerminalView()
        setupWebView()
        setupExtraKeys()
        sessionManager.onSessionsChanged = { updateSessionTabs() }
        startService(Intent(this, ClawdroidService::class.java))

        val isInstalled = bootstrapManager.isInstalled()
        AppLogger.i(TAG, "Bootstrap installed: $isInstalled, GPU: ${GpuDetector.getAccelerationMethod()}")

        if (isInstalled) {
            showTerminal()
            val session = sessionManager.createSession()
            if (bootstrapManager.needsPostSetup()) {
                val script = bootstrapManager.postSetupScript.absolutePath
                binding.terminalView.post { session.write("bash $script\n") }
            }
        }
    }

    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            setWebContentsDebuggingEnabled(true)
        }
        binding.webView.webViewClient = WebViewClient()
        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.addJavascriptInterface(JsBridge(this, sessionManager, bootstrapManager, eventBridge), "ClawDroid")
        binding.webView.addJavascriptInterface(agentBridge, "AgentBridge")
        binding.webView.loadUrl("file:///android_asset/www/index.html")
    }

    private fun setupTerminalView() {
        binding.terminalView.setTerminalViewClient(terminalViewClient)
    }

    private fun setupExtraKeys() {
        binding.btnEsc.setOnClickListener { activeSession?.write("\u001b") }
        binding.btnCtrl.setOnClickListener { }
        binding.btnAlt.setOnClickListener { }
        binding.btnTab.setOnClickListener { activeSession?.write("\t") }
        binding.btnHome.setOnClickListener { activeSession?.write("\u001b[H") }
        binding.btnEnd.setOnClickListener { activeSession?.write("\u001b[F") }
        binding.btnLeft.setOnClickListener { activeSession?.write("\u001b[D") }
        binding.btnUp.setOnClickListener { activeSession?.write("\u001b[A") }
        binding.btnDown.setOnClickListener { activeSession?.write("\u001b[B") }
        binding.btnRight.setOnClickListener { activeSession?.write("\u001b[C") }
        binding.btnDash.setOnClickListener { activeSession?.write("-") }
        binding.btnPipe.setOnClickListener { activeSession?.write("|") }
        binding.btnPaste.setOnClickListener { pasteFromClipboard() }
    }

    private fun updateSessionTabs() {
        binding.tabsLayout.removeAllViews()
        val sessions = sessionManager.getSessionsInfo()
        sessions.forEach { info ->
            val tab = android.widget.TextView(this).apply {
                text = info["name"] as String
                textSize = 12f
                setPadding(24, 8, 24, 8)
                setTextColor(getColor(R.color.extraKeyText))
                setBackgroundColor(getColor(R.color.tabBarBackground))
                setOnClickListener { sessionManager.switchSession(info["id"] as String) }
            }
            binding.tabsLayout.addView(tab)
        }
    }

    fun showTerminal() {
        binding.webView.visibility = View.GONE
        binding.terminalContainer.visibility = View.VISIBLE
    }

    fun showWebView() {
        binding.terminalContainer.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
    }

    private val activeSession: TerminalSession?
        get() = sessionManager.activeSession

    private fun pasteFromClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text.toString()
            activeSession?.write(text)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private inner class ClawdroidViewClient : TerminalViewClient {
        override fun onScale(scale: Float): Float = scale
        override fun onSingleTapUp(e: MotionEvent) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
        }
        override fun shouldBackButtonBeMappedToEscape(): Boolean = false
        override fun shouldEnforceCharBasedInput(): Boolean = true
        override fun getInputMode(): Int = 1
        override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
        override fun isTerminalViewSelected(): Boolean = binding.terminalContainer.visibility == View.VISIBLE
        override fun copyModeChanged(copyMode: Boolean) {}
        override fun onKeyDown(keyCode: Int, e: android.view.KeyEvent, session: TerminalSession): Boolean = false
        override fun onKeyUp(keyCode: Int, e: android.view.KeyEvent): Boolean = false
        override fun onLongPress(event: MotionEvent): Boolean = false
        override fun readControlKey(): Boolean = false
        override fun readAltKey(): Boolean = false
        override fun readShiftKey(): Boolean = false
        override fun readFnKey(): Boolean = false
        override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession): Boolean = false
        override fun onEmulatorSet() {}
        override fun logError(tag: String, message: String) { AppLogger.e(tag, message) }
        override fun logWarn(tag: String, message: String) { AppLogger.w(tag, message) }
        override fun logInfo(tag: String, message: String) { AppLogger.i(tag, message) }
        override fun logDebug(tag: String, message: String) { AppLogger.d(tag, message) }
        override fun logVerbose(tag: String, message: String) { AppLogger.d(tag, message) }
        override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) { AppLogger.e(tag, message, e) }
        override fun logStackTrace(tag: String, e: Exception) { AppLogger.e(tag, "Exception", e) }
    }

    private inner class ClawdroidSessionClient : TerminalSessionClient {
        override fun onTextChanged(session: TerminalSession) {
            binding.terminalView.onScreenUpdated()
        }
        override fun onTitleChanged(session: TerminalSession) {}
        override fun onBell(session: TerminalSession) {}
        override fun onColorsChanged(session: TerminalSession) {}
        override fun onTerminalCursorStateChange(state: Boolean) {}
        override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}
        override fun onSessionFinished(session: TerminalSession) {}
        override fun getTerminalCursorStyle(): Int = 0
        override fun logError(tag: String, message: String) { AppLogger.e(tag, message) }
        override fun logWarn(tag: String, message: String) { AppLogger.w(tag, message) }
        override fun logInfo(tag: String, message: String) { AppLogger.i(tag, message) }
        override fun logDebug(tag: String, message: String) { AppLogger.d(tag, message) }
        override fun logVerbose(tag: String, message: String) { AppLogger.d(tag, message) }
        override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) { AppLogger.e(tag, message, e) }
        override fun logStackTrace(tag: String, e: Exception) { AppLogger.e(tag, "Exception", e) }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, ClawdroidService::class.java))
    }
}
