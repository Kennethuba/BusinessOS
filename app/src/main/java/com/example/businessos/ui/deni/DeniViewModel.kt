package com.example.businessos.ui.deni

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.local.entity.CustomerEntity
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeniViewModel(
    private val authRepository: AuthRepository,
    private val repository: BusinessOsRepository
) : ViewModel() {

    val session: StateFlow<CurrentUserSession?> = authRepository.session

    val customers: StateFlow<List<CustomerEntity>> = repository.observeCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deniList: StateFlow<List<CustomerEntity>> = repository.observeDeniLedger()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun addCustomer(
        name: String,
        phone: String,
        initialCredit: Money = Money.ZERO,
        dueDate: Long? = null,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.createCustomer(name, phone, initialCredit, dueDate, notes)
            result.onSuccess {
                _successMessage.value = "Customer ${it.name} created"
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun updateCustomerReminder(customerId: String, dueDate: Long?, notes: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.updateCustomerReminder(customerId, dueDate, notes)
            result.onSuccess {
                _successMessage.value = "Tarehe ya makubaliano imehifadhiwa"
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun recordReminderSent(customerId: String, customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomerReminder(customerId, customer.dueDate, customer.notes)
        }
    }

    fun recordPayment(
        customerId: String,
        amountPaid: Money,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        mpesaCode: String? = null
    ) {
        if (amountPaid <= Money.ZERO) {
            _errorMessage.value = "Amount paid must be greater than 0"
            return
        }
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.recordDeniPayment(customerId, amountPaid, paymentMethod, mpesaCode)
            result.onSuccess {
                _successMessage.value = "Malipo ya ${amountPaid.formatted} yamehifadhiwa"
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
