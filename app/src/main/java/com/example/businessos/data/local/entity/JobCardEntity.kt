package com.example.businessos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.businessos.data.model.JobStage
import com.example.businessos.data.model.Money

@Entity(
    tableName = "job_cards",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["businessId", "jobNumber"], unique = true),
        Index(value = ["vehiclePlate"]),
        Index(value = ["businessId", "stage"]),
        Index(value = ["customerId"]),
        Index(value = ["isVoided"])
    ]
)
data class JobCardEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val jobNumber: String,
    val vehiclePlate: String,
    val vehicleMake: String = "",
    val vehicleModel: String = "",
    val customerId: String? = null,
    val customerName: String,
    val customerPhone: String,
    val issueDescription: String = "",
    val stage: JobStage = JobStage.INTAKE,
    val laborCostCents: Long = 0L,
    val partsCostCents: Long = 0L,
    val estimateTotalCents: Long = 0L,
    val targetDeliveryDate: Long? = null,
    val invoiceId: String? = null,
    val assignedTechnicianId: String? = null,
    val isVoided: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val description: String get() = issueDescription
    val assignedStaffId: String? get() = assignedTechnicianId

    val laborCost: Double get() = laborCostCents / 100.0
    val partsCost: Double get() = partsCostCents / 100.0
    val estimateTotal: Double get() = estimateTotalCents / 100.0

    val laborCostMoney: Money get() = Money(laborCostCents)
    val partsCostMoney: Money get() = Money(partsCostCents)
    val estimateTotalMoney: Money get() = Money(estimateTotalCents)
}
