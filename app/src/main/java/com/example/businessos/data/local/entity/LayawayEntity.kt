package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.Money

@Entity(
    tableName = "layaways",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
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
        Index(value = ["customerId"]),
        Index(value = ["itemId"]),
        Index(value = ["status"])
    ]
)
data class LayawayEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val layawayNumber: String,
    val customerId: String? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val itemId: String? = null,
    val itemDescription: String = "",
    val quantity: Int = 1,
    val totalAmountCents: Long,
    val depositAmountCents: Long = 0L,
    val balanceDueCents: Long = totalAmountCents - depositAmountCents,
    val dueDate: Long,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, CANCELLED, FORFEITED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val description: String get() = itemDescription
    val totalAmount: Double get() = totalAmountCents / 100.0
    val totalPaid: Double get() = depositAmountCents / 100.0
    val balanceDue: Double get() = balanceDueCents / 100.0

    val totalAmountMoney: Money get() = Money(totalAmountCents)
    val depositMoney: Money get() = Money(depositAmountCents)
    val balanceDueMoney: Money get() = Money(balanceDueCents)
}
