package com.example.businessos.data.local.entity

import androidx.room.Entity
import com.example.businessos.data.model.Role
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "business_members",
    primaryKeys = ["businessId", "userId"]
)
data class BusinessMemberEntity(
    val businessId: String,
    val userId: String,
    val role: Role,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val updatedAt: Long = System.currentTimeMillis()
)
