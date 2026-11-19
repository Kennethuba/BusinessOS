package com.example.businessos.data.model

import java.util.UUID

data class PartItem(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String? = null,
    val name: String,
    val unitPrice: Money,
    val costPrice: Money = Money.ZERO,
    val quantity: Int = 1,
    val isStockDeducted: Boolean = false
) {
    val total: Money get() = unitPrice * quantity
}

data class StageTransition(
    val stage: JobStage,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)

data class SaleLineItem(
    val itemId: String,
    val itemName: String,
    val qty: Int,
    val unitPrice: Money,
    val costPrice: Money = Money.ZERO,
    val priceType: PriceType = PriceType.RETAIL
) {
    val subtotal: Money get() = unitPrice * qty
}

data class CurrentUserSession(
    val userId: String,
    val name: String,
    val phone: String,
    val activeBusinessId: String? = null,
    val activeRole: Role? = null,
    val activeBusinessName: String? = null,
    val isShopEnabled: Boolean = true,
    val isGarageEnabled: Boolean = false,
    val isDemo: Boolean = false
)

data class JournalLineItem(
    val accountId: String,
    val accountCode: String,
    val accountName: String,
    val debit: Money = Money.ZERO,
    val credit: Money = Money.ZERO,
    val narration: String? = null
)

data class InvoiceLineItem(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String? = null,
    val description: String,
    val qty: Int = 1,
    val unitPrice: Money,
    val costPrice: Money = Money.ZERO
) {
    val total: Money get() = unitPrice * qty
}
