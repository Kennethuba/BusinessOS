package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.JournalLineItem
import com.example.businessos.data.model.JournalSourceType
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "journal_entries",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "entryNumber"], unique = true),
        Index(value = ["businessId", "date"]),
        Index(value = ["sourceReferenceId"])
    ]
)
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val entryNumber: String,
    val date: Long,
    val description: String,
    val sourceType: JournalSourceType,
    val sourceReferenceId: String? = null,
    val totalDebit: Double,
    val totalCredit: Double,
    val createdByUserId: String,
    val lines: List<JournalLineItem>,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isBalanced: Boolean
        get() = Math.abs(totalDebit - totalCredit) < 0.001
}
