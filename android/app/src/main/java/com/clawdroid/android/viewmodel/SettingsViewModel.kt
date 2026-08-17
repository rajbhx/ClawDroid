package com.clawdroid.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clawdroid.android.data.local.entity.ProviderKey
import com.clawdroid.android.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<Int> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val fontSize: StateFlow<Int> = settingsRepository.fontSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 14)

    val activeProvider: StateFlow<String> = settingsRepository.activeProvider
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "openai")

    val systemPrompt: StateFlow<String> = settingsRepository.systemPrompt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val providerKeys: StateFlow<List<ProviderKey>> = settingsRepository.getProviderKeys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setThemeMode(mode: Int) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    fun setFontSize(size: Int) = viewModelScope.launch { settingsRepository.setFontSize(size) }
    fun setSystemPrompt(prompt: String) = viewModelScope.launch { settingsRepository.setSystemPrompt(prompt) }
    fun upsertProviderKey(provider: ProviderKey) = viewModelScope.launch { settingsRepository.upsertProviderKey(provider) }
    fun deleteProviderKey(provider: ProviderKey) = viewModelScope.launch { settingsRepository.deleteProviderKey(provider) }
}
