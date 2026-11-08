package com.example.businessos.data.model

enum class BusinessType {
    GARAGE,
    STALL
}

enum class Role {
    OWNER,
    MANAGER,
    ACCOUNTANT,
    CASHIER,
    STAFF,
    VIEWER
}

enum class JobStage(val title: String, val order: Int) {
    INTAKE("Intake", 1),
    DIAGNOSIS("Diagnosis", 2),
    PARTS_ASSIGNED("Parts Assigned", 3),
    AWAITING_APPROVAL("Awaiting Approval", 4),
    IN_PROGRESS("In Progress", 5),
    COMPLETED("Completed", 6),
    INVOICED("Invoiced", 7);

    fun next(): JobStage? = entries.firstOrNull { it.order == this.order + 1 }
    fun prev(): JobStage? = entries.firstOrNull { it.order == this.order - 1 }
}

enum class PaymentMethod {
    CASH,
    MPESA,
    BANK,
    SPLIT,
    CREDIT
}

enum class AccountCategory {
    ASSET,
    LIABILITY,
    EQUITY,
    INCOME,
    EXPENSE
}

enum class AccountSubCategory {
    CURRENT_ASSET,
    FIXED_ASSET,
    CURRENT_LIABILITY,
    LONG_TERM_LIABILITY,
    OWNERS_EQUITY,
    OPERATING_REVENUE,
    DIRECT_COST,
    OPERATING_EXPENSE,
    TAX_EXPENSE
}

enum class InvoiceStatus {
    DRAFT,
    UNPAID,
    PARTIALLY_PAID,
    PAID,
    CANCELLED,
    OVERDUE
}

enum class BillStatus {
    DRAFT,
    UNPAID,
    PARTIALLY_PAID,
    PAID,
    CANCELLED,
    OVERDUE
}

enum class PaymentType {
    CUSTOMER_RECEIPT,
    SUPPLIER_PAYMENT,
    EXPENSE_PAYMENT,
    ACCOUNT_TRANSFER,
    LOAN_REPAYMENT,
    ASSET_PURCHASE
}

enum class DepreciationMethod {
    STRAIGHT_LINE,
    REDUCING_BALANCE
}

enum class JournalSourceType {
    SALE,
    PURCHASE_BILL,
    CUSTOMER_PAYMENT,
    SUPPLIER_PAYMENT,
    EXPENSE,
    ASSET_PURCHASE,
    DEPRECIATION,
    LOAN_DISBURSEMENT,
    LOAN_REPAYMENT,
    TRANSFER,
    MANUAL
}

enum class PriceType {
    RETAIL,
    WHOLESALE
}

enum class ActionType {
    PRICE_OVERRIDE,
    SALE_DELETED,
    JOB_CANCELLED,
    STOCK_EDITED,
    ROLE_CHANGED
}

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}

enum class SubscriptionPlan(val maxUsers: Int, val maxBusinesses: Int) {
    STARTER(maxUsers = 2, maxBusinesses = 1),
    GROWTH(maxUsers = 5, maxBusinesses = 3),
    ENTERPRISE(maxUsers = Int.MAX_VALUE, maxBusinesses = 10)
}
