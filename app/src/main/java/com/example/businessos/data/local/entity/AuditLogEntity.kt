package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.ActionType

@Entity(
    tableName = "audit_logs",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "createdAt"]),
        Index(value = ["actionType"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val actionType: ActionType,
    val beforeValue: String? = null,
    val afterValue: String? = null,
    val performedByUserId: String,
    val performedByName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
