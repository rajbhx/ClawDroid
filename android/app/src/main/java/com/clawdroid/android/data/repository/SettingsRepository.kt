package com.clawdroid.android.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.clawdroid.android.data.local.dao.ProviderKeyDao
import com.clawdroid.android.data.local.entity.ProviderKey
import com.clawdroid.android.data.network.DEFAULT_PROVIDERS
import com.clawdroid.android.data.network.ProviderConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val providerKeyDao: ProviderKeyDao,
) {
    companion object {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val FONT_SIZE = intPreferencesKey("font_size")
        val ACTIVE_PROVIDER = stringPreferencesKey("active_provider")
        val ACTIVE_MODEL = stringPreferencesKey("active_model")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
    }

    val themeMode: Flow<Int> = dataStore.data.map { it[THEME_MODE] ?: 1 }
    val fontSize: Flow<Int> = dataStore.data.map { it[FONT_SIZE] ?: 14 }
    val activeProvider: Flow<String> = dataStore.data.map { it[ACTIVE_PROVIDER] ?: "openai" }
    val activeModel: Flow<String> = dataStore.data.map { it[ACTIVE_MODEL] ?: "gpt-4o-mini" }
    val systemPrompt: Flow<String> = dataStore.data.map { it[SYSTEM_PROMPT] ?: "You are ClawDroid, a helpful AI assistant running on Android." }

    suspend fun setThemeMode(mode: Int) = dataStore.edit { it[THEME_MODE] = mode }
    suspend fun setFontSize(size: Int) = dataStore.edit { it[FONT_SIZE] = size }
    suspend fun setActiveProvider(provider: String) = dataStore.edit { it[ACTIVE_PROVIDER] = provider }
    suspend fun setActiveModel(model: String) = dataStore.edit { it[ACTIVE_MODEL] = model }
    suspend fun setSystemPrompt(prompt: String) = dataStore.edit { it[SYSTEM_PROMPT] = prompt }

    fun getProviderKeys(): Flow<List<ProviderKey>> = providerKeyDao.getAll()
    suspend fun upsertProviderKey(provider: ProviderKey) = providerKeyDao.upsert(provider)
    suspend fun deleteProviderKey(provider: ProviderKey) = providerKeyDao.delete(provider)
    suspend fun getProviderKey(id: String): ProviderKey? = providerKeyDao.getById(id)

    suspend fun getActiveProviderConfig(): ProviderConfig {
        val key = providerKeyDao.getAll().first().firstOrNull { it.isActive }
        if (key != null) {
            val defaultProvider = DEFAULT_PROVIDERS.find { it.id == key.providerId }
            return ProviderConfig(
                id = key.providerId,
                name = key.name,
                baseUrl = key.baseUrl.ifBlank { defaultProvider?.baseUrl ?: "" },
                apiKey = key.apiKey,
                models = defaultProvider?.models ?: listOf(key.selectedModel),
            )
        }
        return DEFAULT_PROVIDERS.first()
    }
}
