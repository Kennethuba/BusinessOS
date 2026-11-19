package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "suppliers",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "name"])
    ]
)
data class SupplierEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val name: String,
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val balanceCents: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
