package com.example.businessos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.businessos.data.local.entity.BusinessMemberEntity
import com.example.businessos.data.model.Role
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessMemberDao {
    @Query("SELECT * FROM business_members WHERE businessId = :businessId AND userId = :userId LIMIT 1")
    suspend fun getMember(businessId: String, userId: String): BusinessMemberEntity?

    @Query("SELECT * FROM business_members WHERE businessId = :businessId AND userId = :userId LIMIT 1")
    fun observeMember(businessId: String, userId: String): Flow<BusinessMemberEntity?>

    @Query("SELECT * FROM business_members WHERE businessId = :businessId ORDER BY role ASC")
    fun observeMembers(businessId: String): Flow<List<BusinessMemberEntity>>

    @Query("SELECT * FROM business_members WHERE businessId = :businessId ORDER BY role ASC")
    suspend fun getMembers(businessId: String): List<BusinessMemberEntity>

    @Query("SELECT COUNT(*) FROM business_members WHERE businessId = :businessId")
    suspend fun getMemberCount(businessId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: BusinessMemberEntity)

    @Update
    suspend fun updateMember(member: BusinessMemberEntity)

    @Query("DELETE FROM business_members WHERE businessId = :businessId AND userId = :userId")
    suspend fun deleteMember(businessId: String, userId: String)

    @Query("UPDATE business_members SET canSellNegativeStock = :canSell WHERE businessId = :businessId AND userId = :userId")
    suspend fun setCanSellNegativeStock(businessId: String, userId: String, canSell: Boolean)
}
