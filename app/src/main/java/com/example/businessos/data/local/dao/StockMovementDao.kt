package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.StockMovementEntity
import com.example.businessos.data.model.StockMovementType
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity)

    @Query("SELECT * FROM stock_movements WHERE itemId = :itemId ORDER BY createdAt DESC")
    fun observeByItem(itemId: String): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE itemId = :itemId ORDER BY createdAt DESC")
    suspend fun getByItem(itemId: String): List<StockMovementEntity>

    @Query("SELECT * FROM stock_movements WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun observeByBusiness(businessId: String): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE businessId = :businessId AND balanceAfter < 0 ORDER BY createdAt DESC")
    fun observeNegativeStockMovements(businessId: String): Flow<List<StockMovementEntity>>

    @Query("SELECT COALESCE(SUM(quantityDelta), 0) FROM stock_movements WHERE itemId = :itemId")
    suspend fun sumDeltaForItem(itemId: String): Long

    @Query("SELECT * FROM stock_movements WHERE businessId = :businessId AND movementType = :type ORDER BY createdAt DESC")
    suspend fun getByType(businessId: String, type: StockMovementType): List<StockMovementEntity>
}
