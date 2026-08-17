package com.clawdroid.android.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memories",
    indices = [Index("category"), Index("createdAt")],
)
data class MemoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val embedding: ByteArray? = null,
    val category: String = "general",
    val tags: String = "",
    val sourceAgent: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MemoryEntry) return false
        return id == other.id
    }
    override fun hashCode(): Int = id.hashCode()
}
