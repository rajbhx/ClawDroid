package com.clawdroid.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.clawdroid.android.data.local.entity.ProviderKey
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderKeyDao {
    @Query("SELECT * FROM provider_keys ORDER BY name ASC")
    fun getAll(): Flow<List<ProviderKey>>

    @Query("SELECT * FROM provider_keys WHERE isActive = 1")
    fun getActive(): Flow<List<ProviderKey>>

    @Query("SELECT * FROM provider_keys WHERE providerId = :id")
    suspend fun getById(id: String): ProviderKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(provider: ProviderKey)

    @Delete
    suspend fun delete(provider: ProviderKey)

    @Query("UPDATE provider_keys SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE provider_keys SET isActive = 1 WHERE providerId = :id")
    suspend fun activate(id: String)
}
