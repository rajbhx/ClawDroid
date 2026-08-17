package com.clawdroid.android.ui.screens

import android.widget.LinearLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.clawdroid.android.EnvironmentBuilder
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

@Composable
fun TerminalScreen() {
    val context = LocalContext.current
    val env = remember { EnvironmentBuilder.build(context) }
    var sessionRef by remember { mutableStateOf<TerminalSession?>(null) }
    var inputText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize()) {
        val tv = remember {
            val prefix = env["PREFIX"] ?: ""
            val homeDir = env["HOME"] ?: context.filesDir.absolutePath
            val tmpDir = env["TMPDIR"] ?: "${context.filesDir.absolutePath}/tmp"
            java.io.File(homeDir).mkdirs()
            java.io.File(tmpDir).mkdirs()

            val shell = when {
                java.io.File("$prefix/bin/bash").exists() -> "$prefix/bin/bash"
                java.io.File("$prefix/bin/sh").exists() -> "$prefix/bin/sh"
                else -> "/system/bin/sh"
            }

            val sessionClient = object : TerminalSessionClient {
                override fun onTextChanged(session: TerminalSession) {}
                override fun onTitleChanged(session: TerminalSession) {}
                override fun onBell(session: TerminalSession) {}
                override fun onColorsChanged(session: TerminalSession) {}
                override fun onTerminalCursorStateChange(state: Boolean) {}
                override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}
                override fun onSessionFinished(session: TerminalSession) {}
                override fun onCopyTextToClipboard(session: TerminalSession, text: String) {}
                override fun onPasteTextFromClipboard(session: TerminalSession?) {}
                override fun getTerminalCursorStyle(): Int = 0
                override fun logError(tag: String, message: String) {}
                override fun logWarn(tag: String, message: String) {}
                override fun logInfo(tag: String, message: String) {}
                override fun logDebug(tag: String, message: String) {}
                override fun logVerbose(tag: String, message: String) {}
                override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) {}
                override fun logStackTrace(tag: String, e: Exception) {}
            }

            val session = TerminalSession(
                shell, homeDir, arrayOf(),
                env.entries.map { "${it.key}=${it.value}" }.toTypedArray(),
                2000, sessionClient,
            )
            sessionRef = session

            TerminalView(context, null).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                )
                setTerminalViewClient(object : TerminalViewClient {
                    override fun onScale(scale: Float): Float = scale
                    override fun onSingleTapUp(e: android.view.MotionEvent) {}
                    override fun shouldBackButtonBeMappedToEscape(): Boolean = false
                    override fun shouldEnforceCharBasedInput(): Boolean = true
                    override fun getInputMode(): Int = 1
                    override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
                    override fun isTerminalViewSelected(): Boolean = true
                    override fun copyModeChanged(copyMode: Boolean) {}
                    override fun onKeyDown(keyCode: Int, e: android.view.KeyEvent, session: TerminalSession): Boolean = false
                    override fun onKeyUp(keyCode: Int, e: android.view.KeyEvent): Boolean = false
                    override fun onLongPress(event: android.view.MotionEvent): Boolean = false
                    override fun readControlKey(): Boolean = false
                    override fun readAltKey(): Boolean = false
                    override fun readShiftKey(): Boolean = false
                    override fun readFnKey(): Boolean = false
                    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession): Boolean = false
                    override fun onEmulatorSet() {}
                    override fun logError(tag: String, message: String) {}
                    override fun logWarn(tag: String, message: String) {}
                    override fun logInfo(tag: String, message: String) {}
                    override fun logDebug(tag: String, message: String) {}
                    override fun logVerbose(tag: String, message: String) {}
                    override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) {}
                    override fun logStackTrace(tag: String, e: Exception) {}
                })
                attachSession(session)
            }
        }

        AndroidView(
            factory = { tv },
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            placeholder = { Text("Type command...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (inputText.isNotBlank()) {
                    sessionRef?.write(inputText + "\n")
                    inputText = ""
                    keyboardController?.hide()
                }
            }),
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            try { sessionRef?.finishIfRunning() } catch (_: Exception) {}
            sessionRef = null
        }
    }
}
