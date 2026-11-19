package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.AccountCategory
import com.example.businessos.data.model.AccountSubCategory
import com.example.businessos.data.model.Money

@Entity(
    tableName = "accounts",
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
        Index(value = ["businessId", "code"], unique = true)
    ]
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val code: String,
    val name: String,
    val category: AccountCategory,
    val subCategory: AccountSubCategory,
    val isSystemAccount: Boolean = true,
    val balanceCents: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val balance: Double get() = balanceCents / 100.0
    val balanceMoney: Money get() = Money(balanceCents)
}
