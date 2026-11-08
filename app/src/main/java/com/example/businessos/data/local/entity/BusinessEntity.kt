package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.businessos.data.model.BusinessType
import com.example.businessos.data.model.SubscriptionPlan
import com.example.businessos.data.model.SyncStatus

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: BusinessType,
    val createdAt: Long = System.currentTimeMillis(),
    val ownerId: String,
    val subscriptionPlan: SubscriptionPlan = SubscriptionPlan.STARTER,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val updatedAt: Long = System.currentTimeMillis()
)
