package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.DocumentCounterEntity
import com.example.businessos.data.model.DocumentType

@Dao
interface DocumentCounterDao {

    @Query("SELECT * FROM document_counters WHERE businessId = :businessId AND documentType = :type LIMIT 1")
    suspend fun getCounter(businessId: String, type: String): DocumentCounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCounter(counter: DocumentCounterEntity)

    /**
     * Atomically increments the counter and returns the new formatted document number.
     * Must be called from within a withTransaction block.
     */
    @Transaction
    suspend fun nextNumber(businessId: String, type: DocumentType, prefix: String): String {
        val existing = getCounter(businessId, type.name)
        val newSeq = (existing?.currentValue ?: 0L) + 1L
        upsertCounter(
            DocumentCounterEntity(
                businessId = businessId,
                documentType = type.name,
                currentValue = newSeq
            )
        )
        return "$prefix${newSeq.toString().padStart(5, '0')}"
    }
}
