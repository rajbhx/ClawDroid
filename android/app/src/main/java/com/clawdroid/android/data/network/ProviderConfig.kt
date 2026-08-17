package com.clawdroid.android.data.network

data class ProviderConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val models: List<String>,
    val isOpenAICompatible: Boolean = true,
)

val DEFAULT_PROVIDERS = listOf(
    ProviderConfig("openai", "OpenAI", "https://api.openai.com/v1", "", listOf("gpt-4o", "gpt-4o-mini", "o1", "o3-mini")),
    ProviderConfig("anthropic", "Anthropic", "https://api.anthropic.com/v1", "", listOf("claude-sonnet-4-20250514", "claude-haiku-4-20250514"), false),
    ProviderConfig("google", "Google AI", "https://generativelanguage.googleapis.com/v1beta", "", listOf("gemini-2.0-flash", "gemini-2.5-pro")),
    ProviderConfig("deepseek", "DeepSeek", "https://api.deepseek.com/v1", "", listOf("deepseek-chat", "deepseek-coder")),
    ProviderConfig("groq", "Groq", "https://api.groq.com/openai/v1", "", listOf("llama-3.3-70b-versatile", "mixtral-8x7b-32768")),
    ProviderConfig("mistral", "Mistral", "https://api.mistral.ai/v1", "", listOf("mistral-large-latest", "codestral-latest")),
    ProviderConfig("openrouter", "OpenRouter", "https://openrouter.ai/api/v1", "", listOf("anthropic/claude-sonnet-4-20250514", "openai/gpt-4o", "google/gemini-2.0-flash-001")),
    ProviderConfig("ollama", "Ollama (Local)", "http://localhost:11434/v1", "", listOf("llama3.2", "codellama", "mistral")),
)

data class ModelInfo(
    val providerId: String,
    val modelId: String,
    val displayName: String,
    val isAvailable: Boolean = true,
)
