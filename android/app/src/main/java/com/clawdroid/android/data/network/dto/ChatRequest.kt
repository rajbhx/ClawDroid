package com.clawdroid.android.data.network.dto

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = true,
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("max_tokens") val maxTokens: Int = 4096,
)

data class ChatMessage(
    val role: String,
    val content: String,
)

data class ToolCall(
    val id: String,
    val type: String = "function",
    val function: FunctionCall,
)

data class FunctionCall(
    val name: String,
    val arguments: String,
)
