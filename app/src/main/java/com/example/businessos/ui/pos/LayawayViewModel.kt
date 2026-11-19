package com.example.businessos.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.local.entity.LayawayEntity
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LayawayViewModel(
    private val authRepository: AuthRepository,
    private val repository: BusinessOsRepository
) : ViewModel() {

    val session: StateFlow<CurrentUserSession?> = authRepository.session

    val layaways: StateFlow<List<LayawayEntity>> = repository.observeLayaways()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun createLayaway(
        customerName: String,
        customerPhone: String,
        description: String,
        totalAmount: Money,
        depositPaid: Money,
        dueDate: Long
    ) {
        repository.createLayaway(customerName, customerPhone, description, totalAmount, depositPaid, dueDate)
    }

    suspend fun recordPayment(layawayId: String, amount: Money, method: PaymentMethod) {
        repository.recordLayawayPayment(layawayId, amount, method)
    }
}
