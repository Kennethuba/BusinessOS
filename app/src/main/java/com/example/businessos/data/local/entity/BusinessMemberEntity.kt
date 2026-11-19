package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.businessos.data.model.Role

@Entity(
    tableName = "business_members",
    primaryKeys = ["businessId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["userId"])
    ]
)
data class BusinessMemberEntity(
    val businessId: String,
    val userId: String,
    val role: Role,
    val canSellNegativeStock: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
