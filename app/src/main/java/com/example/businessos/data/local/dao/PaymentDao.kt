package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE businessId = :businessId ORDER BY date DESC, createdAt DESC")
    fun observePayments(businessId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE targetInvoiceId = :invoiceId ORDER BY date DESC")
    fun observePaymentsForInvoice(invoiceId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE targetBillId = :billId ORDER BY date DESC")
    fun observePaymentsForBill(billId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getPaymentById(id: String, businessId: String): PaymentEntity?

    @Query("SELECT COUNT(*) FROM payments WHERE businessId = :businessId")
    suspend fun countPayments(businessId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)
}
