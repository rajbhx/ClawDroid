package com.clawdroid.android.data.network.dto

import com.google.gson.annotations.SerializedName

data class ChatResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage?,
)

data class Choice(
    val index: Int = 0,
    val delta: Delta?,
    val finishReason: String? = null,
)

data class Delta(
    val role: String? = null,
    val content: String? = null,
)

data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int = 0,
    @SerializedName("completion_tokens") val completionTokens: Int = 0,
    @SerializedName("total_tokens") val totalTokens: Int = 0,
)
