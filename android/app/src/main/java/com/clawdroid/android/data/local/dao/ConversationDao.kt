package com.clawdroid.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.clawdroid.android.data.local.entity.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: Long): Conversation?

    @Insert
    suspend fun insert(conversation: Conversation): Long

    @Update
    suspend fun update(conversation: Conversation)

    @Delete
    suspend fun delete(conversation: Conversation)

    @Query("UPDATE conversations SET title = :title, updatedAt = :time WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String, time: Long = System.currentTimeMillis())
}
