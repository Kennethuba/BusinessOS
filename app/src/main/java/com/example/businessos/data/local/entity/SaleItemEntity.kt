package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
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
        Index(value = ["saleId"]),
        Index(value = ["itemId"])
    ]
)
data class SaleItemEntity(
    @PrimaryKey val id: String,
    val saleId: String,
    val itemId: String,
    val itemName: String,
    val quantity: Int,
    val unitPriceCents: Long,
    val costPriceCents: Long = 0L,
    val subtotalCents: Long
) {
    @get:Ignore
    val qty: Int get() = quantity
}
