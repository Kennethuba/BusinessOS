package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "suppliers",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "name"])
    ]
)
data class SupplierEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val name: String,
    val phone: String,
    val email: String = "",
    val totalPayable: Double = 0.0,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
