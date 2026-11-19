package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "businesses",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["isDemo"])
    ]
)
data class BusinessEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val address: String = "",
    val receiptFooter: String = "",
    val isShopEnabled: Boolean = true,
    val isGarageEnabled: Boolean = false,
    val isDemo: Boolean = false,
    val allowNegativeStock: Boolean = false,
    val ownerId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
