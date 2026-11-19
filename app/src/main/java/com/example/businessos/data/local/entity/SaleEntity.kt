package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.BusinessSide
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PaymentMethod

@Entity(
    tableName = "sales",
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
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "saleNumber"], unique = true),
        Index(value = ["businessId", "createdAt"]),
        Index(value = ["customerId"]),
        Index(value = ["isVoided"])
    ]
)
data class SaleEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val saleNumber: String,
    val customerId: String? = null,
    val customerName: String? = null,
    val subtotalCents: Long,
    val discountCents: Long = 0L,
    val totalCents: Long,
    val paymentMethod: PaymentMethod,
    val mpesaReference: String? = null,
    val side: BusinessSide = BusinessSide.SHOP,
    val isVoided: Boolean = false,
    val voidReason: String? = null,
    val voidedByUserId: String? = null,
    val voidedAt: Long? = null,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val total: Double get() = totalCents / 100.0
    val subtotal: Double get() = subtotalCents / 100.0
    val discount: Double get() = discountCents / 100.0

    val totalMoney: Money get() = Money(totalCents)
    val subtotalMoney: Money get() = Money(subtotalCents)
    val discountMoney: Money get() = Money(discountCents)

    val creditAmountCents: Long get() = if (paymentMethod == PaymentMethod.CREDIT) totalCents else 0L
}
