package com.clawdroid.android.di

import android.content.Context
import androidx.room.Room
import com.clawdroid.android.data.local.ClawdroidDatabase
import com.clawdroid.android.data.local.dao.ConversationDao
import com.clawdroid.android.data.local.dao.MemoryDao
import com.clawdroid.android.data.local.dao.MessageDao
import com.clawdroid.android.data.local.dao.ProviderKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ClawdroidDatabase =
        Room.databaseBuilder(context, ClawdroidDatabase::class.java, "clawdroid.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideConversationDao(db: ClawdroidDatabase): ConversationDao = db.conversationDao()
    @Provides fun provideMessageDao(db: ClawdroidDatabase): MessageDao = db.messageDao()
    @Provides fun provideMemoryDao(db: ClawdroidDatabase): MemoryDao = db.memoryDao()
    @Provides fun provideProviderKeyDao(db: ClawdroidDatabase): ProviderKeyDao = db.providerKeyDao()
}
