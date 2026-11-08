package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.businessos.data.model.SyncStatus

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val authId: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val updatedAt: Long = System.currentTimeMillis()
)
