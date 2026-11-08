package com.example.businessos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.businessos.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE businessId = :businessId ORDER BY name ASC")
    fun observeItemsForBusiness(businessId: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE businessId = :businessId ORDER BY name ASC")
    suspend fun getItemsForBusiness(businessId: String): List<ItemEntity>

    @Query("SELECT * FROM items WHERE businessId = :businessId AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchItems(businessId: String, query: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getItemById(id: String, businessId: String): ItemEntity?

    @Query("SELECT * FROM items WHERE id = :id AND businessId = :businessId LIMIT 1")
    fun observeItemById(id: String, businessId: String): Flow<ItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Query("UPDATE items SET stockQty = stockQty - :qty WHERE id = :id AND businessId = :businessId")
    suspend fun decrementStock(id: String, businessId: String, qty: Int)

    @Query("DELETE FROM items WHERE id = :id AND businessId = :businessId")
    suspend fun deleteItem(id: String, businessId: String)

    @Query("SELECT * FROM items WHERE syncStatus = 'PENDING'")
    suspend fun getPendingItems(): List<ItemEntity>
}
