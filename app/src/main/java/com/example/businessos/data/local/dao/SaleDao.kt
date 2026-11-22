package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales WHERE businessId = :businessId AND isVoided = 0 ORDER BY createdAt DESC")
    fun observeSalesForBusiness(businessId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun observeAllSalesForBusiness(businessId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getSaleById(id: String, businessId: String): SaleEntity?

    @Query("SELECT * FROM sales WHERE customerId = :customerId AND businessId = :businessId AND isVoided = 0 ORDER BY createdAt DESC")
    fun observeSalesForCustomer(customerId: String, businessId: String): Flow<List<SaleEntity>>

    @Query("SELECT COALESCE(SUM(totalCents), 0) FROM sales WHERE businessId = :businessId AND createdAt >= :sinceTimestamp AND isVoided = 0")
    fun observeTotalSalesSince(businessId: String, sinceTimestamp: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(totalCents), 0) FROM sales WHERE businessId = :businessId AND isVoided = 0")
    fun observeTotalSales(businessId: String): Flow<Long>

    @Query("SELECT * FROM sales WHERE businessId = :businessId AND createdAt >= :startMs AND createdAt <= :endMs AND isVoided = 0 ORDER BY createdAt DESC")
    suspend fun getSalesBetween(businessId: String, startMs: Long, endMs: Long): List<SaleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Update
    suspend fun updateSale(sale: SaleEntity)

    /** No hard delete — use voidSale in the repository instead. */
    @Query("UPDATE sales SET isVoided = 1, voidReason = :reason, voidedByUserId = :voidedBy, voidedAt = :voidedAt WHERE id = :id AND businessId = :businessId")
    suspend fun markVoided(id: String, businessId: String, reason: String, voidedBy: String, voidedAt: Long = System.currentTimeMillis())
}
