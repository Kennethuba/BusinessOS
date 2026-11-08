package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "createdAt"]),
        Index(value = ["businessId", "category"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val businessId: String,
    val category: String, // Rent, Stock/Supplies, Tokens/Electricity, Transport/Fare, Wages, Lunch/Food, Spares, Other
    val title: String,
    val amount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdByUserId: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val updatedAt: Long = System.currentTimeMillis()
)
