package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.WagePaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WagePaymentDao {
    @Query("SELECT * FROM wage_payments WHERE businessId = :businessId AND isVoided = 0 ORDER BY createdAt DESC")
    fun observeWagePayments(businessId: String): Flow<List<WagePaymentEntity>>

    @Query("SELECT * FROM wage_payments WHERE businessId = :businessId AND staffUserId = :staffUserId AND isVoided = 0 ORDER BY createdAt DESC")
    fun observeWagePaymentsForStaff(businessId: String, staffUserId: String): Flow<List<WagePaymentEntity>>

    @Query("SELECT * FROM wage_payments WHERE businessId = :businessId AND isVoided = 0 ORDER BY createdAt DESC")
    suspend fun getWagePayments(businessId: String): List<WagePaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWagePayment(payment: WagePaymentEntity)

    @Update
    suspend fun updateWagePayment(payment: WagePaymentEntity)

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM wage_payments WHERE businessId = :businessId AND isVoided = 0")
    suspend fun getTotalWagesPaid(businessId: String): Long

    @Query("UPDATE wage_payments SET isVoided = 1, voidReason = :reason, voidedByUserId = :voidedBy, voidedAt = :voidedAt WHERE id = :id AND businessId = :businessId")
    suspend fun markVoided(id: String, businessId: String, reason: String, voidedBy: String, voidedAt: Long = System.currentTimeMillis())
}
