package com.example.businessos.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.repository.AuthRepository
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

    fun login(phone: String, pin: String = "", onSuccess: (hasBusiness: Boolean) -> Unit) {
        if (phone.isBlank()) {
            _errorMessage.value = "Please enter your phone number"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.login(phone.trim(), pin.trim())
            _isLoading.value = false
            result.onSuccess {
                val hasBusiness = authRepository.session.value?.activeBusinessId != null
                onSuccess(hasBusiness)
            }.onFailure {
                _errorMessage.value = it.message ?: "Login failed"
            }
        }
    }

    fun signup(name: String, phone: String, pin: String = "1234", onSuccess: () -> Unit) {
        if (name.isBlank() || phone.isBlank()) {
            _errorMessage.value = "Name and phone are required"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.signup(name.trim(), phone.trim(), pin.trim().ifBlank { "1234" })
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
