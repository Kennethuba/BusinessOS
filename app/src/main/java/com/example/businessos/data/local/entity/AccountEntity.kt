package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.AccountCategory
import com.example.businessos.data.model.AccountSubCategory
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "code"], unique = true)
    ]
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val code: String,
    val name: String,
    val category: AccountCategory,
    val subCategory: AccountSubCategory,
    val isSystemAccount: Boolean = false,
    val balance: Double = 0.0,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val updatedAt: Long = System.currentTimeMillis()
)
