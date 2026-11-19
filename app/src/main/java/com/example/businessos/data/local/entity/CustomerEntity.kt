package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.Money

@Entity(
    tableName = "customers",
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
        Index(value = ["businessId", "phone"], unique = true),
        Index(value = ["businessId", "name"])
    ]
)
data class CustomerEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val name: String,
    val phone: String,
    val balanceCents: Long = 0L,
    val dueDate: Long? = null,
    val notes: String = "",
    val lastReminderSent: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val balance: Double get() = balanceCents / 100.0
    val balanceMoney: Money get() = Money(balanceCents)
}
