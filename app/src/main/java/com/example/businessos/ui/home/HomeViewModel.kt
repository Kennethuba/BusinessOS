package com.example.businessos.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.local.entity.BusinessEntity
import com.example.businessos.data.local.entity.CustomerEntity
import com.example.businessos.data.local.entity.ExpenseEntity
import com.example.businessos.data.local.entity.InvoiceEntity
import com.example.businessos.data.local.entity.ItemEntity
import com.example.businessos.data.local.entity.JobCardEntity
import com.example.businessos.data.local.entity.SaleEntity
import com.example.businessos.data.model.BusinessType
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val repository: BusinessOsRepository
) : ViewModel() {

    val session: StateFlow<CurrentUserSession?> = authRepository.session
    val currentBusiness: StateFlow<BusinessEntity?> = repository.observeCurrentBusiness()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val jobCards: StateFlow<List<JobCardEntity>> = repository.observeJobCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleEntity>> = repository.observeSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deniCustomers: StateFlow<List<CustomerEntity>> = repository.observeDeniLedger()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.observeExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items: StateFlow<List<ItemEntity>> = repository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.observeInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    /** Turns the Sales and Job orders features on or off. Existing records are never deleted. */
    fun setModules(useSales: Boolean, useJobOrders: Boolean) {
        val business = currentBusiness.value ?: return
        if (!useSales && !useJobOrders) {
            _message.value = "Keep at least one of Sales or Job orders switched on"
            return
        }
        viewModelScope.launch {
            repository.updateBusinessSettings(
                name = business.name,
                phone = business.phone,
                address = business.address,
                receiptFooter = business.receiptFooter,
                isShopEnabled = useSales,
                isGarageEnabled = useJobOrders,
                allowNegativeStock = business.allowNegativeStock
            ).onFailure { _message.value = it.message ?: "Could not save" }
        }
    }

    fun seedDemoData(type: BusinessType) {
        viewModelScope.launch {
            repository.seedDemoData(type)
        }
    }
}
