package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "layaways",
    indices = [Index(value = ["businessId"]), Index(value = ["customerId"])]
)
data class LayawayEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val customerId: String? = null,
    val customerName: String,
    val customerPhone: String,
    val description: String,
    val totalAmount: Double,
    val depositPaid: Double,
    val totalPaid: Double = depositPaid,
    val balanceDue: Double = totalAmount - totalPaid,
    val dueDate: Long,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, CANCELLED
    val createdByUserId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
