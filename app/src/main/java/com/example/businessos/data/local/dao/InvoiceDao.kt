package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.InvoiceEntity
import com.example.businessos.data.model.InvoiceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE businessId = :businessId ORDER BY issueDate DESC, createdAt DESC")
    fun observeInvoices(businessId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE businessId = :businessId AND status = :status ORDER BY issueDate DESC")
    fun observeInvoicesByStatus(businessId: String, status: InvoiceStatus): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE businessId = :businessId AND customerId = :customerId ORDER BY issueDate DESC")
    fun observeInvoicesForCustomer(businessId: String, customerId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getInvoiceById(id: String, businessId: String): InvoiceEntity?

    @Query("SELECT SUM(balanceDue) FROM invoices WHERE businessId = :businessId AND status != 'PAID' AND status != 'CANCELLED'")
    fun observeTotalReceivables(businessId: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM invoices WHERE businessId = :businessId")
    suspend fun countInvoices(businessId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("UPDATE invoices SET paidAmount = paidAmount + :amountPaid, balanceDue = totalAmount - (paidAmount + :amountPaid), status = :newStatus, updatedAt = :updatedAt WHERE id = :id AND businessId = :businessId")
    suspend fun recordInvoicePayment(id: String, businessId: String, amountPaid: Double, newStatus: InvoiceStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM invoices WHERE id = :id AND businessId = :businessId")
    suspend fun deleteInvoice(id: String, businessId: String)
}
