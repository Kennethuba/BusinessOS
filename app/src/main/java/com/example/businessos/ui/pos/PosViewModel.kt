package com.example.businessos.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.local.entity.CustomerEntity
import com.example.businessos.data.local.entity.ItemEntity
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.PriceType
import com.example.businessos.data.model.Role
import com.example.businessos.data.model.SaleLineItem
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import com.example.businessos.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItem(
    val item: ItemEntity,
    var qty: Int = 1,
    var priceType: PriceType = PriceType.RETAIL,
    var customPrice: Double? = null
) {
    val unitPrice: Double
        get() = customPrice ?: if (priceType == PriceType.RETAIL) item.retailPrice else item.wholesalePrice

    val subtotal: Double
        get() = qty * unitPrice
}

class PosViewModel(
    private val authRepository: AuthRepository,
    private val repository: BusinessOsRepository,
    val syncManager: SyncManager
) : ViewModel() {

    val session: StateFlow<CurrentUserSession?> = authRepository.session

    val items: StateFlow<List<ItemEntity>> = repository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.observeCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(item: ItemEntity) {
        val currentList = _cart.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.item.id == item.id }
        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(qty = existing.qty + 1)
        } else {
            currentList.add(CartItem(item = item, qty = 1))
        }
        _cart.value = currentList
    }

    fun updateCartQty(itemId: String, delta: Int) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.item.id == itemId }
        if (index >= 0) {
            val current = currentList[index]
            val newQty = current.qty + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = current.copy(qty = newQty)
            }
            _cart.value = currentList
        }
    }

    fun togglePriceType(itemId: String) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.item.id == itemId }
        if (index >= 0) {
            val current = currentList[index]
            val nextType = if (current.priceType == PriceType.RETAIL) PriceType.WHOLESALE else PriceType.RETAIL
            currentList[index] = current.copy(priceType = nextType, customPrice = null)
            _cart.value = currentList
        }
    }

    fun overrideItemPrice(itemId: String, newPrice: Double) {
        val userRole = session.value?.activeRole ?: Role.STAFF
        if (userRole == Role.STAFF) {
            _errorMessage.value = "Staff members are not allowed to override prices."
            return
        }
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.item.id == itemId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(customPrice = newPrice)
            _cart.value = currentList
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun addItemToInventory(
        name: String,
        retail: Double,
        wholesale: Double,
        stock: Int,
        minStockLevel: Int = 3,
        onItemCreated: ((ItemEntity) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.createItem(name, retail, wholesale, stock, minStockLevel)
            result.onSuccess {
                _successMessage.value = "Bidhaa ${it.name} imeongezwa dukani"
                onItemCreated?.invoke(it)
            }.onFailure {
                _errorMessage.value = it.message
            }
        }
    }

    fun checkout(
        paymentMethod: PaymentMethod,
        customerId: String? = null,
        mpesaCode: String? = null,
        onComplete: () -> Unit
    ) {
        if (_cart.value.isEmpty()) {
            _errorMessage.value = "Cart is empty"
            return
        }

        val hasPriceOverride = _cart.value.any { it.customPrice != null }
        val lineItems = _cart.value.map {
            SaleLineItem(
                itemId = it.item.id,
                itemName = it.item.name,
                qty = it.qty,
                priceUsed = it.unitPrice,
                priceType = it.priceType
            )
        }

        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.recordSale(
                items = lineItems,
                paymentMethod = paymentMethod,
                customerId = customerId,
                mpesaCode = mpesaCode,
                priceOverridden = hasPriceOverride
            )
            result.onSuccess {
                clearCart()
                _successMessage.value = "Sale recorded successfully (KSh ${it.total.toInt()})"
                onComplete()
            }.onFailure {
                _errorMessage.value = it.message ?: "Failed to complete checkout"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    suspend fun updateStockItemCount(itemId: String, physicalCount: Int) {
        repository.updateStockItemCount(itemId, physicalCount, "Ukaguzi wa akiba (Stock Take)")
    }
}
