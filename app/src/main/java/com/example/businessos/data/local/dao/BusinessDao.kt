package com.example.businessos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.businessos.data.local.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses WHERE id = :businessId LIMIT 1")
    suspend fun getBusinessById(businessId: String): BusinessEntity?

    @Query("SELECT * FROM businesses WHERE id = :businessId LIMIT 1")
    fun observeBusinessById(businessId: String): Flow<BusinessEntity?>

    @Query("""
        SELECT b.* FROM businesses b
        INNER JOIN business_members bm ON b.id = bm.businessId
        WHERE bm.userId = :userId
        ORDER BY b.name ASC
    """)
    fun observeBusinessesForUser(userId: String): Flow<List<BusinessEntity>>

    @Query("""
        SELECT b.* FROM businesses b
        INNER JOIN business_members bm ON b.id = bm.businessId
        WHERE bm.userId = :userId
        ORDER BY b.name ASC
    """)
    suspend fun getBusinessesForUser(userId: String): List<BusinessEntity>

    @Query("SELECT COUNT(*) FROM businesses WHERE ownerId = :ownerId")
    suspend fun getCountOwnedBy(ownerId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    @Query("DELETE FROM businesses WHERE id = :businessId")
    suspend fun deleteBusiness(businessId: String)
}
