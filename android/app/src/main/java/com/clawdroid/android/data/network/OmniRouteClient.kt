package com.clawdroid.android.data.network

import com.clawdroid.android.data.network.dto.ChatMessage
import com.clawdroid.android.data.network.dto.ChatRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OmniRouteClient @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson,
) {
    fun streamChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessage>,
        temperature: Double = 0.7,
        maxTokens: Int = 4096,
    ): Flow<String> = callbackFlow {
        withContext(Dispatchers.IO) {
            try {
                val request = ChatRequest(
                    model = model,
                    messages = messages,
                    stream = true,
                    temperature = temperature,
                    maxTokens = maxTokens,
                )

                val url = "${baseUrl.trimEnd('/')}/chat/completions"
                val body = gson.toJson(request)
                    .toRequestBody("application/json".toMediaType())

                val reqBuilder = Request.Builder()
                    .url(url)
                    .post(body)
                    .header("Content-Type", "application/json")

                if (apiKey.isNotBlank()) {
                    reqBuilder.header("Authorization", "Bearer $apiKey")
                }

                val response = client.newCall(reqBuilder.build()).execute()
                val source = response.body?.source() ?: throw Exception("Empty response body")

                val buffer = StringBuilder()
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: continue
                    if (line.startsWith("data: ")) {
                        val data = line.removePrefix("data: ").trim()
                        if (data == "[DONE]") break
                        try {
                            val chunk = gson.fromJson(data, com.clawdroid.android.data.network.dto.ChatResponse::class.java)
                            val content = chunk.choices?.firstOrNull()?.delta?.content
                            if (content != null) {
                                trySend(content)
                            }
                        } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                close(e)
            }
        }
        awaitClose()
    }

    suspend fun chatCompletion(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessage>,
        temperature: Double = 0.7,
        maxTokens: Int = 4096,
    ): String = withContext(Dispatchers.IO) {
        val request = ChatRequest(
            model = model,
            messages = messages,
            stream = false,
            temperature = temperature,
            maxTokens = maxTokens,
        )

        val url = "${baseUrl.trimEnd('/')}/chat/completions"
        val body = gson.toJson(request)
            .toRequestBody("application/json".toMediaType())

        val reqBuilder = Request.Builder()
            .url(url)
            .post(body)
            .header("Content-Type", "application/json")

        if (apiKey.isNotBlank()) {
            reqBuilder.header("Authorization", "Bearer $apiKey")
        }

        val response = client.newCall(reqBuilder.build()).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        val chatResponse = gson.fromJson(responseBody, com.clawdroid.android.data.network.dto.ChatResponse::class.java)
        chatResponse.choices?.firstOrNull()?.delta?.content ?: ""
    }

    suspend fun listModels(
        baseUrl: String,
        apiKey: String,
    ): List<String> = withContext(Dispatchers.IO) {
        val url = "${baseUrl.trimEnd('/')}/models"
        val reqBuilder = Request.Builder().url(url).get()
        if (apiKey.isNotBlank()) {
            reqBuilder.header("Authorization", "Bearer $apiKey")
        }
        val response = client.newCall(reqBuilder.build()).execute()
        val body = response.body?.string() ?: return@withContext emptyList()
        val json = gson.fromJson(body, Map::class.java)
        val data = json["data"] as? List<*> ?: return@withContext emptyList()
        data.mapNotNull { (it as? Map<*, *>)?.get("id") as? String }
    }
}
