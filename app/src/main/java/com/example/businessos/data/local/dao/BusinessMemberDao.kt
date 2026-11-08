package com.example.businessos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.businessos.data.local.entity.BusinessMemberEntity
import com.example.businessos.data.model.Role
import kotlinx.coroutines.flow.Flow

data class MemberWithUser(
    val businessId: String,
    val userId: String,
    val role: Role,
    val name: String,
    val email: String,
    val phone: String
)

@Dao
interface BusinessMemberDao {
    @Query("SELECT * FROM business_members WHERE businessId = :businessId AND userId = :userId LIMIT 1")
    suspend fun getMember(businessId: String, userId: String): BusinessMemberEntity?

    @Query("SELECT * FROM business_members WHERE businessId = :businessId AND userId = :userId LIMIT 1")
    fun observeMember(businessId: String, userId: String): Flow<BusinessMemberEntity?>

    @Query("""
        SELECT bm.businessId, bm.userId, bm.role, u.name, u.email, u.phone
        FROM business_members bm
        INNER JOIN users u ON bm.userId = u.id
        WHERE bm.businessId = :businessId
        ORDER BY bm.role ASC, u.name ASC
    """)
    fun observeMembersWithUser(businessId: String): Flow<List<MemberWithUser>>

    @Query("SELECT COUNT(*) FROM business_members WHERE businessId = :businessId")
    suspend fun getMemberCount(businessId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: BusinessMemberEntity)

    @Update
    suspend fun updateMember(member: BusinessMemberEntity)

    @Query("DELETE FROM business_members WHERE businessId = :businessId AND userId = :userId")
    suspend fun deleteMember(businessId: String, userId: String)

    @Query("SELECT * FROM business_members WHERE syncStatus = 'PENDING'")
    suspend fun getPendingMembers(): List<BusinessMemberEntity>
}
