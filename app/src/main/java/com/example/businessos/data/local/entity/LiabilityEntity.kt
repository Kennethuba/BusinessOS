package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "liabilities",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "lenderName"])
    ]
)
data class LiabilityEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val lenderName: String,
    val principalAmount: Double,
    val interestRatePercent: Double = 0.0,
    val totalRepayable: Double,
    val totalPaid: Double = 0.0,
    val balanceDue: Double = totalRepayable - totalPaid,
    val startDate: Long,
    val dueDate: Long,
    val status: String = "ACTIVE",
    val notes: String = "",
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
