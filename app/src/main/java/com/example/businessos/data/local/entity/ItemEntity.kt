package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "items",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "name"])
    ]
)
data class ItemEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val name: String,
    val retailPrice: Double,
    val wholesalePrice: Double,
    val stockQty: Int,
    val minStockLevel: Int = 3,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val updatedAt: Long = System.currentTimeMillis()
)
