package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun observeExpenses(businessId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE businessId = :businessId AND createdAt >= :startMs AND createdAt <= :endMs ORDER BY createdAt DESC")
    suspend fun getExpensesBetween(businessId: String, startMs: Long, endMs: Long): List<ExpenseEntity>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE businessId = :businessId")
    fun observeTotalExpenses(businessId: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE businessId = :businessId AND createdAt >= :startMs AND createdAt <= :endMs")
    suspend fun getTotalExpensesBetween(businessId: String, startMs: Long, endMs: Long): Double

    @Query("DELETE FROM expenses WHERE id = :id AND businessId = :businessId")
    suspend fun deleteExpense(id: String, businessId: String)

    @Query("SELECT * FROM expenses WHERE syncStatus = 'PENDING'")
    suspend fun getPendingExpenses(): List<ExpenseEntity>

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
}
