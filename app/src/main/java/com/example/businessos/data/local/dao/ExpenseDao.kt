package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE businessId = :businessId AND isVoided = 0 ORDER BY createdAt DESC")
    fun observeExpenses(businessId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE businessId = :businessId AND createdAt >= :startMs AND createdAt <= :endMs AND isVoided = 0 ORDER BY createdAt DESC")
    suspend fun getExpensesBetween(businessId: String, startMs: Long, endMs: Long): List<ExpenseEntity>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM expenses WHERE businessId = :businessId AND isVoided = 0")
    fun observeTotalExpenses(businessId: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM expenses WHERE businessId = :businessId AND createdAt >= :startMs AND createdAt <= :endMs AND isVoided = 0")
    suspend fun getTotalExpensesBetween(businessId: String, startMs: Long, endMs: Long): Long

    @Query("UPDATE expenses SET isVoided = 1, voidReason = :reason, voidedByUserId = :voidedBy, voidedAt = :voidedAt WHERE id = :id AND businessId = :businessId")
    suspend fun markVoided(id: String, businessId: String, reason: String, voidedBy: String, voidedAt: Long = System.currentTimeMillis())
}
