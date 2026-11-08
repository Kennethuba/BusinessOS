package com.example.businessos.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    val syncManager: SyncManager
) : ViewModel() {

    val session: StateFlow<CurrentUserSession?> = authRepository.session

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun login(emailOrPhone: String, onSuccess: (hasBusiness: Boolean) -> Unit) {
        if (emailOrPhone.isBlank()) {
            _errorMessage.value = "Please enter your email or phone number"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.login(emailOrPhone.trim())
            _isLoading.value = false
            result.onSuccess {
                val hasBusiness = authRepository.session.value?.activeBusinessId != null
                onSuccess(hasBusiness)
            }.onFailure {
                _errorMessage.value = it.message ?: "Login failed"
            }
        }
    }

    fun signup(name: String, email: String, phone: String, onSuccess: () -> Unit) {
        if (name.isBlank() || email.isBlank() || phone.isBlank()) {
            _errorMessage.value = "All fields are required"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.signup(name.trim(), email.trim(), phone.trim())
            _isLoading.value = false
            result.onSuccess {
                onSuccess()
            }.onFailure {
                _errorMessage.value = it.message ?: "Signup failed"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
