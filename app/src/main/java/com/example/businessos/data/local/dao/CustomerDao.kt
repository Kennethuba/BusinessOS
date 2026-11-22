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
    @Query("SELECT * FROM customers WHERE businessId = :businessId ORDER BY balanceCents DESC, name ASC")
    fun observeCustomersForBusiness(businessId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE businessId = :businessId ORDER BY balanceCents DESC, name ASC")
    suspend fun getCustomers(businessId: String): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE businessId = :businessId AND balanceCents > 0 ORDER BY balanceCents DESC")
    fun observeDeniLedger(businessId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getCustomerById(id: String, businessId: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE id = :id AND businessId = :businessId LIMIT 1")
    fun observeCustomerById(id: String, businessId: String): Flow<CustomerEntity?>

    @Query("SELECT COALESCE(SUM(balanceCents), 0) FROM customers WHERE businessId = :businessId AND balanceCents > 0")
    fun observeTotalDeni(businessId: String): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    /** Adjust outstanding deni balance — deltaCents positive = owe more, negative = paid down. */
    @Query("SELECT * FROM customers WHERE businessId = :businessId AND phone = :phone AND phone != '' LIMIT 1")
    suspend fun getByPhone(businessId: String, phone: String): CustomerEntity?

    @Query("UPDATE customers SET balanceCents = balanceCents + :deltaCents WHERE id = :id AND businessId = :businessId")
    suspend fun updateBalanceCents(id: String, businessId: String, deltaCents: Long)

    @Query("DELETE FROM customers WHERE id = :id AND businessId = :businessId")
    suspend fun deleteCustomer(id: String, businessId: String)
}
