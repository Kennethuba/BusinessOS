package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers WHERE businessId = :businessId ORDER BY name ASC")
    fun observeSuppliers(businessId: String): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE businessId = :businessId ORDER BY name ASC")
    suspend fun getSuppliers(businessId: String): List<SupplierEntity>

    @Query("SELECT * FROM suppliers WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getSupplierById(id: String, businessId: String): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Query("UPDATE suppliers SET balanceCents = balanceCents + :deltaCents WHERE id = :id AND businessId = :businessId")
    suspend fun adjustPayable(id: String, businessId: String, deltaCents: Long)

    @Query("DELETE FROM suppliers WHERE id = :id AND businessId = :businessId")
    suspend fun deleteSupplier(id: String, businessId: String)
}
