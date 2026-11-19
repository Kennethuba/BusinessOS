package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.BusinessSide
import com.example.businessos.data.model.JournalSourceType
import com.example.businessos.data.model.Money

@Entity(
    tableName = "journal_entries",
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
        Index(value = ["businessId", "entryNumber"], unique = true),
        Index(value = ["businessId", "date"]),
        Index(value = ["sourceId"])
    ]
)
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val entryNumber: String,
    val date: Long,
    val description: String,
    val sourceType: JournalSourceType,
    val sourceId: String? = null,
    val totalDebitCents: Long,
    val totalCreditCents: Long,
    val side: BusinessSide = BusinessSide.SHOP,
    val isReversed: Boolean = false,
    val reversedByEntryId: String? = null,
    val createdByUserId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isBalanced: Boolean get() = totalDebitCents == totalCreditCents

    val totalDebit: Double get() = totalDebitCents / 100.0
    val totalCredit: Double get() = totalCreditCents / 100.0

    val debitMoney: Money get() = Money(totalDebitCents)
    val creditMoney: Money get() = Money(totalCreditCents)
}
