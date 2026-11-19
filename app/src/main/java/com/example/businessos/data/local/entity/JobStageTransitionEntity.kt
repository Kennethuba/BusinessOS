package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "job_stage_transitions",
    foreignKeys = [
        ForeignKey(
            entity = JobCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["jobCardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["jobCardId"]),
        Index(value = ["jobCardId", "timestamp"])
    ]
)
data class JobStageTransitionEntity(
    @PrimaryKey val id: String,
    val jobCardId: String,
    val fromStage: String,
    val toStage: String,
    val notes: String = "",
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)
