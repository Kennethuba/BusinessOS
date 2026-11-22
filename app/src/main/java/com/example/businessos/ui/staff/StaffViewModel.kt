package com.example.businessos.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.local.dao.MemberWithUser
import com.example.businessos.data.local.entity.BusinessEntity
import com.example.businessos.data.local.entity.WagePaymentEntity
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.Role
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StaffViewModel(
    private val authRepository: AuthRepository,
    private val repository: BusinessOsRepository
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

    fun inviteStaff(name: String, phone: String, role: Role, pin: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.inviteOrAddStaff(name, phone, role, pin)
            result.onSuccess {
                _successMessage.value = "$name was added. Share the PIN with them privately."
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun resetPin(userId: String, newPin: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            repository.resetStaffPin(userId, newPin)
                .onSuccess { _successMessage.value = "PIN changed. Share it with them privately." }
                .onFailure { _errorMessage.value = it.message }
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
        amount: Money,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.recordWagePayment(
                staffUserId = staffUserId,
                staffName = staffName,
                amount = amount,
                paymentMethod = paymentMethod,
                notes = notes
            )
            result.onSuccess {
                _successMessage.value = "Mshahara wa ${amount.formatted} kwa $staffName umerekodiwa"
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
