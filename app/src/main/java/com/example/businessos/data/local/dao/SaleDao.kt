package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun observeSalesForBusiness(businessId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getSaleById(id: String, businessId: String): SaleEntity?

    @Query("SELECT * FROM sales WHERE customerId = :customerId AND businessId = :businessId ORDER BY createdAt DESC")
    fun observeSalesForCustomer(customerId: String, businessId: String): Flow<List<SaleEntity>>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM sales WHERE businessId = :businessId AND createdAt >= :sinceTimestamp")
    fun observeTotalSalesSince(businessId: String, sinceTimestamp: Long): Flow<Double?>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM sales WHERE businessId = :businessId")
    fun observeTotalSales(businessId: String): Flow<Double?>

    @Query("SELECT * FROM sales WHERE businessId = :businessId AND createdAt >= :startMs AND createdAt <= :endMs ORDER BY createdAt DESC")
    suspend fun getSalesBetween(businessId: String, startMs: Long, endMs: Long): List<SaleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Query("DELETE FROM sales WHERE id = :id AND businessId = :businessId")
    suspend fun deleteSale(id: String, businessId: String)

    @Query("SELECT * FROM sales WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSales(): List<SaleEntity>
}
