package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.BusinessSide
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.PaymentType

@Entity(
    tableName = "payments",
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
        Index(value = ["businessId", "paymentNumber"], unique = true),
        Index(value = ["businessId", "createdAt"]),
        Index(value = ["referenceId"])
    ]
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val paymentNumber: String,
    val paymentType: PaymentType = PaymentType.CUSTOMER_RECEIPT,
    val amountCents: Long,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val referenceId: String? = null,
    val mpesaCode: String? = null,
    val side: BusinessSide = BusinessSide.SHOP,
    val notes: String = "",
    val createdByUserId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val targetInvoiceId: String? get() = referenceId
    val date: Long get() = createdAt
    val amount: Double get() = amountCents / 100.0
    val amountMoney: Money get() = Money(amountCents)
}
