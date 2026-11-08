package com.example.businessos.data.assistant

import java.util.UUID

enum class AssistantCategory(val titleSwahili: String, val titleEnglish: String) {
    APP_GUIDE("Mwongozo wa App", "App Guidance"),
    BUSINESS_DATA("Takwimu za Biashara", "Business Data"),
    BUSINESS_ANALYST("Mchambuzi wa Biashara", "Business Analyst"),
    ACTION("Kitendo cha Haraka", "Quick Action"),
    EXTERNAL_KNOWLEDGE("KRA & Maarifa ya Nje", "External & Market Info")
}

sealed class AssistantActionPayload {
    data class WhatsAppReminder(
        val customerName: String,
        val customerPhone: String,
        val balance: Double,
        val dueDate: Long? = null
    ) : AssistantActionPayload()

    data class Navigate(
        val screenRoute: String,
        val label: String
    ) : AssistantActionPayload()

    data class StageExpense(
        val amount: Double,
        val category: String,
        val description: String
    ) : AssistantActionPayload()
}

data class AssistantMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val category: AssistantCategory = AssistantCategory.APP_GUIDE,
    val actionPayload: AssistantActionPayload? = null,
    val actionExecuted: Boolean = false,
    val isError: Boolean = false
)

data class DebtorSummary(
    val name: String,
    val phone: String,
    val balance: Double,
    val daysOverdue: Int = 0
)

data class BusinessSnapshot(
    val businessName: String,
    val businessType: String,
    val totalSalesToday: Double,
    val totalSalesAllTime: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val profitMarginPct: Double,
    val totalDeniReceivable: Double,
    val debtorCount: Int,
    val topDebtors: List<DebtorSummary>,
    val cashOnHand: Double,
    val bankMpesaBalance: Double,
    val totalBillsPayable: Double,
    val overdueBillsCount: Int,
    val activeJobsCount: Int,
    val inventoryCount: Int
)
