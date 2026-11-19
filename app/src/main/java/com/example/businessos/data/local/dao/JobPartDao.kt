package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.JobPartEntity

@Dao
interface JobPartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobPart(part: JobPartEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobParts(parts: List<JobPartEntity>)

    @Query("SELECT * FROM job_parts WHERE jobCardId = :jobCardId ORDER BY addedAt ASC")
    suspend fun getByJob(jobCardId: String): List<JobPartEntity>

    @Query("DELETE FROM job_parts WHERE jobCardId = :jobCardId")
    suspend fun deleteByJob(jobCardId: String)

    @Query("DELETE FROM job_parts WHERE id = :partId")
    suspend fun deletePart(partId: String)

    @Query("UPDATE job_parts SET stockDeducted = :deducted WHERE id = :partId")
    suspend fun updateStockDeducted(partId: String, deducted: Boolean)

    @Update
    suspend fun updateJobPart(part: JobPartEntity)
}
