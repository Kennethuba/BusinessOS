package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.StockMovementType
import java.util.UUID

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
        Index(value = ["businessId", "occurredAt"])
    ]
)
data class StockMovementEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val itemId: String,
    val movementType: StockMovementType,
    val deltaQty: Long,
    val balanceAfter: Long,
    val reference: String = "",
    val recordedByUserId: String = "",
    val occurredAt: Long = System.currentTimeMillis()
) {
    @get:Ignore
    val quantityDelta: Int get() = deltaQty.toInt()

    @get:Ignore
    val createdAt: Long get() = occurredAt

    @get:Ignore
    val createdByUserId: String get() = recordedByUserId
}
