package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PaymentMethod

@Entity(
    tableName = "wage_payments",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["staffUserId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["staffUserId"]),
        Index(value = ["businessId", "createdAt"]),
        Index(value = ["isVoided"])
    ]
)
data class WagePaymentEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val paymentNumber: String,
    val staffUserId: String,
    val staffName: String = "",
    val amountCents: Long,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val notes: String? = null,
    val isVoided: Boolean = false,
    val voidReason: String? = null,
    val voidedByUserId: String? = null,
    val voidedAt: Long? = null,
    val paidByUserId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val amount: Double get() = amountCents / 100.0
    val amountMoney: Money get() = Money(amountCents)
}
