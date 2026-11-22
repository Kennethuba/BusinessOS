package com.example.businessos.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.LockCandidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val session: StateFlow<CurrentUserSession?> = authRepository.session

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _candidates = MutableStateFlow<List<LockCandidate>>(emptyList())
    val candidates: StateFlow<List<LockCandidate>> = _candidates.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun login(phone: String, pin: String, onSuccess: (hasBusiness: Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.login(phone, pin)
            _isLoading.value = false
            result.onSuccess {
                onSuccess(authRepository.session.value?.activeBusinessId != null)
            }.onFailure {
                _errorMessage.value = it.message ?: "Could not sign in"
            }
        }
    }

    fun signup(name: String, phone: String, pin: String, confirmPin: String, onSuccess: () -> Unit) {
        if (pin != confirmPin) {
            _errorMessage.value = "The two PINs do not match"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.signup(name, phone, pin)
            _isLoading.value = false
            result.onSuccess { onSuccess() }
                .onFailure { _errorMessage.value = it.message ?: "Could not create the account" }
        }
    }

    fun loadCandidates() {
        viewModelScope.launch { _candidates.value = authRepository.lockCandidates() }
    }

    fun unlock(userId: String, pin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.unlock(userId, pin)
            _isLoading.value = false
            result.onSuccess { onSuccess() }
                .onFailure { _errorMessage.value = it.message ?: "Could not unlock" }
        }
    }

    fun changePin(oldPin: String, newPin: String, confirmPin: String, onSuccess: () -> Unit) {
        val userId = session.value?.userId ?: return
        if (newPin != confirmPin) {
            _errorMessage.value = "The two new PINs do not match"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.changePin(userId, oldPin, newPin)
            _isLoading.value = false
            result.onSuccess {
                _message.value = "PIN changed"
                onSuccess()
            }.onFailure { _errorMessage.value = it.message ?: "Could not change the PIN" }
        }
    }

    fun logout() = authRepository.logout()

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearMessage() {
        _message.value = null
    }
}
