package com.clawdroid.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clawdroid.android.data.local.entity.Conversation
import com.clawdroid.android.data.local.entity.Message
import com.clawdroid.android.data.network.ProviderConfig
import com.clawdroid.android.data.network.DEFAULT_PROVIDERS
import com.clawdroid.android.data.repository.ChatRepository
import com.clawdroid.android.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val currentConversationId: Long? = null,
    val messages: List<Message> = emptyList(),
    val isStreaming: Boolean = false,
    val streamingContent: String = "",
    val error: String? = null,
    val providers: List<ProviderConfig> = DEFAULT_PROVIDERS,
    val selectedProvider: String = "openai",
    val selectedModel: String = "gpt-4o-mini",
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            chatRepository.getAllConversations().collect { convs ->
                _uiState.value = _uiState.value.copy(conversations = convs)
            }
        }
        viewModelScope.launch {
            settingsRepository.activeProvider.collect { provider ->
                _uiState.value = _uiState.value.copy(selectedProvider = provider)
            }
        }
        viewModelScope.launch {
            settingsRepository.activeModel.collect { model ->
                _uiState.value = _uiState.value.copy(selectedModel = model)
            }
        }
    }

    fun selectConversation(id: Long) {
        _uiState.value = _uiState.value.copy(currentConversationId = id)
        viewModelScope.launch {
            chatRepository.getMessages(id).collect { msgs ->
                _uiState.value = _uiState.value.copy(messages = msgs)
            }
        }
    }

    fun sendMessage(content: String) {
        val state = _uiState.value
        if (content.isBlank() || state.isStreaming) return

        viewModelScope.launch {
            val convId = state.currentConversationId
                ?: chatRepository.createConversation(
                    title = content.take(50),
                    providerId = state.selectedProvider,
                    modelName = state.selectedModel,
                )

            if (state.currentConversationId == null) {
                _uiState.value = state.copy(currentConversationId = convId)
                selectConversation(convId)
            }

            _uiState.value = _uiState.value.copy(isStreaming = true, streamingContent = "", error = null)

            try {
                val providerConfig = settingsRepository.getActiveProviderConfig()

                chatRepository.streamReply(
                    conversationId = convId,
                    content = content,
                    providerId = state.selectedProvider,
                    baseUrl = providerConfig.baseUrl,
                    apiKey = providerConfig.apiKey,
                    model = state.selectedModel,
                    onToken = { token ->
                        _uiState.value = _uiState.value.copy(
                            streamingContent = _uiState.value.streamingContent + token,
                        )
                    },
                    onComplete = { _ ->
                        _uiState.value = _uiState.value.copy(isStreaming = false, streamingContent = "")
                    },
                    onError = { e ->
                        _uiState.value = _uiState.value.copy(
                            isStreaming = false,
                            streamingContent = "",
                            error = e.message ?: "Unknown error",
                        )
                    },
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    streamingContent = "",
                    error = e.message ?: "Unknown error",
                )
            }
        }
    }

    fun selectProvider(providerId: String) {
        _uiState.value = _uiState.value.copy(selectedProvider = providerId)
        viewModelScope.launch { settingsRepository.setActiveProvider(providerId) }
    }

    fun selectModel(model: String) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
        viewModelScope.launch { settingsRepository.setActiveModel(model) }
    }

    fun deleteConversation(conversation: Conversation) {
        viewModelScope.launch {
            chatRepository.deleteConversation(conversation)
            if (_uiState.value.currentConversationId == conversation.id) {
                _uiState.value = _uiState.value.copy(currentConversationId = null, messages = emptyList())
            }
        }
    }

    fun newConversation() {
        _uiState.value = _uiState.value.copy(currentConversationId = null, messages = emptyList(), error = null)
    }
}
