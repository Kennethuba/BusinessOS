package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.LiabilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LiabilityDao {
    @Query("SELECT * FROM liabilities WHERE businessId = :businessId ORDER BY startDate DESC")
    fun observeLiabilities(businessId: String): Flow<List<LiabilityEntity>>

    @Query("SELECT * FROM liabilities WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getLiabilityById(id: String, businessId: String): LiabilityEntity?

    @Query("SELECT SUM(balanceDue) FROM liabilities WHERE businessId = :businessId AND status = 'ACTIVE'")
    fun observeTotalLiabilities(businessId: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM liabilities WHERE businessId = :businessId")
    suspend fun countLiabilities(businessId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiability(liability: LiabilityEntity)

    @Update
    suspend fun updateLiability(liability: LiabilityEntity)

    @Query("UPDATE liabilities SET totalPaid = totalPaid + :amountPaid, balanceDue = totalRepayable - (totalPaid + :amountPaid), status = :newStatus WHERE id = :id AND businessId = :businessId")
    suspend fun recordRepayment(id: String, businessId: String, amountPaid: Double, newStatus: String)

    @Query("DELETE FROM liabilities WHERE id = :id AND businessId = :businessId")
    suspend fun deleteLiability(id: String, businessId: String)
}
