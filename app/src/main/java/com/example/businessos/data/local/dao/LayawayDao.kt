package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.LayawayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LayawayDao {
    @Query("SELECT * FROM layaways WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun observeLayaways(businessId: String): Flow<List<LayawayEntity>>

    @Query("SELECT * FROM layaways WHERE businessId = :businessId AND status = 'ACTIVE' ORDER BY dueDate ASC")
    fun observeActiveLayaways(businessId: String): Flow<List<LayawayEntity>>

    @Query("SELECT * FROM layaways WHERE id = :id")
    suspend fun getLayawayById(id: String): LayawayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayaway(layaway: LayawayEntity)

    @Update
    suspend fun updateLayaway(layaway: LayawayEntity)

    @Query("SELECT * FROM layaways WHERE businessId = :businessId ORDER BY createdAt DESC")
    suspend fun getLayaways(businessId: String): List<LayawayEntity>
}
