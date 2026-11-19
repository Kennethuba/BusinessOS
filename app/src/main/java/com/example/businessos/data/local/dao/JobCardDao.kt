package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.JobCardEntity
import com.example.businessos.data.model.JobStage
import kotlinx.coroutines.flow.Flow

@Dao
interface JobCardDao {
    @Query("SELECT * FROM job_cards WHERE businessId = :businessId AND isVoided = 0 ORDER BY createdAt DESC")
    fun observeJobCardsForBusiness(businessId: String): Flow<List<JobCardEntity>>

    @Query("SELECT * FROM job_cards WHERE businessId = :businessId AND stage = :stage AND isVoided = 0 ORDER BY createdAt DESC")
    fun observeJobCardsByStage(businessId: String, stage: JobStage): Flow<List<JobCardEntity>>

    @Query("SELECT * FROM job_cards WHERE id = :id AND businessId = :businessId LIMIT 1")
    fun observeJobCardById(id: String, businessId: String): Flow<JobCardEntity?>

    @Query("SELECT * FROM job_cards WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getJobCardById(id: String, businessId: String): JobCardEntity?

    @Query("SELECT * FROM job_cards WHERE businessId = :businessId AND UPPER(vehiclePlate) = UPPER(:vehiclePlate) ORDER BY createdAt DESC")
    fun observeVehicleHistory(businessId: String, vehiclePlate: String): Flow<List<JobCardEntity>>

    @Query("SELECT * FROM job_cards WHERE businessId = :businessId AND UPPER(vehiclePlate) = UPPER(:vehiclePlate) ORDER BY createdAt DESC")
    suspend fun getVehicleHistory(businessId: String, vehiclePlate: String): List<JobCardEntity>

    @Query("SELECT COUNT(*) FROM job_cards WHERE businessId = :businessId AND stage != 'INVOICED' AND isVoided = 0")
    fun observeActiveJobCount(businessId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobCard(jobCard: JobCardEntity)

    @Update
    suspend fun updateJobCard(jobCard: JobCardEntity)

    @Query("UPDATE job_cards SET stage = :stage, updatedAt = :updatedAt WHERE id = :id AND businessId = :businessId")
    suspend fun updateStage(id: String, businessId: String, stage: JobStage, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE job_cards SET invoiceId = :invoiceId, updatedAt = :updatedAt WHERE id = :id AND businessId = :businessId")
    suspend fun linkInvoice(id: String, businessId: String, invoiceId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE job_cards SET isVoided = 1, updatedAt = :updatedAt WHERE id = :id AND businessId = :businessId")
    suspend fun markVoided(id: String, businessId: String, updatedAt: Long = System.currentTimeMillis())
}
