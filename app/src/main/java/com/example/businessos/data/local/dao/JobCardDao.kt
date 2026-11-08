package com.example.businessos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.businessos.data.local.entity.JobCardEntity
import com.example.businessos.data.model.JobStage
import kotlinx.coroutines.flow.Flow

@Dao
interface JobCardDao {
    @Query("SELECT * FROM job_cards WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun observeJobCardsForBusiness(businessId: String): Flow<List<JobCardEntity>>

    @Query("SELECT * FROM job_cards WHERE businessId = :businessId AND stage = :stage ORDER BY createdAt DESC")
    fun observeJobCardsByStage(businessId: String, stage: JobStage): Flow<List<JobCardEntity>>

    @Query("SELECT * FROM job_cards WHERE id = :id AND businessId = :businessId LIMIT 1")
    fun observeJobCardById(id: String, businessId: String): Flow<JobCardEntity?>

    @Query("SELECT * FROM job_cards WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getJobCardById(id: String, businessId: String): JobCardEntity?

    @Query("SELECT * FROM job_cards WHERE businessId = :businessId AND UPPER(vehiclePlate) = UPPER(:vehiclePlate) ORDER BY createdAt DESC")
    fun observeVehicleHistory(businessId: String, vehiclePlate: String): Flow<List<JobCardEntity>>

    @Query("SELECT * FROM job_cards WHERE businessId = :businessId AND UPPER(vehiclePlate) = UPPER(:vehiclePlate) ORDER BY createdAt DESC")
    suspend fun getVehicleHistory(businessId: String, vehiclePlate: String): List<JobCardEntity>

    @Query("SELECT COUNT(*) FROM job_cards WHERE businessId = :businessId AND stage != 'INVOICED'")
    fun observeActiveJobCount(businessId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobCard(jobCard: JobCardEntity)

    @Update
    suspend fun updateJobCard(jobCard: JobCardEntity)

    @Query("DELETE FROM job_cards WHERE id = :id AND businessId = :businessId")
    suspend fun deleteJobCard(id: String, businessId: String)

    @Query("SELECT * FROM job_cards WHERE syncStatus = 'PENDING'")
    suspend fun getPendingJobCards(): List<JobCardEntity>
}
