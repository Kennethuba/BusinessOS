package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.PaymentType
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "paymentNumber"], unique = true),
        Index(value = ["targetInvoiceId"]),
        Index(value = ["targetBillId"])
    ]
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val paymentNumber: String,
    val type: PaymentType,
    val fromAccountId: String,
    val toAccountId: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val reference: String = "",
    val targetInvoiceId: String? = null,
    val targetBillId: String? = null,
    val description: String = "",
    val date: Long,
    val createdByUserId: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
