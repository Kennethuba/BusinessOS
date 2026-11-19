package com.example.businessos.data.model

data class TrialBalanceItem(
    val accountCode: String,
    val accountName: String,
    val category: AccountCategory,
    val debit: Money,
    val credit: Money
)

data class TrialBalanceReport(
    val items: List<TrialBalanceItem>,
    val totalDebit: Money,
    val totalCredit: Money,
    val isBalanced: Boolean = totalDebit == totalCredit
)

data class AccountAmountItem(
    val accountCode: String,
    val accountName: String,
    val amount: Money
)

data class ProfitAndLossReport(
    val revenueItems: List<AccountAmountItem>,
    val totalRevenue: Money,
    val costOfSalesItems: List<AccountAmountItem>,
    val totalCostOfSales: Money,
    val grossProfit: Money = totalRevenue - totalCostOfSales,
    val operatingExpenses: List<AccountAmountItem>,
    val totalOperatingExpenses: Money,
    val netProfit: Money = grossProfit - totalOperatingExpenses
)

data class BalanceSheetReport(
    val currentAssets: List<AccountAmountItem>,
    val totalCurrentAssets: Money,
    val fixedAssets: List<AccountAmountItem>,
    val totalFixedAssets: Money,
    val totalAssets: Money = totalCurrentAssets + totalFixedAssets,

    val currentLiabilities: List<AccountAmountItem>,
    val totalCurrentLiabilities: Money,
    val longTermLiabilities: List<AccountAmountItem>,
    val totalLongTermLiabilities: Money,
    val totalLiabilities: Money = totalCurrentLiabilities + totalLongTermLiabilities,

    val equityItems: List<AccountAmountItem>,
    val currentPeriodNetProfit: Money,
    val totalEquity: Money,

    val totalLiabilitiesAndEquity: Money = totalLiabilities + totalEquity,
    val isBalanced: Boolean = totalAssets == (totalLiabilities + totalEquity)
)

data class AgeingBucket(
    val current: Money = Money.ZERO,
    val days1to30: Money = Money.ZERO,
    val days31to60: Money = Money.ZERO,
    val days61to90: Money = Money.ZERO,
    val days90Plus: Money = Money.ZERO,
    val total: Money = current + days1to30 + days31to60 + days61to90 + days90Plus
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
