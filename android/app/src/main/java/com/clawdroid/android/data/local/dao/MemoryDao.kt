package com.clawdroid.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.clawdroid.android.data.local.entity.MemoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<MemoryEntry>>

    @Query("SELECT * FROM memories WHERE category = :category ORDER BY updatedAt DESC")
    fun getByCategory(category: String): Flow<List<MemoryEntry>>

    @Query("SELECT * FROM memories WHERE content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun search(query: String): Flow<List<MemoryEntry>>

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getById(id: Long): MemoryEntry?

    @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
    suspend fun getAllOnce(): List<MemoryEntry>

    @Insert
    suspend fun insert(memory: MemoryEntry): Long

    @Update
    suspend fun update(memory: MemoryEntry)

    @Delete
    suspend fun delete(memory: MemoryEntry)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE memories SET embedding = :embedding WHERE id = :id")
    suspend fun updateEmbedding(id: Long, embedding: ByteArray)
}
