package com.example.businessos.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.model.SubscriptionPlan
import com.example.businessos.data.model.Role
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SubscriptionUiState(
    val currentPlan: SubscriptionPlan = SubscriptionPlan.STARTER,
    val isLoading: Boolean = false,
    val upgradeSuccess: Boolean = false,
    val error: String? = null,
    val userRole: Role? = null,
    val businessName: String = "",
    val memberCount: Int = 0,
    val jobCardCount: Int = 0,
    val saleCount: Int = 0
)

class SubscriptionViewModel(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessOsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadCurrentSubscription()
    }

    private fun loadCurrentSubscription() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val session = authRepository.session.value
                val role = session?.activeRole
                val bid = session?.activeBusinessId

                val business = if (bid != null) {
                    businessRepository.getBusinessById(bid)
                } else null

                val members = if (bid != null) {
                    businessRepository.getMemberCount(bid)
                } else 0

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentPlan = business?.subscriptionPlan ?: SubscriptionPlan.STARTER,
                        userRole = role,
                        businessName = business?.name ?: "",
                        memberCount = members,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun upgradePlan(newPlan: SubscriptionPlan) {
        val currentRole = _uiState.value.userRole
        if (currentRole != Role.OWNER) {
            _uiState.update { it.copy(error = "Only the business owner can change the subscription plan.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                businessRepository.updateSubscriptionPlan(newPlan)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentPlan = newPlan,
                        upgradeSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearUpgradeSuccess() {
        _uiState.update { it.copy(upgradeSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
