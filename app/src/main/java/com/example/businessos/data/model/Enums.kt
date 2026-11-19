package com.example.businessos.data.model

enum class BusinessType {
    GARAGE,
    STALL
}

enum class BusinessSide {
    SHOP,
    GARAGE
}

enum class Role {
    OWNER,
    MANAGER,
    CASHIER,
    VIEWER;

    companion object {
        fun fromLegacy(roleStr: String?): Role {
            return when (roleStr?.uppercase()) {
                "OWNER" -> OWNER
                "MANAGER" -> MANAGER
                "CASHIER", "STAFF", "ACCOUNTANT" -> CASHIER
                "VIEWER" -> VIEWER
                else -> CASHIER
            }
        }
    }
}

enum class JobStage(val title: String, val order: Int) {
    INTAKE("Intake", 1),
    DIAGNOSIS("Diagnosis", 2),
    PARTS_ASSIGNED("Parts Assigned", 3),
    AWAITING_APPROVAL("Awaiting Approval", 4),
    IN_PROGRESS("In Progress", 5),
    COMPLETED("Completed", 6),
    INVOICED("Invoiced", 7),
    CANCELLED("Cancelled", 8);

    fun next(): JobStage? = entries.firstOrNull { it.order == this.order + 1 && it != CANCELLED }
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
    VOIDED,
    OVERDUE
}

enum class BillStatus {
    DRAFT,
    UNPAID,
    PARTIALLY_PAID,
    PAID,
    VOIDED,
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
    INVOICE,
    PURCHASE_BILL,
    CUSTOMER_PAYMENT,
    SUPPLIER_PAYMENT,
    EXPENSE,
    WAGE,
    STOCK_ADJUSTMENT,
    OPENING_BALANCE,
    VOID_REVERSAL,
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
    SALE_VOIDED,
    EXPENSE_VOIDED,
    JOB_CANCELLED,
    STOCK_EDITED,
    STOCK_ADJUSTED,
    WAGE_PAID,
    ROLE_CHANGED
}

enum class StockMovementType {
    OPENING,
    SALE,
    PURCHASE,
    JOB_PART,
    ADJUSTMENT,
    VOID_SALE,
    VOID_JOB
}

enum class DocumentType {
    SALE,
    INVOICE,
    PAYMENT,
    JOURNAL,
    BILL,
    EXPENSE,
    WAGE,
    LAYAWAY
}

enum class Permission {
    MAKE_SALE,
    SELL_NEGATIVE_STOCK,
    TAKE_PAYMENT,
    CREATE_CUSTOMER,
    INTAKE_JOB,
    UPDATE_JOB_STAGE,
    EDIT_STOCK,
    STOCK_TAKE_ADJUST,
    RECORD_EXPENSE,
    VOID_RECORD,
    PAY_WAGES,
    MANAGE_STAFF,
    EDIT_BUSINESS_SETTINGS,
    VIEW_FINANCIAL_REPORTS,
    VIEW_NEGATIVE_STOCK_REPORT,
    BACKUP_RESTORE,
    DELETE_BUSINESS
}
