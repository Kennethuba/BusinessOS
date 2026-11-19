package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "document_counters",
    primaryKeys = ["businessId", "documentType"],
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["businessId"])
    ]
)
data class DocumentCounterEntity(
    val businessId: String,
    val documentType: String,
    val currentValue: Long = 0L
)
