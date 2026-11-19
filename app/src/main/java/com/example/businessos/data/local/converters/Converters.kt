package com.example.businessos.data.local.converters

import androidx.room.TypeConverter
import com.example.businessos.data.model.AccountCategory
import com.example.businessos.data.model.AccountSubCategory
import com.example.businessos.data.model.ActionType
import com.example.businessos.data.model.BillStatus
import com.example.businessos.data.model.BusinessSide
import com.example.businessos.data.model.BusinessType
import com.example.businessos.data.model.DepreciationMethod
import com.example.businessos.data.model.DocumentType
import com.example.businessos.data.model.InvoiceStatus
import com.example.businessos.data.model.JobStage
import com.example.businessos.data.model.JournalSourceType
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.PaymentType
import com.example.businessos.data.model.PriceType
import com.example.businessos.data.model.Role
import com.example.businessos.data.model.StockMovementType

/**
 * Room TypeConverters.
 *
 * Rules:
 * - Money (amounts) is stored as Long cents directly — NO converter needed.
 * - JSON blob converters are REMOVED: sale items, invoice items, job parts,
 *   stage transitions and journal lines now live in proper child tables.
 * - SyncStatus and SubscriptionPlan are REMOVED (deleted from the domain model).
 * - DepreciationMethod kept for future asset module; BillStatus kept for supplier bills.
 */
class Converters {

    // ── Enums ────────────────────────────────────────────────────────────────

    @TypeConverter fun fromBusinessType(v: BusinessType): String = v.name
    @TypeConverter fun toBusinessType(v: String): BusinessType = BusinessType.valueOf(v)

    @TypeConverter fun fromBusinessSide(v: BusinessSide): String = v.name
    @TypeConverter fun toBusinessSide(v: String): BusinessSide = BusinessSide.valueOf(v)

    @TypeConverter fun fromRole(v: Role): String = v.name
    @TypeConverter fun toRole(v: String): Role = Role.valueOf(v)

    @TypeConverter fun fromJobStage(v: JobStage): String = v.name
    @TypeConverter fun toJobStage(v: String): JobStage = JobStage.valueOf(v)

    @TypeConverter fun fromPaymentMethod(v: PaymentMethod): String = v.name
    @TypeConverter fun toPaymentMethod(v: String): PaymentMethod = PaymentMethod.valueOf(v)

    @TypeConverter fun fromAccountCategory(v: AccountCategory): String = v.name
    @TypeConverter fun toAccountCategory(v: String): AccountCategory = AccountCategory.valueOf(v)

    @TypeConverter fun fromAccountSubCategory(v: AccountSubCategory): String = v.name
    @TypeConverter fun toAccountSubCategory(v: String): AccountSubCategory = AccountSubCategory.valueOf(v)

    @TypeConverter fun fromInvoiceStatus(v: InvoiceStatus): String = v.name
    @TypeConverter fun toInvoiceStatus(v: String): InvoiceStatus = InvoiceStatus.valueOf(v)

    @TypeConverter fun fromBillStatus(v: BillStatus): String = v.name
    @TypeConverter fun toBillStatus(v: String): BillStatus = BillStatus.valueOf(v)

    @TypeConverter fun fromPaymentType(v: PaymentType): String = v.name
    @TypeConverter fun toPaymentType(v: String): PaymentType = PaymentType.valueOf(v)

    @TypeConverter fun fromDepreciationMethod(v: DepreciationMethod): String = v.name
    @TypeConverter fun toDepreciationMethod(v: String): DepreciationMethod = DepreciationMethod.valueOf(v)

    @TypeConverter fun fromJournalSourceType(v: JournalSourceType): String = v.name
    @TypeConverter fun toJournalSourceType(v: String): JournalSourceType = JournalSourceType.valueOf(v)

    @TypeConverter fun fromPriceType(v: PriceType): String = v.name
    @TypeConverter fun toPriceType(v: String): PriceType = PriceType.valueOf(v)

    @TypeConverter fun fromActionType(v: ActionType): String = v.name
    @TypeConverter fun toActionType(v: String): ActionType = ActionType.valueOf(v)

    @TypeConverter fun fromStockMovementType(v: StockMovementType): String = v.name
    @TypeConverter fun toStockMovementType(v: String): StockMovementType = StockMovementType.valueOf(v)

    @TypeConverter fun fromDocumentType(v: DocumentType): String = v.name
    @TypeConverter fun toDocumentType(v: String): DocumentType = DocumentType.valueOf(v)
}
