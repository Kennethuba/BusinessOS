package com.example.businessos.data.model

data class PartItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val cost: Double
)

data class StageTransition(
    val stage: JobStage,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)

data class SaleLineItem(
    val itemId: String,
    val itemName: String,
    val qty: Int,
    val priceUsed: Double,
    val priceType: PriceType
) {
    val subtotal: Double get() = qty * priceUsed
}

data class CurrentUserSession(
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val activeBusinessId: String? = null,
    val activeRole: Role? = null,
    val activeBusinessName: String? = null,
    val activeBusinessType: BusinessType? = null
)

data class JournalLineItem(
    val accountId: String,
    val accountCode: String,
    val accountName: String,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val narration: String? = null
)

data class InvoiceLineItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val description: String,
    val qty: Int = 1,
    val unitPrice: Double,
    val total: Double = qty * unitPrice
)
