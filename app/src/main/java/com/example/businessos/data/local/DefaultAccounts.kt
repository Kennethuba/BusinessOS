package com.example.businessos.data.local

import com.example.businessos.data.local.entity.AccountEntity
import com.example.businessos.data.model.AccountCategory
import com.example.businessos.data.model.AccountSubCategory
import java.util.UUID

object DefaultAccounts {
    fun createDefaultAccounts(businessId: String): List<AccountEntity> = listOf(
        // Assets (1000 - 1999)
        AccountEntity(UUID.randomUUID().toString(), businessId, "1010", "Cash on Hand (Till / Sanduku)", AccountCategory.ASSET, AccountSubCategory.CURRENT_ASSET, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "1020", "M-Pesa Business Till / Paybill", AccountCategory.ASSET, AccountSubCategory.CURRENT_ASSET, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "1030", "Bank Account (Akaunti ya Benki)", AccountCategory.ASSET, AccountSubCategory.CURRENT_ASSET, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "1200", "Accounts Receivable (Wateja wa Deni)", AccountCategory.ASSET, AccountSubCategory.CURRENT_ASSET, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "1300", "Inventory / Stock Asset (Mzigo & Vipuri)", AccountCategory.ASSET, AccountSubCategory.CURRENT_ASSET, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "1500", "Equipment & Tools (Mitambo & Vifaa)", AccountCategory.ASSET, AccountSubCategory.FIXED_ASSET, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "1510", "Accumulated Depreciation - Equipment", AccountCategory.ASSET, AccountSubCategory.FIXED_ASSET, isSystemAccount = true),

        // Liabilities (2000 - 2999)
        AccountEntity(UUID.randomUUID().toString(), businessId, "2010", "Accounts Payable (Madeni ya Wasambazaji)", AccountCategory.LIABILITY, AccountSubCategory.CURRENT_LIABILITY, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "2020", "Short-Term Loans & Overdrafts (Mikopo)", AccountCategory.LIABILITY, AccountSubCategory.CURRENT_LIABILITY, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "2100", "Taxes & Levies Payable (Kodi ya KRA)", AccountCategory.LIABILITY, AccountSubCategory.CURRENT_LIABILITY, isSystemAccount = true),

        // Equity (3000 - 3999)
        AccountEntity(UUID.randomUUID().toString(), businessId, "3010", "Owner's Capital (Mtaji wa Mmiliki)", AccountCategory.EQUITY, AccountSubCategory.OWNERS_EQUITY, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "3020", "Retained Earnings (Faida Iliyobakishwa)", AccountCategory.EQUITY, AccountSubCategory.OWNERS_EQUITY, isSystemAccount = true),

        // Income / Revenue (4000 - 4999)
        AccountEntity(UUID.randomUUID().toString(), businessId, "4010", "Sales Revenue (Mauzo ya Bidhaa)", AccountCategory.INCOME, AccountSubCategory.OPERATING_REVENUE, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "4020", "Service & Repair Revenue (Ufundi & Ukarabati)", AccountCategory.INCOME, AccountSubCategory.OPERATING_REVENUE, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "4030", "Other Income (Mapato ya Ziada)", AccountCategory.INCOME, AccountSubCategory.OPERATING_REVENUE, isSystemAccount = true),

        // Cost of Goods Sold (5000 - 5999)
        AccountEntity(UUID.randomUUID().toString(), businessId, "5010", "Cost of Goods Sold (Gharama ya Mzigo)", AccountCategory.EXPENSE, AccountSubCategory.DIRECT_COST, isSystemAccount = true),

        // Operating Expenses (6000 - 6999)
        AccountEntity(UUID.randomUUID().toString(), businessId, "6010", "Rent Expense (Kodi ya Chumba/Kibanda)", AccountCategory.EXPENSE, AccountSubCategory.OPERATING_EXPENSE, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "6020", "Electricity & Water (Umeme/Tokeni & Maji)", AccountCategory.EXPENSE, AccountSubCategory.OPERATING_EXPENSE, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "6030", "Salaries & Wages (Mishahara & Vibaruwa)", AccountCategory.EXPENSE, AccountSubCategory.OPERATING_EXPENSE, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "6040", "Transport & Delivery (Nauli & Usafiri)", AccountCategory.EXPENSE, AccountSubCategory.OPERATING_EXPENSE, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "6050", "Meals & Staff Lunch (Chakula)", AccountCategory.EXPENSE, AccountSubCategory.OPERATING_EXPENSE, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "6060", "Depreciation Expense (Uchakavu wa Vifaa)", AccountCategory.EXPENSE, AccountSubCategory.OPERATING_EXPENSE, isSystemAccount = true),
        AccountEntity(UUID.randomUUID().toString(), businessId, "6070", "General & Miscellaneous (Matumizi Mengineyo)", AccountCategory.EXPENSE, AccountSubCategory.OPERATING_EXPENSE, isSystemAccount = true)
    )
}
