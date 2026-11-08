package com.example.businessos.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.local.entity.BusinessEntity
import com.example.businessos.data.local.entity.CustomerEntity
import com.example.businessos.data.local.entity.JobCardEntity
import com.example.businessos.data.local.entity.SaleEntity
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import com.example.businessos.sync.SyncManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val repository: BusinessOsRepository,
    val syncManager: SyncManager
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

    val expenses: StateFlow<List<com.example.businessos.data.local.entity.ExpenseEntity>> = repository.observeExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshSync() {
        viewModelScope.launch {
            syncManager.triggerSync()
        }
    }

    fun seedDemoData(type: com.example.businessos.data.model.BusinessType) {
        viewModelScope.launch {
            repository.seedDemoData(type)
        }
    }
}
