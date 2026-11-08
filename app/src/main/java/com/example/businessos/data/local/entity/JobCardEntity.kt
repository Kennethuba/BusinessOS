package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.JobStage
import com.example.businessos.data.model.PartItem
import com.example.businessos.data.model.StageTransition
import com.example.businessos.data.model.SyncStatus

@Entity(
    tableName = "job_cards",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["vehiclePlate"]),
        Index(value = ["businessId", "stage"])
    ]
)
data class JobCardEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val vehiclePlate: String,
    val customerName: String,
    val customerPhone: String,
    val description: String,
    val stage: JobStage = JobStage.INTAKE,
    val assignedStaffId: String? = null,
    val partsList: List<PartItem> = emptyList(),
    val estimateTotal: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val stageHistory: List<StageTransition> = listOf(StageTransition(JobStage.INTAKE)),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val updatedAt: Long = System.currentTimeMillis()
)
