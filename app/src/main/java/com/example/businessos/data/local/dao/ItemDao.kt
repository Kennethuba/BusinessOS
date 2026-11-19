package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE businessId = :businessId AND isActive = 1 ORDER BY name ASC")
    fun observeItems(businessId: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE businessId = :businessId ORDER BY name ASC")
    suspend fun getItems(businessId: String): List<ItemEntity>

    @Query("SELECT * FROM items WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getItemById(id: String, businessId: String): ItemEntity?

    @Query("SELECT * FROM items WHERE businessId = :businessId AND (name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%') AND isActive = 1 ORDER BY name ASC")
    fun searchItems(businessId: String, query: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE businessId = :businessId AND stockQty <= lowStockThreshold AND isActive = 1")
    fun observeLowStockItems(businessId: String): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Update
    suspend fun updateItem(item: ItemEntity)

    /** Adjust stock quantity — delta can be positive (stock in) or negative (stock out). */
    @Query("UPDATE items SET stockQty = stockQty + :delta, updatedAt = :updatedAt WHERE id = :id AND businessId = :businessId")
    suspend fun adjustStock(id: String, businessId: String, delta: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM items WHERE id = :id AND businessId = :businessId")
    suspend fun deleteItem(id: String, businessId: String)
}
