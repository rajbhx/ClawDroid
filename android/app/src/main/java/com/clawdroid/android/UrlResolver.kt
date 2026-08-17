package com.clawdroid.android

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.withTimeout
import java.io.File
import java.net.URL

class UrlResolver(private val context: Context) {
    companion object {
        private const val CONFIG_FETCH_TIMEOUT_MS = 5_000L
    }

    private val configFile = File(context.filesDir, "usr/share/clawdroid/config.json")
    private val gson = Gson()

    suspend fun getBootstrapUrl(): String {
        val config = loadConfig()
        return config?.bootstrap?.url ?: "https://github.com/termux/termux-packages/releases/download/bootstrap-2026.08.16-r1%2Bapt.android-7/bootstrap-aarch64.zip"
    }

    suspend fun getWwwUrl(): String {
        val config = loadConfig()
        return config?.www?.url ?: "https://raw.githubusercontent.com/rajbhx/ClawDroid/main/android/app/src/main/assets/www.zip"
    }

    private suspend fun loadConfig(): RemoteConfig? {
        if (configFile.exists()) {
            return try {
                gson.fromJson(configFile.readText(), RemoteConfig::class.java)
            } catch (_: Exception) { null }
        }
        return try {
            withTimeout(CONFIG_FETCH_TIMEOUT_MS) {
                val json = URL("https://raw.githubusercontent.com/rajbhx/ClawDroid/main/config.json").readText()
                configFile.parentFile?.mkdirs()
                configFile.writeText(json)
                gson.fromJson(json, RemoteConfig::class.java)
            }
        } catch (_: Exception) { null }
    }

    data class RemoteConfig(
        val version: Int?, val bootstrap: ComponentConfig?,
        val www: ComponentConfig?, val platforms: List<PlatformConfig>?,
    )
    data class ComponentConfig(val url: String, val version: String?, @SerializedName("sha256") val sha256: String?)
    data class PlatformConfig(val id: String, val name: String, val icon: String?, val description: String?)
}
