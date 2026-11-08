package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.InvoiceLineItem
import com.example.businessos.data.model.InvoiceStatus
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "invoices",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "invoiceNumber"], unique = true),
        Index(value = ["businessId", "customerId"]),
        Index(value = ["businessId", "status"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val invoiceNumber: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val issueDate: Long,
    val dueDate: Long,
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val balanceDue: Double = totalAmount - paidAmount,
    val status: InvoiceStatus = InvoiceStatus.UNPAID,
    val paymentMethod: PaymentMethod = PaymentMethod.CREDIT,
    val items: List<InvoiceLineItem> = emptyList(),
    val notes: String = "",
    val createdByUserId: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
