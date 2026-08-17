package com.clawdroid.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.clawdroid.android.data.local.dao.ConversationDao
import com.clawdroid.android.data.local.dao.MemoryDao
import com.clawdroid.android.data.local.dao.MessageDao
import com.clawdroid.android.data.local.dao.ProviderKeyDao
import com.clawdroid.android.data.local.entity.Conversation
import com.clawdroid.android.data.local.entity.MemoryEntry
import com.clawdroid.android.data.local.entity.Message
import com.clawdroid.android.data.local.entity.ProviderKey

@Database(
    entities = [Conversation::class, Message::class, MemoryEntry::class, ProviderKey::class],
    version = 1,
    exportSchema = false,
)
abstract class ClawdroidDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun providerKeyDao(): ProviderKeyDao
}
