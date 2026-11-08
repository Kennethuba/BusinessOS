package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.WagePaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WagePaymentDao {
    @Query("SELECT * FROM wage_payments WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun observeWagePayments(businessId: String): Flow<List<WagePaymentEntity>>

    @Query("SELECT * FROM wage_payments WHERE businessId = :businessId AND staffUserId = :staffUserId ORDER BY createdAt DESC")
    fun observeWagePaymentsForStaff(businessId: String, staffUserId: String): Flow<List<WagePaymentEntity>>

    @Query("SELECT * FROM wage_payments WHERE businessId = :businessId ORDER BY createdAt DESC")
    suspend fun getWagePayments(businessId: String): List<WagePaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWagePayment(payment: WagePaymentEntity)

    @Delete
    suspend fun deleteWagePayment(payment: WagePaymentEntity)

    @Query("SELECT SUM(amount) FROM wage_payments WHERE businessId = :businessId")
    suspend fun getTotalWagesPaid(businessId: String): Double?
}
