package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.JournalEntryEntity
import com.example.businessos.data.model.BusinessSide
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries WHERE businessId = :businessId ORDER BY date DESC, createdAt DESC")
    fun observeJournalEntries(businessId: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE businessId = :businessId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeJournalEntriesBetween(businessId: String, startDate: Long, endDate: Long): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE businessId = :businessId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getJournalEntriesBetween(businessId: String, startDate: Long, endDate: Long): List<JournalEntryEntity>

    @Query("SELECT * FROM journal_entries WHERE businessId = :businessId AND side = :side AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getJournalEntriesBySide(businessId: String, side: BusinessSide, startDate: Long, endDate: Long): List<JournalEntryEntity>

    @Query("SELECT * FROM journal_entries WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getEntryById(id: String, businessId: String): JournalEntryEntity?

    @Query("SELECT COUNT(*) FROM journal_entries WHERE businessId = :businessId")
    suspend fun countEntries(businessId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity)

    /** Journal entries are never hard-deleted; mark reversed instead. */
    @Query("UPDATE journal_entries SET isReversed = 1, reversedByEntryId = :reversalId WHERE id = :id")
    suspend fun markReversed(id: String, reversalId: String)
}
