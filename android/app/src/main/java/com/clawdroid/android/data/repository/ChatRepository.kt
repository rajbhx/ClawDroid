package com.clawdroid.android.data.repository

import com.clawdroid.android.data.local.dao.ConversationDao
import com.clawdroid.android.data.local.dao.MessageDao
import com.clawdroid.android.data.local.entity.Conversation
import com.clawdroid.android.data.local.entity.Message
import com.clawdroid.android.data.network.OmniRouteClient
import com.clawdroid.android.data.network.dto.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val omniRoute: OmniRouteClient,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
) {
    fun getAllConversations(): Flow<List<Conversation>> = conversationDao.getAll()

    fun getMessages(conversationId: Long): Flow<List<Message>> = messageDao.getForConversation(conversationId)

    suspend fun createConversation(title: String, providerId: String, modelName: String): Long =
        conversationDao.insert(Conversation(title = title, providerId = providerId, modelName = modelName))

    suspend fun streamReply(
        conversationId: Long,
        content: String,
        providerId: String,
        baseUrl: String,
        apiKey: String,
        model: String,
        onToken: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            try {
                messageDao.insert(Message(conversationId = conversationId, role = "user", content = content))
                val history = messageDao.getForConversationOnce(conversationId)
                val chatMessages = history.map { ChatMessage(role = it.role, content = it.content) }

                val fullResponse = StringBuilder()
                omniRoute.streamChat(
                    baseUrl = baseUrl,
                    apiKey = apiKey,
                    model = model,
                    messages = chatMessages,
                ).collect { token ->
                    fullResponse.append(token)
                    onToken(token)
                }

                messageDao.insert(Message(
                    conversationId = conversationId,
                    role = "assistant",
                    content = fullResponse.toString(),
                    model = model,
                    tokenCount = fullResponse.length,
                ))

                val conv = conversationDao.getById(conversationId)
                if (conv != null) {
                    conversationDao.update(conv.copy(updatedAt = System.currentTimeMillis()))
                }

                onComplete(fullResponse.toString())
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    suspend fun deleteConversation(conversation: Conversation) = conversationDao.delete(conversation)
    suspend fun updateConversationTitle(id: Long, title: String) = conversationDao.updateTitle(id, title)
}
