package com.example.businessos.ui.accounting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.local.entity.*
import com.example.businessos.data.model.*
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AccountingUiState(
    val totalSales: Money = Money.ZERO,
    val totalExpenses: Money = Money.ZERO,
    val netProfit: Money = Money.ZERO,
    val totalDeniOwed: Money = Money.ZERO,
    val totalPayables: Money = Money.ZERO,
    val mpesaSales: Money = Money.ZERO,
    val cashSales: Money = Money.ZERO,
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val userRole: Role? = null,
    val businessName: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class AccountingViewModel(
    private val authRepository: AuthRepository,
    private val repository: BusinessOsRepository
) : ViewModel() {

    val session: StateFlow<CurrentUserSession?> = authRepository.session

    val sales: StateFlow<List<SaleEntity>> = repository.observeSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.observeExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deniCustomers: StateFlow<List<CustomerEntity>> = repository.observeDeniLedger()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountEntity>> = repository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.observeInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.observeSuppliers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.observePayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journalEntries: StateFlow<List<JournalEntryEntity>> = repository.observeJournalEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _pnlReport = MutableStateFlow<ProfitAndLossReport?>(null)
    val pnlReport: StateFlow<ProfitAndLossReport?> = _pnlReport.asStateFlow()

    private val _balanceSheetReport = MutableStateFlow<BalanceSheetReport?>(null)
    val balanceSheetReport: StateFlow<BalanceSheetReport?> = _balanceSheetReport.asStateFlow()

    private val _trialBalanceReport = MutableStateFlow<TrialBalanceReport?>(null)
    val trialBalanceReport: StateFlow<TrialBalanceReport?> = _trialBalanceReport.asStateFlow()

    private val _arAgeingReport = MutableStateFlow<AccountsReceivableAgeingReport?>(null)
    val arAgeingReport: StateFlow<AccountsReceivableAgeingReport?> = _arAgeingReport.asStateFlow()

    private val _apAgeingReport = MutableStateFlow<AccountsPayableAgeingReport?>(null)
    val apAgeingReport: StateFlow<AccountsPayableAgeingReport?> = _apAgeingReport.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
            session.collectLatest { sess ->
                sess?.activeBusinessId?.let { bizId ->
                    repository.ensureDefaultAccounts(bizId)
                    refreshReports()
                }
            }
        }
    }

    val uiState: StateFlow<AccountingUiState> = combine(
        sales,
        expenses,
        deniCustomers,
        session
    ) { salesList, expensesList, deniList, sess ->
        val totSales = salesList.fold(Money.ZERO) { acc, s -> acc + Money(s.totalCents) }
        val totExp = expensesList.fold(Money.ZERO) { acc, e -> acc + Money(e.amountCents) }
        val net = totSales - totExp
        val deni = deniList.fold(Money.ZERO) { acc, c -> acc + Money(c.balanceCents) }
        val mpesa = salesList.filter { it.paymentMethod == PaymentMethod.MPESA }.fold(Money.ZERO) { acc, s -> acc + Money(s.totalCents) }
        val cash = salesList.filter { it.paymentMethod == PaymentMethod.CASH }.fold(Money.ZERO) { acc, s -> acc + Money(s.totalCents) }

        AccountingUiState(
            totalSales = totSales,
            totalExpenses = totExp,
            netProfit = net,
            totalDeniOwed = deni,
            totalPayables = Money.ZERO,
            mpesaSales = mpesa,
            cashSales = cash,
            recentExpenses = expensesList.take(5),
            userRole = sess?.activeRole,
            businessName = sess?.activeBusinessName ?: "BusinessOS"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountingUiState())

    fun refreshReports() {
        viewModelScope.launch {
            repository.generateProfitAndLoss().onSuccess { _pnlReport.value = it }
            repository.generateBalanceSheet().onSuccess { _balanceSheetReport.value = it }
            repository.generateTrialBalance().onSuccess { _trialBalanceReport.value = it }
            repository.generateAccountsReceivableAgeing().onSuccess { _arAgeingReport.value = it }
            repository.generateAccountsPayableAgeing().onSuccess { _apAgeingReport.value = it }
        }
    }

    fun recordExpense(
        category: String,
        title: String,
        amount: Money,
        method: PaymentMethod,
        note: String
    ) {
        viewModelScope.launch {
            val res = repository.recordExpense(category, title, amount, method, note)
            res.onSuccess {
                _userMessage.value = "Matumizi yamehifadhiwa: ${it.expenseNumber}"
                refreshReports()
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            val res = repository.deleteExpense(id)
            res.onSuccess {
                _userMessage.value = "Matumizi yamefutwa"
                refreshReports()
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun createInvoice(
        customerId: String?,
        customerName: String,
        customerPhone: String,
        items: List<InvoiceLineItem>
    ) {
        viewModelScope.launch {
            val res = repository.createInvoice(
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                items = items
            )
            res.onSuccess {
                _userMessage.value = "Ankara ${it.invoiceNumber} imetolewa"
                refreshReports()
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun recordInvoicePayment(
        invoiceId: String,
        amount: Money,
        paymentMethod: PaymentMethod,
        reference: String?,
        description: String?
    ) {
        viewModelScope.launch {
            val res = repository.recordInvoicePayment(invoiceId, amount, paymentMethod, reference, description)
            res.onSuccess {
                _userMessage.value = "Malipo ya ${amount.formatted} yamerekodiwa"
                refreshReports()
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun createSupplier(name: String, phone: String, email: String) {
        viewModelScope.launch {
            val res = repository.createSupplier(name, phone, email)
            res.onSuccess {
                _userMessage.value = "Msambazaji ${it.name} ameongezwa"
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun recordAccountTransfer(
        fromAccountCode: String,
        toAccountCode: String,
        amount: Money,
        reference: String?,
        description: String?
    ) {
        viewModelScope.launch {
            val res = repository.recordAccountTransfer(fromAccountCode, toAccountCode, amount, reference, description)
            res.onSuccess {
                _userMessage.value = "Uhamisho wa ${amount.formatted} umekamilika"
                refreshReports()
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
