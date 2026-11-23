package com.example.businessos.ui.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.local.entity.PaymentEntity
import com.example.businessos.data.local.entity.SaleEntity
import com.example.businessos.data.local.entity.SaleItemEntity
import com.example.businessos.data.repository.BusinessOsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ReceiptsViewModel(private val repository: BusinessOsRepository) : ViewModel() {

    val sales: StateFlow<List<SaleEntity>> = repository.observeSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.observePayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun saleItems(saleId: String): List<SaleItemEntity> = repository.getSaleItems(saleId)

    suspend fun businessName(): String = repository.businessName()
}
