package com.clawdroid.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provider_keys")
data class ProviderKey(
    @PrimaryKey val providerId: String,
    val name: String,
    val apiKey: String,
    val baseUrl: String,
    val selectedModel: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)
