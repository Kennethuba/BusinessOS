package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.StockMovementType

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["itemId"]),
        Index(value = ["businessId", "createdAt"]),
        Index(value = ["referenceType", "referenceId"])
    ]
)
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val itemId: String,
    val movementType: StockMovementType,
    val quantityDelta: Long,
    val balanceAfter: Long,
    val unitCostCents: Long = 0L,
    val referenceType: String,
    val referenceId: String,
    val notes: String = "",
    val createdByUserId: String,
    val createdAt: Long = System.currentTimeMillis()
)
