package com.example.businessos.data.local.converters

import androidx.room.TypeConverter
import com.example.businessos.data.model.ActionType
import com.example.businessos.data.model.BusinessType
import com.example.businessos.data.model.JobStage
import com.example.businessos.data.model.PartItem
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.PriceType
import com.example.businessos.data.model.Role
import com.example.businessos.data.model.SaleLineItem
import com.example.businessos.data.model.StageTransition
import com.example.businessos.data.model.SubscriptionPlan
import com.example.businessos.data.model.SyncStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromBusinessType(value: BusinessType): String = value.name

    @TypeConverter
    fun toBusinessType(value: String): BusinessType = BusinessType.valueOf(value)

    @TypeConverter
    fun fromRole(value: Role): String = value.name

    @TypeConverter
    fun toRole(value: String): Role = Role.valueOf(value)

    @TypeConverter
    fun fromJobStage(value: JobStage): String = value.name

    @TypeConverter
    fun toJobStage(value: String): JobStage = JobStage.valueOf(value)

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)

    @TypeConverter
    fun fromActionType(value: ActionType): String = value.name

    @TypeConverter
    fun toActionType(value: String): ActionType = ActionType.valueOf(value)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)

    @TypeConverter
    fun fromSubscriptionPlan(value: SubscriptionPlan): String = value.name

    @TypeConverter
    fun toSubscriptionPlan(value: String): SubscriptionPlan = SubscriptionPlan.valueOf(value)

    @TypeConverter
    fun fromPartItemList(list: List<PartItem>?): String {
        return gson.toJson(list ?: emptyList<PartItem>())
    }

    @TypeConverter
    fun toPartItemList(json: String?): List<PartItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<PartItem>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromStageTransitionList(list: List<StageTransition>?): String {
        return gson.toJson(list ?: emptyList<StageTransition>())
    }

    @TypeConverter
    fun toStageTransitionList(json: String?): List<StageTransition> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<StageTransition>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromSaleLineItemList(list: List<SaleLineItem>?): String {
        return gson.toJson(list ?: emptyList<SaleLineItem>())
    }

    @TypeConverter
    fun toSaleLineItemList(json: String?): List<SaleLineItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<SaleLineItem>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromAccountCategory(value: com.example.businessos.data.model.AccountCategory): String = value.name

    @TypeConverter
    fun toAccountCategory(value: String): com.example.businessos.data.model.AccountCategory =
        com.example.businessos.data.model.AccountCategory.valueOf(value)

    @TypeConverter
    fun fromAccountSubCategory(value: com.example.businessos.data.model.AccountSubCategory): String = value.name

    @TypeConverter
    fun toAccountSubCategory(value: String): com.example.businessos.data.model.AccountSubCategory =
        com.example.businessos.data.model.AccountSubCategory.valueOf(value)

    @TypeConverter
    fun fromInvoiceStatus(value: com.example.businessos.data.model.InvoiceStatus): String = value.name

    @TypeConverter
    fun toInvoiceStatus(value: String): com.example.businessos.data.model.InvoiceStatus =
        com.example.businessos.data.model.InvoiceStatus.valueOf(value)

    @TypeConverter
    fun fromBillStatus(value: com.example.businessos.data.model.BillStatus): String = value.name

    @TypeConverter
    fun toBillStatus(value: String): com.example.businessos.data.model.BillStatus =
        com.example.businessos.data.model.BillStatus.valueOf(value)

    @TypeConverter
    fun fromPaymentType(value: com.example.businessos.data.model.PaymentType): String = value.name

    @TypeConverter
    fun toPaymentType(value: String): com.example.businessos.data.model.PaymentType =
        com.example.businessos.data.model.PaymentType.valueOf(value)

    @TypeConverter
    fun fromDepreciationMethod(value: com.example.businessos.data.model.DepreciationMethod): String = value.name

    @TypeConverter
    fun toDepreciationMethod(value: String): com.example.businessos.data.model.DepreciationMethod =
        com.example.businessos.data.model.DepreciationMethod.valueOf(value)

    @TypeConverter
    fun fromJournalSourceType(value: com.example.businessos.data.model.JournalSourceType): String = value.name

    @TypeConverter
    fun toJournalSourceType(value: String): com.example.businessos.data.model.JournalSourceType =
        com.example.businessos.data.model.JournalSourceType.valueOf(value)

    @TypeConverter
    fun fromJournalLineItemList(list: List<com.example.businessos.data.model.JournalLineItem>?): String {
        return gson.toJson(list ?: emptyList<com.example.businessos.data.model.JournalLineItem>())
    }

    @TypeConverter
    fun toJournalLineItemList(json: String?): List<com.example.businessos.data.model.JournalLineItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<com.example.businessos.data.model.JournalLineItem>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromInvoiceLineItemList(list: List<com.example.businessos.data.model.InvoiceLineItem>?): String {
        return gson.toJson(list ?: emptyList<com.example.businessos.data.model.InvoiceLineItem>())
    }

    @TypeConverter
    fun toInvoiceLineItemList(json: String?): List<com.example.businessos.data.model.InvoiceLineItem> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<com.example.businessos.data.model.InvoiceLineItem>>() {}.type
        return gson.fromJson(json, type)
    }
}
