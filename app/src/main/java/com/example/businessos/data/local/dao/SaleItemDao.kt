package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.SaleItemEntity

@Dao
interface SaleItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(item: SaleItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId ORDER BY rowid ASC")
    suspend fun getBySale(saleId: String): List<SaleItemEntity>

    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    suspend fun deleteBySale(saleId: String)
}
