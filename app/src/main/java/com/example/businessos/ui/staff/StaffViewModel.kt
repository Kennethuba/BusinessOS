package com.example.businessos.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.local.dao.MemberWithUser
import com.example.businessos.data.local.entity.BusinessEntity
import com.example.businessos.data.local.entity.WagePaymentEntity
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.model.Role
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import com.example.businessos.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StaffViewModel(
    private val authRepository: AuthRepository,
    private val repository: BusinessOsRepository,
    val syncManager: SyncManager
) : ViewModel() {

    val session: StateFlow<CurrentUserSession?> = authRepository.session
    val currentBusiness: StateFlow<BusinessEntity?> = repository.observeCurrentBusiness()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val staffMembers: StateFlow<List<MemberWithUser>> = repository.observeStaffMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wagePayments: StateFlow<List<WagePaymentEntity>> = repository.observeWagePayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun inviteStaff(name: String, email: String, phone: String, role: Role) {
        if (name.isBlank() || email.isBlank()) {
            _errorMessage.value = "Name and Email are required"
            return
        }
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.inviteOrAddStaff(name, email, phone, role)
            result.onSuccess {
                _successMessage.value = "Staff member $name added successfully"
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun updateRole(userId: String, newRole: Role) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.updateStaffRole(userId, newRole)
            result.onSuccess {
                _successMessage.value = "Staff role updated to ${newRole.name}"
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun removeStaff(userId: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.removeStaff(userId)
            result.onSuccess {
                _successMessage.value = "Staff member removed"
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun recordWagePayment(
        staffUserId: String,
        staffName: String,
        amount: Double,
        period: String,
        paymentMethod: String,
        mpesaCode: String? = null,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.recordWagePayment(
                staffUserId, staffName, amount, period, paymentMethod, mpesaCode, notes
            )
            result.onSuccess {
                _successMessage.value = "Mshahara wa KSh ${amount.toInt()} kwa $staffName umerekodiwa"
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
