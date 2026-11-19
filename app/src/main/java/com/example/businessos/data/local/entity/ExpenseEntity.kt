package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.BusinessSide
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PaymentMethod

@Entity(
    tableName = "expenses",
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
        Index(value = ["businessId", "createdAt"]),
        Index(value = ["isVoided"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val expenseNumber: String,
    val category: String,
    val title: String = "",
    val amountCents: Long,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val notes: String? = null,
    val side: BusinessSide = BusinessSide.SHOP,
    val isVoided: Boolean = false,
    val voidReason: String? = null,
    val voidedByUserId: String? = null,
    val voidedAt: Long? = null,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val amount: Double get() = amountCents / 100.0
    val amountMoney: Money get() = Money(amountCents)
}
