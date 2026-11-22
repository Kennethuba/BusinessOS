package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.JobStageTransitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobStageTransitionDao {

    @Query("SELECT * FROM job_stage_transitions WHERE jobCardId = :jobCardId ORDER BY timestamp ASC")
    fun observeByJob(jobCardId: String): Flow<List<JobStageTransitionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransition(transition: JobStageTransitionEntity)

    @Query("SELECT * FROM job_stage_transitions WHERE jobCardId = :jobCardId ORDER BY timestamp ASC")
    suspend fun getByJob(jobCardId: String): List<JobStageTransitionEntity>
}
