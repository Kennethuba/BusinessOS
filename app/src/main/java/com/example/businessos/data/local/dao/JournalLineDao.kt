package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.JournalLineEntity

@Dao
interface JournalLineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(line: JournalLineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<JournalLineEntity>)

    @Query("SELECT * FROM journal_lines WHERE entryId = :entryId ORDER BY rowid ASC")
    suspend fun getByEntry(entryId: String): List<JournalLineEntity>
}
