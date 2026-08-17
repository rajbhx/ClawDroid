package com.clawdroid.android.data.repository

import com.clawdroid.android.data.local.dao.MemoryDao
import com.clawdroid.android.data.local.entity.MemoryEntry
import com.clawdroid.android.data.ml.EmbeddingService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepository @Inject constructor(
    private val memoryDao: MemoryDao,
    private val embeddingService: EmbeddingService,
) {
    fun getAll(): Flow<List<MemoryEntry>> = memoryDao.getAll()
    fun getByCategory(category: String): Flow<List<MemoryEntry>> = memoryDao.getByCategory(category)
    fun search(query: String): Flow<List<MemoryEntry>> = memoryDao.search(query)
    suspend fun getById(id: Long): MemoryEntry? = memoryDao.getById(id)

    suspend fun insert(memory: MemoryEntry): Long {
        val id = memoryDao.insert(memory)
        // Try to generate embedding asynchronously
        val embedding = embeddingService.embed(memory.content)
        if (embedding != null) {
            memoryDao.updateEmbedding(id, embedding.toByteArray())
        }
        return id
    }

    suspend fun update(memory: MemoryEntry) = memoryDao.update(memory)
    suspend fun delete(memory: MemoryEntry) = memoryDao.delete(memory)
    suspend fun deleteById(id: Long) = memoryDao.deleteById(id)

    suspend fun semanticSearch(query: String, topK: Int = 10): List<MemoryEntry> {
        val queryEmbedding = embeddingService.embed(query) ?: return memoryDao.getAllOnce().take(topK)
        val allMemories = memoryDao.getAllOnce()
        return allMemories
            .filter { it.embedding != null }
            .map { memory ->
                val memoryEmbedding = floatArrayOf()
                val bytes = memory.embedding!!
                val buffer = java.nio.ByteBuffer.wrap(bytes)
                val floats = FloatArray(bytes.size / 4)
                buffer.asFloatBuffer().get(floats)
                memory to embeddingService.cosineSimilarity(queryEmbedding, floats)
            }
            .sortedByDescending { it.second }
            .take(topK)
            .map { it.first }
    }
}
