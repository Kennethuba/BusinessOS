package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.AccountEntity
import com.example.businessos.data.model.AccountCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE businessId = :businessId ORDER BY code ASC")
    fun observeAccounts(businessId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE businessId = :businessId AND category = :category ORDER BY code ASC")
    fun observeAccountsByCategory(businessId: String, category: AccountCategory): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getAccountById(id: String, businessId: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE code = :code AND businessId = :businessId LIMIT 1")
    suspend fun getAccountByCode(code: String, businessId: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE businessId = :businessId ORDER BY code ASC")
    suspend fun getAccounts(businessId: String): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = balance + :delta, updatedAt = :updatedAt WHERE id = :id AND businessId = :businessId")
    suspend fun adjustBalance(id: String, businessId: String, delta: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM accounts WHERE id = :id AND businessId = :businessId AND isSystemAccount = 0")
    suspend fun deleteAccount(id: String, businessId: String)
}
