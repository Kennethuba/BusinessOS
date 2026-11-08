package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.BillStatus
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "bills",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "billNumber"]),
        Index(value = ["businessId", "supplierId"]),
        Index(value = ["businessId", "status"])
    ]
)
data class BillEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val billNumber: String,
    val supplierId: String,
    val supplierName: String,
    val issueDate: Long,
    val dueDate: Long,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val balanceDue: Double = totalAmount - paidAmount,
    val status: BillStatus = BillStatus.UNPAID,
    val category: String = "Mzigo / Stock",
    val description: String = "",
    val createdByUserId: String,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
