package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.BusinessSide
import com.example.businessos.data.model.InvoiceStatus
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PaymentMethod

@Entity(
    tableName = "invoices",
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
        Index(value = ["businessId", "invoiceNumber"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["businessId", "customerId"]),
        Index(value = ["businessId", "status"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val invoiceNumber: String,
    val customerId: String? = null,
    val customerName: String,
    val customerPhone: String = "",
    val issueDate: Long,
    val dueDate: Long,
    val subtotalCents: Long = 0L,
    val discountAmountCents: Long = 0L,
    val taxAmountCents: Long = 0L,
    val totalAmountCents: Long,
    val paidAmountCents: Long = 0L,
    val balanceDueCents: Long = totalAmountCents - paidAmountCents,
    val status: InvoiceStatus = InvoiceStatus.UNPAID,
    val paymentMethod: PaymentMethod = PaymentMethod.CREDIT,
    val side: BusinessSide = BusinessSide.SHOP,
    val notes: String = "",
    val createdByUserId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalAmount: Double get() = totalAmountCents / 100.0
    val paidAmount: Double get() = paidAmountCents / 100.0
    val balanceDue: Double get() = balanceDueCents / 100.0

    val totalAmountMoney: Money get() = Money(totalAmountCents)
    val paidAmountMoney: Money get() = Money(paidAmountCents)
    val balanceDueMoney: Money get() = Money(balanceDueCents)
}
