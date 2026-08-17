package com.clawdroid.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.clawdroid.android.data.local.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY createdAt ASC")
    fun getForConversation(convId: Long): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY createdAt ASC")
    suspend fun getForConversationOnce(convId: Long): List<Message>

    @Insert
    suspend fun insert(message: Message): Long

    @Insert
    suspend fun insertAll(messages: List<Message>)

    @Delete
    suspend fun delete(message: Message)

    @Query("DELETE FROM messages WHERE conversationId = :convId")
    suspend fun deleteForConversation(convId: Long)

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :convId")
    suspend fun countForConversation(convId: Long): Int
}
