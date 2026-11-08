package com.example.businessos.ui.accounting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.local.entity.*
import com.example.businessos.data.model.*
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import com.example.businessos.sync.SyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AccountingUiState(
    val totalSales: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val totalDeniOwed: Double = 0.0,
    val totalPayables: Double = 0.0,
    val totalAssetValue: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val mpesaSales: Double = 0.0,
    val cashSales: Double = 0.0,
    val bankBalance: Double = 0.0,
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val userRole: Role? = null,
    val businessName: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class AccountingViewModel(
    private val authRepository: AuthRepository,
    private val repository: BusinessOsRepository,
    val syncManager: SyncManager
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

    val bills: StateFlow<List<BillEntity>> = repository.observeBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.observeSuppliers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assets: StateFlow<List<AssetEntity>> = repository.observeAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liabilities: StateFlow<List<LiabilityEntity>> = repository.observeLiabilities()
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
        bills,
        session
    ) { salesList, expensesList, deniList, billList, sess ->
        val totSales = salesList.sumOf { it.total }
        val totExp = expensesList.sumOf { it.amount }
        val net = totSales - totExp
        val deniTot = deniList.sumOf { it.balance }
        val payablesTot = billList.filter { it.status != BillStatus.PAID && it.status != BillStatus.CANCELLED }.sumOf { it.balanceDue }

        val mpesaTot = salesList.filter { it.paymentMethod == PaymentMethod.MPESA }.sumOf { it.total }
        val cashTot = salesList.filter { it.paymentMethod == PaymentMethod.CASH }.sumOf { it.total }

        AccountingUiState(
            totalSales = totSales,
            totalExpenses = totExp,
            netProfit = net,
            totalDeniOwed = deniTot,
            totalPayables = payablesTot,
            mpesaSales = mpesaTot,
            cashSales = cashTot,
            recentExpenses = expensesList,
            userRole = sess?.activeRole,
            businessName = sess?.activeBusinessName ?: "My Business",
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountingUiState(isLoading = true))

    fun refreshReports() {
        viewModelScope.launch {
            repository.generateProfitAndLoss().onSuccess { _pnlReport.value = it }
            repository.generateBalanceSheet().onSuccess { _balanceSheetReport.value = it }
            repository.generateTrialBalance().onSuccess { _trialBalanceReport.value = it }
            repository.generateAccountsReceivableAgeing().onSuccess { _arAgeingReport.value = it }
            repository.generateAccountsPayableAgeing().onSuccess { _apAgeingReport.value = it }
        }
    }

    fun addExpense(category: String, title: String, amount: Double, method: PaymentMethod, note: String) {
        viewModelScope.launch {
            val res = repository.recordExpense(category, title, amount, method, note)
            res.onSuccess {
                _userMessage.value = "Matumizi yamerekodiwa kikamilifu"
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
        customerId: String,
        customerName: String,
        customerPhone: String,
        items: List<InvoiceLineItem>,
        discountAmount: Double,
        taxAmount: Double,
        dueDate: Long,
        notes: String,
        paymentMethod: PaymentMethod
    ) {
        viewModelScope.launch {
            val res = repository.createInvoice(
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                items = items,
                discountAmount = discountAmount,
                taxAmount = taxAmount,
                dueDate = dueDate,
                notes = notes,
                paymentMethod = paymentMethod
            )
            res.onSuccess {
                _userMessage.value = "Ankara ${it.invoiceNumber} imetengenezwa kikamilifu"
                refreshReports()
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun recordInvoicePayment(
        invoiceId: String,
        amount: Double,
        paymentMethod: PaymentMethod,
        reference: String,
        description: String
    ) {
        viewModelScope.launch {
            val res = repository.recordInvoicePayment(invoiceId, amount, paymentMethod, reference, description)
            res.onSuccess {
                _userMessage.value = "Malipo ya ankara yamerekodiwa: KSh ${amount.toInt()}"
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

    fun createBill(
        supplierId: String,
        supplierName: String,
        billNumber: String,
        totalAmount: Double,
        dueDate: Long,
        category: String,
        description: String
    ) {
        viewModelScope.launch {
            val res = repository.createBill(supplierId, supplierName, billNumber, totalAmount, dueDate, category, description)
            res.onSuccess {
                _userMessage.value = "Bili ya msambazaji ${it.billNumber} imehifadhiwa"
                refreshReports()
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun recordBillPayment(
        billId: String,
        amount: Double,
        paymentMethod: PaymentMethod,
        reference: String,
        description: String
    ) {
        viewModelScope.launch {
            val res = repository.recordBillPayment(billId, amount, paymentMethod, reference, description)
            res.onSuccess {
                _userMessage.value = "Malipo ya bili yamerekodiwa: KSh ${amount.toInt()}"
                refreshReports()
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun recordAccountTransfer(
        fromAccountCode: String,
        toAccountCode: String,
        amount: Double,
        reference: String,
        description: String
    ) {
        viewModelScope.launch {
            val res = repository.recordAccountTransfer(fromAccountCode, toAccountCode, amount, reference, description)
            res.onSuccess {
                _userMessage.value = "Uhamisho wa KSh ${amount.toInt()} umekamilika"
                refreshReports()
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun recordAssetPurchase(
        name: String,
        category: String,
        cost: Double,
        usefulLifeYears: Int,
        salvageValue: Double,
        paymentMethod: PaymentMethod,
        notes: String
    ) {
        viewModelScope.launch {
            val res = repository.recordAssetPurchase(name, category, cost, usefulLifeYears, salvageValue, paymentMethod, notes)
            res.onSuccess {
                _userMessage.value = "Kifaa ${it.name} kimerekodiwa kwenye sajili ya rasilimali"
                refreshReports()
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun postAssetDepreciation(assetId: String) {
        viewModelScope.launch {
            val res = repository.postAssetDepreciation(assetId)
            res.onSuccess {
                _userMessage.value = "Uchakavu wa kifaa umerekodiwa kwenye daftari kuu"
                refreshReports()
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun recordLoanDisbursement(
        lenderName: String,
        principalAmount: Double,
        interestRate: Double,
        dueDate: Long,
        depositAccountCode: String,
        notes: String
    ) {
        viewModelScope.launch {
            val res = repository.recordLoanDisbursement(lenderName, principalAmount, interestRate, dueDate, depositAccountCode, notes)
            res.onSuccess {
                _userMessage.value = "Mkopo wa KSh ${principalAmount.toInt()} umewekwa kwenye hesabu"
                refreshReports()
            }.onFailure {
                _userMessage.value = "Hitilafu: ${it.localizedMessage}"
            }
        }
    }

    fun recordLoanRepayment(
        liabilityId: String,
        amount: Double,
        paymentMethod: PaymentMethod,
        notes: String
    ) {
        viewModelScope.launch {
            val res = repository.recordLoanRepayment(liabilityId, amount, paymentMethod, notes)
            res.onSuccess {
                _userMessage.value = "Malipo ya mkopo yamerekodiwa: KSh ${amount.toInt()}"
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
