package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.JobStageTransitionEntity

@Dao
interface JobStageTransitionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransition(transition: JobStageTransitionEntity)

    @Query("SELECT * FROM job_stage_transitions WHERE jobCardId = :jobCardId ORDER BY transitionAt ASC")
    suspend fun getByJob(jobCardId: String): List<JobStageTransitionEntity>
}
