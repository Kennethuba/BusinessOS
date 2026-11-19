package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["invoiceId"]),
        Index(value = ["itemId"])
    ]
)
data class InvoiceItemEntity(
    @PrimaryKey val id: String,
    val invoiceId: String,
    val itemId: String? = null,
    val description: String,
    val quantity: Int = 1,
    val unitPriceCents: Long,
    val costPriceCents: Long = 0L,
    val subtotalCents: Long
)
