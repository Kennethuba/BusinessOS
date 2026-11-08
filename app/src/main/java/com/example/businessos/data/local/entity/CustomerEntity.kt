package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "name"])
    ]
)
data class CustomerEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val name: String,
    val phone: String,
    val balance: Double = 0.0,
    val dueDate: Long? = null,
    val notes: String = "",
    val lastReminderSent: Long = 0L,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val updatedAt: Long = System.currentTimeMillis()
)
