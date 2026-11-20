package com.example.businessos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.businessos.data.local.entity.AuditLogEntity
import com.example.businessos.data.model.ActionType
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun observeAuditLogsForBusiness(businessId: String): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE businessId = :businessId AND actionType = :actionType ORDER BY createdAt DESC")
    fun observeByAction(businessId: String, actionType: ActionType): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Append-only: No UPDATE or DELETE methods on this DAO.
}
