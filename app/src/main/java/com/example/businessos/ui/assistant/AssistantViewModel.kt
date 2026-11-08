package com.example.businessos.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.assistant.AssistantActionPayload
import com.example.businessos.data.assistant.AssistantCategory
import com.example.businessos.data.assistant.AssistantEngine
import com.example.businessos.data.assistant.AssistantMessage
import com.example.businessos.data.assistant.BusinessSnapshot
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import com.example.businessos.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class AssistantUiState(
    val messages: List<AssistantMessage> = emptyList(),
    val isLoading: Boolean = false,
    val businessSnapshot: BusinessSnapshot? = null,
    val geminiApiKey: String = "",
    val error: String? = null
)

class AssistantViewModel(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessOsRepository,
    val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        // Post Initial Welcoming Greeting from Mshauri
        val initialGreeting = AssistantMessage(
            text = """
                👋 **Habari Mheshimiwa! Mimi ni Mshauri wako wa Biashara (BusinessOS AI Copilot).**
                
                Niko hapa kukusaidia kusimamia mauzo, kudhibiti madeni (Deni), kupanga matumizi, na kutoa ushauri wa kukuza biashara yako.
                
                Unaweza kuchagua swali hapa chini au uandike swali lako kwa Kiswahili au Kiingereza:
            """.trimIndent(),
            isUser = false,
            category = AssistantCategory.APP_GUIDE
        )
        _uiState.value = _uiState.value.copy(messages = listOf(initialGreeting))

        observeBusinessData()
    }

    private fun observeBusinessData() {
        val sessionFlow = authRepository.session
        val salesFlow = businessRepository.observeSales()
        val expensesFlow = businessRepository.observeExpenses()
        val deniFlow = businessRepository.observeDeniLedger()
        val accountsFlow = businessRepository.observeAccounts()

        // Combine core streams into snapshot
        combine(
            sessionFlow,
            salesFlow,
            expensesFlow,
            deniFlow,
            accountsFlow
        ) { session, sales, expenses, customers, accounts ->
            val businessName = session?.activeBusinessName ?: "Biashara Yangu"
            val businessType = session?.activeBusinessType?.name ?: "GENERAL"
            AssistantEngine.buildSnapshot(
                businessName = businessName,
                businessType = businessType,
                sales = sales,
                expenses = expenses,
                customers = customers,
                invoices = emptyList(),
                bills = emptyList(),
                accounts = accounts,
                jobCards = emptyList()
            )
        }.onEach { snapshot ->
            _uiState.value = _uiState.value.copy(businessSnapshot = snapshot)
        }.launchIn(viewModelScope)
    }

    fun sendMessage(query: String) {
        if (query.isBlank()) return

        val userMessage = AssistantMessage(
            text = query.trim(),
            isUser = true
        )

        val updatedMessages = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(messages = updatedMessages, isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val snapshot = _uiState.value.businessSnapshot ?: BusinessSnapshot(
                    businessName = authRepository.session.value?.activeBusinessName ?: "Business",
                    businessType = "GENERAL",
                    totalSalesToday = 0.0,
                    totalSalesAllTime = 0.0,
                    totalExpenses = 0.0,
                    netProfit = 0.0,
                    profitMarginPct = 0.0,
                    totalDeniReceivable = 0.0,
                    debtorCount = 0,
                    topDebtors = emptyList(),
                    cashOnHand = 0.0,
                    bankMpesaBalance = 0.0,
                    totalBillsPayable = 0.0,
                    overdueBillsCount = 0,
                    activeJobsCount = 0,
                    inventoryCount = 0
                )

                val response = AssistantEngine.processQuery(
                    query = query,
                    snapshot = snapshot,
                    geminiApiKey = _uiState.value.geminiApiKey.ifBlank { null }
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + response,
                    isLoading = false
                )
            } catch (e: Exception) {
                val errorMsg = AssistantMessage(
                    text = "Samahani, kumetokea hitilafu kidogo wakati wa kuchakata swali lako: ${e.localizedMessage}",
                    isUser = false,
                    isError = true
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMsg,
                    isLoading = false
                )
            }
        }
    }

    fun executeStagedExpense(messageId: String, payload: AssistantActionPayload.StageExpense) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = businessRepository.recordExpense(
                category = payload.category,
                title = payload.description,
                amount = payload.amount,
                paymentMethod = PaymentMethod.CASH,
                note = "Recorded via Mshauri AI Assistant"
            )

            result.onSuccess {
                val updated = _uiState.value.messages.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(
                            actionExecuted = true,
                            text = msg.text + "\n\n✅ **Matumizi yamerekodiwa kikamilifu kwenye Daftari la Hesabu!**"
                        )
                    } else msg
                }
                _uiState.value = _uiState.value.copy(messages = updated, isLoading = false)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Kushindwa kurekodi matumizi: ${err.localizedMessage}"
                )
            }
        }
    }

    fun setGeminiApiKey(key: String) {
        _uiState.value = _uiState.value.copy(geminiApiKey = key.trim())
    }

    fun clearChat() {
        val initialGreeting = AssistantMessage(
            text = "Gumzo limesafishwa. Uliza swali lolote kuhusu mauzo, deni, ushauri, au matumizi ya mfumo:",
            isUser = false,
            category = AssistantCategory.APP_GUIDE
        )
        _uiState.value = _uiState.value.copy(messages = listOf(initialGreeting))
    }
}
