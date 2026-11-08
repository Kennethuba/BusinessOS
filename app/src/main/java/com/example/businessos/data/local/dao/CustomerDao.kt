package com.example.businessos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.businessos.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE businessId = :businessId ORDER BY balance DESC, name ASC")
    fun observeCustomersForBusiness(businessId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE businessId = :businessId ORDER BY balance DESC, name ASC")
    suspend fun getCustomers(businessId: String): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE businessId = :businessId AND balance > 0 ORDER BY balance DESC")
    fun observeDeniLedger(businessId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getCustomerById(id: String, businessId: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE id = :id AND businessId = :businessId LIMIT 1")
    fun observeCustomerById(id: String, businessId: String): Flow<CustomerEntity?>

    @Query("SELECT SUM(balance) FROM customers WHERE businessId = :businessId AND balance > 0")
    fun observeTotalDeni(businessId: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET balance = balance + :delta WHERE id = :id AND businessId = :businessId")
    suspend fun updateBalance(id: String, businessId: String, delta: Double)

    @Query("DELETE FROM customers WHERE id = :id AND businessId = :businessId")
    suspend fun deleteCustomer(id: String, businessId: String)

    @Query("SELECT * FROM customers WHERE syncStatus = 'PENDING'")
    suspend fun getPendingCustomers(): List<CustomerEntity>
}
