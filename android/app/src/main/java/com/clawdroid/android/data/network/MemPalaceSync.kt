package com.clawdroid.android.data.network

import com.clawdroid.android.data.local.dao.MemoryDao
import com.clawdroid.android.data.local.entity.MemoryEntry
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemPalaceSync @Inject constructor(
    private val client: OkHttpClient,
    private val memoryDao: MemoryDao,
    private val gson: Gson,
) {
    private var serverUrl: String? = null

    fun configure(serverUrl: String) {
        this.serverUrl = serverUrl
    }

    suspend fun pushMemories(apiKey: String): SyncResult = withContext(Dispatchers.IO) {
        val url = serverUrl ?: return@withContext SyncResult(false, "No server configured")
        val memories = memoryDao.getAllOnce()
        val payload = mapOf(
            "memories" to memories.map { mapOf(
                "content" to it.content,
                "category" to it.category,
                "tags" to it.tags,
                "source" to "clawdroid",
            )},
        )
        val body = gson.toJson(payload).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$url/api/v1/memories/sync")
            .post(body)
            .header("Authorization", "Bearer $apiKey")
            .build()

        try {
            val response = client.newCall(request).execute()
            SyncResult(response.isSuccessful, if (response.isSuccessful) "Pushed ${memories.size} memories" else "Server error: ${response.code}")
        } catch (e: Exception) {
            SyncResult(false, e.message ?: "Connection failed")
        }
    }

    suspend fun pullMemories(apiKey: String): SyncResult = withContext(Dispatchers.IO) {
        val url = serverUrl ?: return@withContext SyncResult(false, "No server configured")
        val request = Request.Builder()
            .url("$url/api/v1/memories")
            .get()
            .header("Authorization", "Bearer $apiKey")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: return@withContext SyncResult(false, "Empty response")
                val data = gson.fromJson(json, Map::class.java)
                val memories = data["memories"] as? List<*> ?: emptyList<Any>()
                var count = 0
                memories.forEach { item ->
                    val map = item as? Map<*, *> ?: return@forEach
                    val content = map["content"] as? String ?: return@forEach
                    val category = map["category"] as? String ?: "general"
                    val tags = map["tags"] as? String ?: ""
                    memoryDao.insert(MemoryEntry(content = content, category = category, tags = tags, sourceAgent = "mempalace"))
                    count++
                }
                SyncResult(true, "Pulled $count memories")
            } else {
                SyncResult(false, "Server error: ${response.code}")
            }
        } catch (e: Exception) {
            SyncResult(false, e.message ?: "Connection failed")
        }
    }

    data class SyncResult(val success: Boolean, val message: String)
}
