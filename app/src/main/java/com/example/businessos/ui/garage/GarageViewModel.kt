package com.example.businessos.ui.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.local.dao.MemberWithUser
import com.example.businessos.data.local.entity.JobCardEntity
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.model.JobOrderKind
import com.example.businessos.data.model.JobStage
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PartItem
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GarageViewModel(
    private val authRepository: AuthRepository,
    private val repository: BusinessOsRepository
) : ViewModel() {

    val session: StateFlow<CurrentUserSession?> = authRepository.session

    val jobCards: StateFlow<List<JobCardEntity>> = repository.observeJobCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staffMembers: StateFlow<List<MemberWithUser>> = repository.observeStaffMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedStageFilter = MutableStateFlow<JobStage?>(null)
    val selectedStageFilter: StateFlow<JobStage?> = _selectedStageFilter.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun setStageFilter(stage: JobStage?) {
        _selectedStageFilter.value = stage
    }

    fun createJobCard(
        kind: JobOrderKind,
        reference: String,
        customerName: String,
        customerPhone: String,
        description: String,
        onSuccess: (JobCardEntity) -> Unit
    ) {
        val isClientOrder = kind == JobOrderKind.CLIENT_ORDER
        if (reference.isBlank()) {
            _errorMessage.value = "Please enter what the job order is for"
            return
        }
        if (isClientOrder && (customerName.isBlank() || customerPhone.isBlank())) {
            _errorMessage.value = "Customer name and phone are required"
            return
        }

        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.createJobCard(
                vehiclePlate = reference,
                vehicleMake = "",
                vehicleModel = "",
                customerName = if (isClientOrder) customerName else "In-house",
                customerPhone = if (isClientOrder) customerPhone else "",
                issueDescription = description,
                kind = kind
            )
            result.onSuccess {
                _successMessage.value = "Job order ${it.jobNumber} created"
                onSuccess(it)
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun advanceStage(jobCardId: String, note: String? = null) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.advanceJobStage(jobCardId, note)
            result.onSuccess { stage ->
                _successMessage.value = "Moved to ${stage?.title ?: "Next Stage"}"
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun reopenStage(jobCardId: String, targetStage: JobStage, reason: String) {
        if (reason.isBlank()) {
            _errorMessage.value = "Reason is required to reopen/rollback stages"
            return
        }
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.reopenJobStage(jobCardId, targetStage, reason)
            result.onSuccess {
                _successMessage.value = "Job Card reopened to ${targetStage.title} (Logged to Audit)"
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun addPartToJob(jobCardId: String, currentParts: List<PartItem>, partName: String, cost: Money) {
        if (partName.isBlank() || cost < Money.ZERO) {
            _errorMessage.value = "Valid part name and cost are required"
            return
        }
        val newParts = currentParts + PartItem(name = partName.trim(), unitPrice = cost)
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.updateJobParts(jobCardId, newParts)
            result.onSuccess {
                _successMessage.value = "Part added to job"
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun removePartFromJob(jobCardId: String, currentParts: List<PartItem>, partId: String) {
        val newParts = currentParts.filter { it.id != partId }
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.updateJobParts(jobCardId, newParts)
            result.onSuccess {
                _successMessage.value = "Part removed from job"
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
