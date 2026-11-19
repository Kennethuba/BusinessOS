package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["phone"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val pinSalt: String = "",
    val pinHash: String = "",
    val failedAttempts: Int = 0,
    val lockoutUntil: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
