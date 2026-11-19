package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "journal_lines",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["entryId"]),
        Index(value = ["accountId"])
    ]
)
data class JournalLineEntity(
    @PrimaryKey val id: String,
    val entryId: String,
    val accountId: String,
    val debitCents: Long = 0L,
    val creditCents: Long = 0L,
    val notes: String = ""
)
