package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.DepreciationMethod
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "assets",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "assetNumber"], unique = true)
    ]
)
data class AssetEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val assetNumber: String,
    val name: String,
    val category: String,
    val acquisitionCost: Double,
    val purchaseDate: Long,
    val usefulLifeYears: Int = 5,
    val depreciationMethod: DepreciationMethod = DepreciationMethod.STRAIGHT_LINE,
    val salvageValue: Double = 0.0,
    val accumulatedDepreciation: Double = 0.0,
    val currentValue: Double = acquisitionCost - accumulatedDepreciation,
    val notes: String = "",
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
