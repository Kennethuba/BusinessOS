package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wage_payments",
    indices = [Index(value = ["businessId"]), Index(value = ["businessId", "staffUserId"])]
)
data class WagePaymentEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val staffUserId: String,
    val staffName: String,
    val amount: Double,
    val period: String, // e.g. "October 2026", "Week 44"
    val paymentMethod: String, // CASH, MPESA, BANK
    val mpesaCode: String? = null,
    val notes: String = "",
    val createdByUserId: String,
    val createdAt: Long = System.currentTimeMillis()
)
