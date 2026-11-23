package com.example.businessos.ui.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.model.Money
import com.example.businessos.data.local.entity.BusinessEntity
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkspaceViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val session: StateFlow<CurrentUserSession?> = authRepository.session

    val businesses: StateFlow<List<BusinessEntity>> = session.flatMapLatest { userSession ->
        if (userSession != null) {
            authRepository.observeUserBusinesses(userSession.userId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    fun switchWorkspace(businessId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = authRepository.switchWorkspace(businessId)
            result.onSuccess {
                onSuccess()
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to switch workspace"
            }
        }
    }

    fun createBusiness(
        name: String,
        isShopEnabled: Boolean,
        isGarageEnabled: Boolean,
        openingCash: Money = Money.ZERO,
        openingMpesa: Money = Money.ZERO,
        onSuccess: (String) -> Unit
    ) {
        if (name.isBlank()) {
            _errorMessage.value = "Business name cannot be empty"
            return
        }
        viewModelScope.launch {
            _isCreating.value = true
            _errorMessage.value = null
            val result = authRepository.createBusiness(
                name = name.trim(),
                isShopEnabled = isShopEnabled,
                isGarageEnabled = isGarageEnabled,
                openingCash = openingCash,
                openingMpesa = openingMpesa
            )
            _isCreating.value = false
            result.onSuccess {
                onSuccess(it.id)
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to create business"
            }
        }
    }

    fun deleteBusiness(businessId: String, typedName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _errorMessage.value = null
            authRepository.deleteBusiness(businessId, typedName)
                .onSuccess { onSuccess() }
                .onFailure { _errorMessage.value = it.message ?: "Could not delete business" }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun logout(onSuccess: () -> Unit) {
        authRepository.logout()
        onSuccess()
    }
}
