package com.example.businessos.data.model

data class TrialBalanceItem(
    val accountCode: String,
    val accountName: String,
    val category: AccountCategory,
    val debit: Double,
    val credit: Double
)

data class TrialBalanceReport(
    val items: List<TrialBalanceItem>,
    val totalDebit: Double,
    val totalCredit: Double,
    val isBalanced: Boolean = Math.abs(totalDebit - totalCredit) < 0.01
)

data class AccountAmountItem(
    val accountCode: String,
    val accountName: String,
    val amount: Double
)

data class ProfitAndLossReport(
    val revenueItems: List<AccountAmountItem>,
    val totalRevenue: Double,
    val costOfSalesItems: List<AccountAmountItem>,
    val totalCostOfSales: Double,
    val grossProfit: Double = totalRevenue - totalCostOfSales,
    val operatingExpenses: List<AccountAmountItem>,
    val totalOperatingExpenses: Double,
    val netProfit: Double = grossProfit - totalOperatingExpenses
)

data class BalanceSheetReport(
    val currentAssets: List<AccountAmountItem>,
    val totalCurrentAssets: Double,
    val fixedAssets: List<AccountAmountItem>,
    val totalFixedAssets: Double,
    val totalAssets: Double = totalCurrentAssets + totalFixedAssets,

    val currentLiabilities: List<AccountAmountItem>,
    val totalCurrentLiabilities: Double,
    val longTermLiabilities: List<AccountAmountItem>,
    val totalLongTermLiabilities: Double,
    val totalLiabilities: Double = totalCurrentLiabilities + totalLongTermLiabilities,

    val equityItems: List<AccountAmountItem>,
    val currentPeriodNetProfit: Double,
    val totalEquity: Double,

    val totalLiabilitiesAndEquity: Double = totalLiabilities + totalEquity,
    val isBalanced: Boolean = Math.abs(totalAssets - (totalLiabilities + totalEquity)) < 0.01
)

data class AgeingBucket(
    val current: Double = 0.0,
    val days1to30: Double = 0.0,
    val days31to60: Double = 0.0,
    val days61to90: Double = 0.0,
    val days90Plus: Double = 0.0,
    val total: Double = current + days1to30 + days31to60 + days61to90 + days90Plus
)

data class EntityAgeingItem(
    val entityId: String,
    val entityName: String,
    val phone: String,
    val bucket: AgeingBucket
)

data class AccountsReceivableAgeingReport(
    val overall: AgeingBucket,
    val customers: List<EntityAgeingItem>
)

data class AccountsPayableAgeingReport(
    val overall: AgeingBucket,
    val suppliers: List<EntityAgeingItem>
)
