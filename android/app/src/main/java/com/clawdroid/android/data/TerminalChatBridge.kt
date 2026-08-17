package com.clawdroid.android.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TerminalChatBridge @Inject constructor() {
    private val _terminalOutput = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val terminalOutput: SharedFlow<String> = _terminalOutput.asSharedFlow()

    private val _chatCommand = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val chatCommand: SharedFlow<String> = _chatCommand.asSharedFlow()

    fun emitTerminalOutput(text: String) {
        _terminalOutput.tryEmit(text)
    }

    fun sendCommandToTerminal(command: String) {
        _chatCommand.tryEmit(command)
    }

    private val _selectedTerminalText = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val selectedTerminalText: SharedFlow<String> = _selectedTerminalText.asSharedFlow()

    fun selectTerminalText(text: String) {
        _selectedTerminalText.tryEmit(text)
    }
}
