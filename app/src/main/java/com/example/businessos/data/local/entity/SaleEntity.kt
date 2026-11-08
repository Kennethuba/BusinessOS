package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.SaleLineItem
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "createdAt"]),
        Index(value = ["customerId"])
    ]
)
data class SaleEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val items: List<SaleLineItem>,
    val total: Double,
    val paymentMethod: PaymentMethod,
    val customerId: String? = null,
    val mpesaCode: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val createdByUserId: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val updatedAt: Long = System.currentTimeMillis()
)
