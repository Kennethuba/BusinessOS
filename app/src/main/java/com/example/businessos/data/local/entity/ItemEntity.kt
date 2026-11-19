package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.Money

@Entity(
    tableName = "items",
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
        Index(value = ["businessId", "name"], unique = true),
        Index(value = ["businessId", "sku"])
    ]
)
data class ItemEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val name: String,
    val sku: String? = null,
    val category: String? = "General",
    val unit: String = "pcs",
    val costPriceCents: Long = 0L,
    val retailPriceCents: Long,
    val wholesalePriceCents: Long,
    val stockQty: Long = 0L,
    val lowStockThreshold: Int = 3,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val retailPrice: Double get() = retailPriceCents / 100.0
    val wholesalePrice: Double get() = wholesalePriceCents / 100.0
    val costPrice: Double get() = costPriceCents / 100.0
    val minStockLevel: Int get() = lowStockThreshold

    val retailMoney: Money get() = Money(retailPriceCents)
    val wholesaleMoney: Money get() = Money(wholesalePriceCents)
    val costMoney: Money get() = Money(costPriceCents)
}
