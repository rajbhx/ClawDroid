package com.clawdroid.android.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clawdroid.android.GpuDetector
import com.clawdroid.android.data.local.entity.ProviderKey
import com.clawdroid.android.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context,
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

    private val _gpuInfo = MutableStateFlow(GpuInfo())
    val gpuInfo: StateFlow<GpuInfo> = _gpuInfo.asStateFlow()

    data class GpuInfo(val method: String = "Unknown", val isAvailable: Boolean = false)

    init { refreshGpu() }

    fun refreshGpu() {
        val method = GpuDetector.getAccelerationMethod()
        _gpuInfo.value = GpuInfo(method = method, isAvailable = method != "Software")
    }

    fun setThemeMode(mode: Int) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    fun setFontSize(size: Int) = viewModelScope.launch { settingsRepository.setFontSize(size) }
    fun setSystemPrompt(prompt: String) = viewModelScope.launch { settingsRepository.setSystemPrompt(prompt) }
    fun upsertProviderKey(provider: ProviderKey) = viewModelScope.launch { settingsRepository.upsertProviderKey(provider) }
    fun deleteProviderKey(provider: ProviderKey) = viewModelScope.launch { settingsRepository.deleteProviderKey(provider) }
}
