package com.clawdroid.android

import org.junit.Assert.assertEquals
import org.junit.Test

class ChatViewModelTest {
    @Test
    fun defaultState() {
        val state = com.clawdroid.android.viewmodel.ChatUiState()
        assertEquals(null, state.currentConversationId)
        assertEquals("openai", state.selectedProvider)
        assertEquals("gpt-4o-mini", state.selectedModel)
        assertEquals(false, state.isStreaming)
    }
}
