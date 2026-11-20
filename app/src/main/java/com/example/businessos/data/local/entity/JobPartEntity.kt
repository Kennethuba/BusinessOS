package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "job_parts",
    foreignKeys = [
        ForeignKey(
            entity = JobCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["jobCardId"],
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
        Index(value = ["jobCardId"]),
        Index(value = ["itemId"])
    ]
)
data class JobPartEntity(
    @PrimaryKey val id: String,
    val jobCardId: String,
    val itemId: String? = null,
    val name: String,
    val qty: Int = 1,
    val unitPriceCents: Long = 0L,
    val costPriceCents: Long = 0L,
    val stockDeducted: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
) {
    @get:Ignore
    val partName: String get() = name

    @get:Ignore
    val quantity: Int get() = qty

    @get:Ignore
    val isStockDeducted: Boolean get() = stockDeducted
}
