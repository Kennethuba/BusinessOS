package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.ActionType

@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "timestamp"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val businessId: String,
    val actorUserId: String,
    val actorName: String = "",
    val actionType: ActionType,
    val beforeValue: String,
    val afterValue: String,
    val timestamp: Long = System.currentTimeMillis()
)
