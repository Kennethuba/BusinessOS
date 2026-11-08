package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.BillEntity
import com.example.businessos.data.model.BillStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE businessId = :businessId ORDER BY issueDate DESC, createdAt DESC")
    fun observeBills(businessId: String): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE businessId = :businessId ORDER BY issueDate DESC, createdAt DESC")
    suspend fun getBills(businessId: String): List<BillEntity>

    @Query("SELECT * FROM bills WHERE businessId = :businessId AND supplierId = :supplierId ORDER BY issueDate DESC")
    fun observeBillsForSupplier(businessId: String, supplierId: String): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getBillById(id: String, businessId: String): BillEntity?

    @Query("SELECT SUM(balanceDue) FROM bills WHERE businessId = :businessId AND status != 'PAID' AND status != 'CANCELLED'")
    fun observeTotalPayables(businessId: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM bills WHERE businessId = :businessId")
    suspend fun countBills(businessId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity)

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Query("UPDATE bills SET paidAmount = paidAmount + :amountPaid, balanceDue = totalAmount - (paidAmount + :amountPaid), status = :newStatus WHERE id = :id AND businessId = :businessId")
    suspend fun recordBillPayment(id: String, businessId: String, amountPaid: Double, newStatus: BillStatus)

    @Query("DELETE FROM bills WHERE id = :id AND businessId = :businessId")
    suspend fun deleteBill(id: String, businessId: String)
}
