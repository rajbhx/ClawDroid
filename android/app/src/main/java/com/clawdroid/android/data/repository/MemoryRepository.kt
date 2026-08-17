package com.clawdroid.android.data.repository

import com.clawdroid.android.data.local.dao.MemoryDao
import com.clawdroid.android.data.local.entity.MemoryEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepository @Inject constructor(
    private val memoryDao: MemoryDao,
) {
    fun getAll(): Flow<List<MemoryEntry>> = memoryDao.getAll()
    fun getByCategory(category: String): Flow<List<MemoryEntry>> = memoryDao.getByCategory(category)
    fun search(query: String): Flow<List<MemoryEntry>> = memoryDao.search(query)
    suspend fun getById(id: Long): MemoryEntry? = memoryDao.getById(id)
    suspend fun insert(memory: MemoryEntry): Long = memoryDao.insert(memory)
    suspend fun update(memory: MemoryEntry) = memoryDao.update(memory)
    suspend fun delete(memory: MemoryEntry) = memoryDao.delete(memory)
    suspend fun deleteById(id: Long) = memoryDao.deleteById(id)
}
