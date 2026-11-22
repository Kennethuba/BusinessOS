package com.example.businessos.data.repository

import androidx.room.withTransaction
import com.example.businessos.data.local.AppDatabase
import com.example.businessos.data.local.dao.*
import com.example.businessos.data.local.entity.*
import com.example.businessos.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class BusinessOsRepository(
    val database: AppDatabase,
    private val authRepository: AuthRepository,
    private val businessDao: BusinessDao,
    private val businessMemberDao: BusinessMemberDao,
    private val userDao: UserDao,
    private val itemDao: ItemDao,
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val stockMovementDao: StockMovementDao,
    private val documentCounterDao: DocumentCounterDao,
    private val customerDao: CustomerDao,
    private val jobCardDao: JobCardDao,
    private val jobPartDao: JobPartDao,
    private val jobStageTransitionDao: JobStageTransitionDao,
    private val auditLogDao: AuditLogDao,
    private val expenseDao: ExpenseDao,
    private val accountDao: AccountDao,
    private val journalEntryDao: JournalEntryDao,
    private val journalLineDao: JournalLineDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val supplierDao: SupplierDao,
    private val paymentDao: PaymentDao,
    private val wagePaymentDao: WagePaymentDao,
    private val layawayDao: LayawayDao
) {

    // ── Session Helpers ──────────────────────────────────────────────────────

    fun getSession(): CurrentUserSession =
        authRepository.session.value ?: throw IllegalStateException("Not logged in")

    fun getActiveBusinessId(): String =
        getSession().activeBusinessId ?: throw IllegalStateException("No active business selected")

    fun getActiveUserId(): String = getSession().userId

    // ── Permission Guard (PLAN.md section 4.3) ───────────────────────────────

    /**
     * Throws [SecurityException] unless the signed-in user's role in the active business
     * grants [permission]. Called at the top of every write operation so access control
     * does not depend on the UI hiding buttons.
     */
    private suspend fun checkPermission(permission: Permission) {
        val session = getSession()
        val bizId = session.activeBusinessId
            ?: throw IllegalStateException("No active business selected")
        val member = businessMemberDao.getMember(bizId, session.userId)
            ?: throw SecurityException("You are not a member of this business")
        if (!RolePermissions.has(member.role, permission)) {
            throw SecurityException("Your role (${member.role.name.lowercase()}) is not allowed to do this")
        }
    }

    private fun newMovement(
        businessId: String,
        itemId: String,
        type: StockMovementType,
        delta: Long,
        balanceAfter: Long,
        referenceType: String,
        referenceId: String,
        notes: String,
        userId: String,
        unitCostCents: Long = 0L
    ) = StockMovementEntity(
        id = UUID.randomUUID().toString(),
        businessId = businessId,
        itemId = itemId,
        movementType = type,
        quantityDelta = delta,
        balanceAfter = balanceAfter,
        unitCostCents = unitCostCents,
        referenceType = referenceType,
        referenceId = referenceId,
        notes = notes,
        createdByUserId = userId
    )

    private suspend fun writeAudit(
        businessId: String,
        action: ActionType,
        userId: String,
        details: String,
        before: String? = null
    ) {
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                actionType = action,
                beforeValue = before,
                afterValue = details,
                performedByUserId = userId,
                performedByName = getSession().name
            )
        )
    }

    private fun expenseAccountCode(category: String): String = when (category.lowercase()) {
        "rent" -> "6010"
        "utilities", "electricity", "water" -> "6020"
        "salary", "wage" -> "6030"
        "transport", "delivery" -> "6040"
        "food", "meals" -> "6050"
        else -> "6070"
    }

    // ── Business & Observables ───────────────────────────────────────────────

    fun observeCurrentBusiness(): Flow<BusinessEntity?> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(null)
        return businessDao.observeBusinessById(bizId)
    }

    suspend fun getBusiness(businessId: String): BusinessEntity? =
        businessDao.getBusinessById(businessId)

    suspend fun updateBusinessSettings(
        name: String,
        phone: String,
        address: String,
        receiptFooter: String,
        isShopEnabled: Boolean,
        isGarageEnabled: Boolean,
        allowNegativeStock: Boolean
    ): Result<Unit> = runCatching {
        checkPermission(Permission.EDIT_BUSINESS_SETTINGS)
        val bizId = getActiveBusinessId()
        val existing = businessDao.getBusinessById(bizId)
            ?: throw IllegalStateException("Business not found")
        val updated = existing.copy(
            name = name.trim(),
            phone = phone.trim(),
            address = address.trim(),
            receiptFooter = receiptFooter.trim(),
            isShopEnabled = isShopEnabled,
            isGarageEnabled = isGarageEnabled,
            allowNegativeStock = allowNegativeStock,
            updatedAt = System.currentTimeMillis()
        )
        businessDao.updateBusiness(updated)
    }

    fun observeItems(): Flow<List<ItemEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return itemDao.observeItems(bizId)
    }

    fun observeCustomers(): Flow<List<CustomerEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return customerDao.observeCustomersForBusiness(bizId)
    }

    fun observeDeniLedger(): Flow<List<CustomerEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return customerDao.observeDeniLedger(bizId)
    }

    fun observeSales(): Flow<List<SaleEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return saleDao.observeSalesForBusiness(bizId)
    }

    fun observeJobCards(): Flow<List<JobCardEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return jobCardDao.observeJobCardsForBusiness(bizId)
    }

    fun observeExpenses(): Flow<List<ExpenseEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return expenseDao.observeExpenses(bizId)
    }

    fun observeStaffMembers(): Flow<List<MemberWithUser>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return businessMemberDao.observeMembersWithUser(bizId)
    }

    fun observeAuditLogs(): Flow<List<AuditLogEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return auditLogDao.observeAuditLogsForBusiness(bizId)
    }

    fun observeAccounts(): Flow<List<AccountEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return accountDao.observeAccounts(bizId)
    }

    fun observeJournalEntries(): Flow<List<JournalEntryEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return journalEntryDao.observeJournalEntries(bizId)
    }

    fun observeInvoices(): Flow<List<InvoiceEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return invoiceDao.observeInvoices(bizId)
    }

    fun observeSuppliers(): Flow<List<SupplierEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return supplierDao.observeSuppliers(bizId)
    }

    fun observePayments(): Flow<List<PaymentEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return paymentDao.observePayments(bizId)
    }

    fun observeWagePayments(): Flow<List<WagePaymentEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return wagePaymentDao.observeWagePayments(bizId)
    }

    fun observeLayaways(): Flow<List<LayawayEntity>> {
        val bizId = authRepository.session.value?.activeBusinessId ?: return flowOf(emptyList())
        return layawayDao.observeLayaways(bizId)
    }

    // ── Item / Inventory Operations ──────────────────────────────────────────

    suspend fun createItem(
        name: String,
        retailPrice: Money,
        wholesalePrice: Money,
        initialStock: Long = 0L,
        lowStockThreshold: Int = 5,
        sku: String? = null,
        category: String? = null,
        unit: String = "pcs",
        costPrice: Money = Money.ZERO
    ): Result<ItemEntity> = runCatching {
        checkPermission(Permission.EDIT_STOCK)
        val bizId = getActiveBusinessId()
        val itemId = UUID.randomUUID().toString()
        val item = ItemEntity(
            id = itemId,
            businessId = bizId,
            name = name.trim(),
            sku = sku?.trim()?.ifBlank { null },
            category = category?.trim()?.ifBlank { null },
            unit = unit.trim().ifBlank { "pcs" },
            costPriceCents = costPrice.cents,
            retailPriceCents = retailPrice.cents,
            wholesalePriceCents = wholesalePrice.cents,
            stockQty = initialStock,
            lowStockThreshold = lowStockThreshold,
            isActive = true
        )

        database.withTransaction {
            itemDao.insertItem(item)
            if (initialStock != 0L) {
                stockMovementDao.insertMovement(
                    newMovement(
                        businessId = bizId,
                        itemId = itemId,
                        type = StockMovementType.OPENING,
                        delta = initialStock,
                        balanceAfter = initialStock,
                        referenceType = "ITEM",
                        referenceId = itemId,
                        notes = "Initial opening stock",
                        userId = getActiveUserId(),
                        unitCostCents = costPrice.cents
                    )
                )
            }
        }
        item
    }

    suspend fun updateStockItemCount(
        itemId: String,
        physicalCount: Long,
        reason: String
    ): Result<Unit> = runCatching {
        checkPermission(Permission.STOCK_TAKE_ADJUST)
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()
        database.withTransaction {
            val item = itemDao.getItemById(itemId, bizId)
                ?: throw IllegalArgumentException("Item not found")
            val delta = physicalCount - item.stockQty
            itemDao.adjustStock(itemId, bizId, delta)
            stockMovementDao.insertMovement(
                newMovement(
                    businessId = bizId,
                    itemId = itemId,
                    type = StockMovementType.ADJUSTMENT,
                    delta = delta,
                    balanceAfter = physicalCount,
                    referenceType = "STOCK_AUDIT",
                    referenceId = itemId,
                    notes = reason.ifBlank { "Stock count adjustment" },
                    userId = userId,
                    unitCostCents = item.costPriceCents
                )
            )
            writeAudit(
                bizId, ActionType.STOCK_ADJUSTED, userId,
                details = "Adjusted ${item.name} stock to $physicalCount. Reason: $reason",
                before = "${item.name}: ${item.stockQty}"
            )
        }
    }

    // ── Journal Posting Engine (Debits == Credits Invariant) ──────────────────

    /**
     * Posts a balanced journal entry. Throws if debits != credits so that, when called inside
     * a withTransaction block, the whole operation rolls back instead of leaving the books
     * half-written.
     */
    private suspend fun postJournalEntry(
        businessId: String,
        date: Long,
        description: String,
        sourceType: JournalSourceType,
        sourceId: String?,
        side: BusinessSide,
        lines: List<JournalLineItem>
    ): JournalEntryEntity {
        val totalDebit = lines.fold(Money.ZERO) { acc, l -> acc + l.debit }
        val totalCredit = lines.fold(Money.ZERO) { acc, l -> acc + l.credit }

        if (totalDebit != totalCredit || totalDebit <= Money.ZERO) {
            throw IllegalArgumentException(
                "Unbalanced journal: total debits (${totalDebit.formatted}) must equal total credits (${totalCredit.formatted}) and be greater than 0"
            )
        }

        return database.withTransaction {
            val entryNum = documentCounterDao.nextNumber(businessId, DocumentType.JOURNAL, "JRN-")
            val entryId = UUID.randomUUID().toString()

            val entry = JournalEntryEntity(
                id = entryId,
                businessId = businessId,
                entryNumber = entryNum,
                date = date,
                description = description.trim(),
                sourceType = sourceType,
                sourceId = sourceId,
                totalDebitCents = totalDebit.cents,
                totalCreditCents = totalCredit.cents,
                side = side,
                isReversed = false,
                reversedByEntryId = null,
                createdAt = System.currentTimeMillis()
            )
            journalEntryDao.insertEntry(entry)

            val childLines = lines.map { l ->
                JournalLineEntity(
                    id = UUID.randomUUID().toString(),
                    entryId = entryId,
                    accountId = l.accountId,
                    debitCents = l.debit.cents,
                    creditCents = l.credit.cents,
                    notes = l.narration ?: ""
                )
            }
            journalLineDao.insertLines(childLines)

            // Adjust account balances atomically:
            // Assets & Expenses: balance += debit - credit
            // Liabilities, Equity, Revenue: balance += credit - debit
            for (line in lines) {
                val account = accountDao.getAccountById(line.accountId, businessId)
                    ?: continue
                val delta = when (account.category) {
                    AccountCategory.ASSET, AccountCategory.EXPENSE ->
                        line.debit.cents - line.credit.cents
                    AccountCategory.LIABILITY, AccountCategory.EQUITY, AccountCategory.INCOME ->
                        line.credit.cents - line.debit.cents
                }
                accountDao.adjustBalance(line.accountId, businessId, delta)
            }
            entry
        }
    }

    // ── Sales Engine & Void Operations ───────────────────────────────────────

    suspend fun recordSale(
        customerId: String?,
        customerName: String?,
        items: List<SaleLineItem>,
        discount: Money = Money.ZERO,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        mpesaReference: String? = null,
        side: BusinessSide = BusinessSide.SHOP
    ): Result<SaleEntity> = runCatching {
        checkPermission(Permission.MAKE_SALE)
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()
        if (items.isEmpty()) throw IllegalArgumentException("Cannot record an empty sale")

        val subtotal = items.fold(Money.ZERO) { acc, item -> acc + item.subtotal }
        val total = (subtotal - discount).coerceAtLeast(Money.ZERO)

        val member = businessMemberDao.getMember(bizId, userId)
        val biz = businessDao.getBusinessById(bizId)
        val canSellNegative = (member?.canSellNegativeStock == true) || (biz?.allowNegativeStock == true)

        database.withTransaction {
            // Verify stock constraints before writing
            for (line in items) {
                val dbItem = itemDao.getItemById(line.itemId, bizId)
                    ?: throw IllegalArgumentException("Item ${line.itemName} not found")
                if (!canSellNegative && dbItem.stockQty < line.qty) {
                    throw IllegalStateException("Insufficient stock for ${dbItem.name}. In stock: ${dbItem.stockQty}, selling: ${line.qty}")
                }
            }

            val saleNum = documentCounterDao.nextNumber(bizId, DocumentType.SALE, "SAL-")
            val saleId = UUID.randomUUID().toString()

            val sale = SaleEntity(
                id = saleId,
                businessId = bizId,
                saleNumber = saleNum,
                customerId = customerId,
                customerName = customerName,
                subtotalCents = subtotal.cents,
                discountCents = discount.cents,
                totalCents = total.cents,
                paymentMethod = paymentMethod,
                mpesaReference = mpesaReference?.trim()?.ifBlank { null },
                side = side,
                userId = userId,
                createdAt = System.currentTimeMillis()
            )
            saleDao.insertSale(sale)

            val childItems = items.map { l ->
                SaleItemEntity(
                    id = UUID.randomUUID().toString(),
                    saleId = saleId,
                    itemId = l.itemId,
                    itemName = l.itemName,
                    quantity = l.qty,
                    unitPriceCents = l.unitPrice.cents,
                    costPriceCents = l.costPrice.cents,
                    subtotalCents = l.subtotal.cents
                )
            }
            saleItemDao.insertSaleItems(childItems)

            // Deduct stock and write stock movements
            for (line in items) {
                itemDao.adjustStock(line.itemId, bizId, -line.qty.toLong())
                val currentItem = itemDao.getItemById(line.itemId, bizId)
                val balanceAfter = currentItem?.stockQty ?: 0L
                stockMovementDao.insertMovement(
                    newMovement(
                        businessId = bizId,
                        itemId = line.itemId,
                        type = StockMovementType.SALE,
                        delta = -line.qty.toLong(),
                        balanceAfter = balanceAfter,
                        referenceType = "SALE",
                        referenceId = saleId,
                        notes = "Sale $saleNum",
                        userId = userId,
                        unitCostCents = line.costPrice.cents
                    )
                )
            }

            // Customer Deni update if credit
            if (paymentMethod == PaymentMethod.CREDIT && customerId != null) {
                customerDao.updateBalanceCents(customerId, bizId, total.cents)
            }

            // Financial Posting
            val cashAccCode = when (paymentMethod) {
                PaymentMethod.CASH -> "1010"
                PaymentMethod.MPESA -> "1020"
                PaymentMethod.BANK -> "1030"
                PaymentMethod.CREDIT -> "1200"
                PaymentMethod.SPLIT -> "1010"
            }
            val cashAccount = accountDao.getAccountByCode(cashAccCode, bizId)
            val revenueAccount = accountDao.getAccountByCode("4010", bizId)

            if (cashAccount != null && revenueAccount != null && total > Money.ZERO) {
                postJournalEntry(
                    businessId = bizId,
                    date = sale.createdAt,
                    description = "Sale $saleNum to ${customerName ?: "Cash Customer"}",
                    sourceType = JournalSourceType.SALE,
                    sourceId = saleId,
                    side = side,
                    lines = listOf(
                        JournalLineItem(cashAccount.id, cashAccount.code, cashAccount.name, debit = total),
                        JournalLineItem(revenueAccount.id, revenueAccount.code, revenueAccount.name, credit = total)
                    )
                )
            }

            sale
        }
    }

    suspend fun voidSale(saleId: String, reason: String): Result<Unit> = runCatching {
        checkPermission(Permission.VOID_RECORD)
        if (reason.isBlank()) throw IllegalArgumentException("A reason is required to void a sale")
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()

        database.withTransaction {
            val sale = saleDao.getSaleById(saleId, bizId)
                ?: throw IllegalArgumentException("Sale not found")
            if (sale.isVoided) throw IllegalStateException("Sale is already voided")

            saleDao.markVoided(saleId, bizId, reason.trim(), userId)

            // Restore inventory
            val saleItems = saleItemDao.getBySale(saleId)
            for (item in saleItems) {
                itemDao.adjustStock(item.itemId, bizId, item.quantity.toLong())
                val current = itemDao.getItemById(item.itemId, bizId)
                stockMovementDao.insertMovement(
                    newMovement(
                        businessId = bizId,
                        itemId = item.itemId,
                        type = StockMovementType.VOID_SALE,
                        delta = item.quantity.toLong(),
                        balanceAfter = current?.stockQty ?: 0L,
                        referenceType = "SALE",
                        referenceId = saleId,
                        notes = "Void Sale ${sale.saleNumber}: $reason",
                        userId = userId,
                        unitCostCents = item.costPriceCents
                    )
                )
            }

            // Restore customer balance if credit sale
            if (sale.paymentMethod == PaymentMethod.CREDIT && sale.customerId != null) {
                customerDao.updateBalanceCents(sale.customerId, bizId, -sale.totalCents)
            }

            // Post Reversing Journal Entry
            val cashAccCode = when (sale.paymentMethod) {
                PaymentMethod.CASH -> "1010"
                PaymentMethod.MPESA -> "1020"
                PaymentMethod.BANK -> "1030"
                PaymentMethod.CREDIT -> "1200"
                PaymentMethod.SPLIT -> "1010"
            }
            val cashAccount = accountDao.getAccountByCode(cashAccCode, bizId)
            val revenueAccount = accountDao.getAccountByCode("4010", bizId)
            val saleMoney = Money(sale.totalCents)

            if (cashAccount != null && revenueAccount != null && saleMoney > Money.ZERO) {
                postJournalEntry(
                    businessId = bizId,
                    date = System.currentTimeMillis(),
                    description = "VOID Sale ${sale.saleNumber} ($reason)",
                    sourceType = JournalSourceType.VOID_REVERSAL,
                    sourceId = saleId,
                    side = sale.side,
                    lines = listOf(
                        JournalLineItem(revenueAccount.id, revenueAccount.code, revenueAccount.name, debit = saleMoney),
                        JournalLineItem(cashAccount.id, cashAccount.code, cashAccount.name, credit = saleMoney)
                    )
                )
            }

            writeAudit(
                bizId, ActionType.SALE_VOIDED, userId,
                details = "Voided sale ${sale.saleNumber}. Reason: $reason",
                before = "${sale.saleNumber}: ${Money(sale.totalCents).formatted}"
            )
        }
    }

    // ── Customer & Deni Operations ───────────────────────────────────────────

    suspend fun createCustomer(
        name: String,
        phone: String,
        initialCredit: Money = Money.ZERO,
        dueDate: Long? = null,
        notes: String? = null
    ): Result<CustomerEntity> = runCatching {
        checkPermission(Permission.CREATE_CUSTOMER)
        val bizId = getActiveBusinessId()
        val customerId = UUID.randomUUID().toString()
        val customer = CustomerEntity(
            id = customerId,
            businessId = bizId,
            name = name.trim(),
            phone = phone.trim(),
            balanceCents = initialCredit.cents,
            dueDate = dueDate,
            notes = notes?.trim().orEmpty()
        )

        database.withTransaction {
            customerDao.insertCustomer(customer)
            if (initialCredit > Money.ZERO) {
                val arAccount = accountDao.getAccountByCode("1200", bizId)
                val equityAccount = accountDao.getAccountByCode("3010", bizId)
                if (arAccount != null && equityAccount != null) {
                    postJournalEntry(
                        businessId = bizId,
                        date = System.currentTimeMillis(),
                        description = "Opening Deni balance for ${customer.name}",
                        sourceType = JournalSourceType.OPENING_BALANCE,
                        sourceId = customerId,
                        side = BusinessSide.SHOP,
                        lines = listOf(
                            JournalLineItem(arAccount.id, arAccount.code, arAccount.name, debit = initialCredit),
                            JournalLineItem(equityAccount.id, equityAccount.code, equityAccount.name, credit = initialCredit)
                        )
                    )
                }
            }
        }
        customer
    }

    suspend fun recordDeniPayment(
        customerId: String,
        amountPaid: Money,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        mpesaCode: String? = null
    ): Result<Unit> = runCatching {
        checkPermission(Permission.TAKE_PAYMENT)
        val bizId = getActiveBusinessId()
        database.withTransaction {
            val customer = customerDao.getCustomerById(customerId, bizId)
                ?: throw IllegalArgumentException("Customer not found")

            customerDao.updateBalanceCents(customerId, bizId, -amountPaid.cents)

            val cashAccCode = if (paymentMethod == PaymentMethod.MPESA) "1020" else "1010"
            val cashAccount = accountDao.getAccountByCode(cashAccCode, bizId)
            val arAccount = accountDao.getAccountByCode("1200", bizId)

            if (cashAccount != null && arAccount != null && amountPaid > Money.ZERO) {
                postJournalEntry(
                    businessId = bizId,
                    date = System.currentTimeMillis(),
                    description = "Deni repayment from ${customer.name} (${mpesaCode ?: paymentMethod.name})",
                    sourceType = JournalSourceType.CUSTOMER_PAYMENT,
                    sourceId = customerId,
                    side = BusinessSide.SHOP,
                    lines = listOf(
                        JournalLineItem(cashAccount.id, cashAccount.code, cashAccount.name, debit = amountPaid),
                        JournalLineItem(arAccount.id, arAccount.code, arAccount.name, credit = amountPaid)
                    )
                )
            }
        }
    }

    suspend fun updateCustomerReminder(
        customerId: String,
        dueDate: Long?,
        notes: String?
    ): Result<Unit> = runCatching {
        val bizId = getActiveBusinessId()
        val customer = customerDao.getCustomerById(customerId, bizId)
            ?: throw IllegalArgumentException("Customer not found")
        customerDao.updateCustomer(
            customer.copy(dueDate = dueDate, notes = notes?.trim().orEmpty())
        )
    }

    // ── Garage Job Cards & Workflows ─────────────────────────────────────────

    suspend fun createJobCard(
        vehiclePlate: String,
        vehicleMake: String,
        vehicleModel: String,
        customerName: String,
        customerPhone: String,
        issueDescription: String,
        laborCost: Money = Money.ZERO,
        partsCost: Money = Money.ZERO,
        estimateTotal: Money = Money.ZERO,
        targetDeliveryDate: Long? = null,
        kind: JobOrderKind = JobOrderKind.CLIENT_ORDER
    ): Result<JobCardEntity> = runCatching {
        checkPermission(Permission.INTAKE_JOB)
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()
        val jobId = UUID.randomUUID().toString()

        database.withTransaction {
            val jobNum = documentCounterDao.nextNumber(bizId, DocumentType.INVOICE, "JOB-")
            val jobCard = JobCardEntity(
                id = jobId,
                businessId = bizId,
                jobNumber = jobNum,
                kind = kind,
                vehiclePlate = vehiclePlate.trim().uppercase(),
                vehicleMake = vehicleMake.trim(),
                vehicleModel = vehicleModel.trim(),
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                issueDescription = issueDescription.trim(),
                laborCostCents = laborCost.cents,
                partsCostCents = partsCost.cents,
                estimateTotalCents = estimateTotal.cents,
                targetDeliveryDate = targetDeliveryDate,
                stage = JobStage.INTAKE,
                assignedTechnicianId = userId,
                invoiceId = null,
                isVoided = false
            )
            jobCardDao.insertJobCard(jobCard)
            jobStageTransitionDao.insertTransition(
                JobStageTransitionEntity(
                    id = UUID.randomUUID().toString(),
                    jobCardId = jobId,
                    fromStage = "",
                    toStage = JobStage.INTAKE.name,
                    notes = "Initial intake",
                    userId = userId
                )
            )
            jobCard
        }
    }

    suspend fun updateJobParts(jobCardId: String, newParts: List<PartItem>): Result<Unit> = runCatching {
        checkPermission(Permission.UPDATE_JOB_STAGE)
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()

        database.withTransaction {
            val job = jobCardDao.getJobCardById(jobCardId, bizId)
                ?: throw IllegalArgumentException("Job card not found")

            val existingParts = jobPartDao.getByJob(jobCardId)
            // Restore stock for removed parts that had stock deducted
            for (p in existingParts) {
                if (p.isStockDeducted && p.itemId != null && newParts.none { it.id == p.id }) {
                    itemDao.adjustStock(p.itemId, bizId, p.quantity.toLong())
                    val cur = itemDao.getItemById(p.itemId, bizId)
                    stockMovementDao.insertMovement(
                        newMovement(
                            businessId = bizId,
                            itemId = p.itemId,
                            type = StockMovementType.VOID_JOB,
                            delta = p.quantity.toLong(),
                            balanceAfter = cur?.stockQty ?: 0L,
                            referenceType = "JOB_CARD",
                            referenceId = jobCardId,
                            notes = "Removed from Job ${job.jobNumber}",
                            userId = userId,
                            unitCostCents = p.costPriceCents
                        )
                    )
                }
            }

            jobPartDao.deleteByJob(jobCardId)

            val childParts = newParts.map { p ->
                var stockDeducted = p.isStockDeducted
                if (p.itemId != null && !stockDeducted) {
                    itemDao.adjustStock(p.itemId, bizId, -p.quantity.toLong())
                    val cur = itemDao.getItemById(p.itemId, bizId)
                    stockMovementDao.insertMovement(
                        newMovement(
                            businessId = bizId,
                            itemId = p.itemId,
                            type = StockMovementType.JOB_PART,
                            delta = -p.quantity.toLong(),
                            balanceAfter = cur?.stockQty ?: 0L,
                            referenceType = "JOB_CARD",
                            referenceId = jobCardId,
                            notes = "Assigned to Job ${job.jobNumber}",
                            userId = userId,
                            unitCostCents = p.costPrice.cents
                        )
                    )
                    stockDeducted = true
                }

                JobPartEntity(
                    id = p.id,
                    jobCardId = jobCardId,
                    itemId = p.itemId,
                    partName = p.name,
                    unitPriceCents = p.unitPrice.cents,
                    costPriceCents = p.costPrice.cents,
                    quantity = p.quantity,
                    isStockDeducted = stockDeducted
                )
            }
            jobPartDao.insertJobParts(childParts)

            val totalParts = newParts.fold(Money.ZERO) { acc, p -> acc + p.total }
            jobCardDao.updateJobCard(
                job.copy(
                    partsCostCents = totalParts.cents,
                    estimateTotalCents = job.laborCostCents + totalParts.cents
                )
            )
        }
    }

    suspend fun advanceJobStage(jobCardId: String, note: String?): Result<JobStage?> = runCatching {
        checkPermission(Permission.UPDATE_JOB_STAGE)
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()
        database.withTransaction {
            val job = jobCardDao.getJobCardById(jobCardId, bizId)
                ?: throw IllegalArgumentException("Job card not found")
            val next = job.stage.next() ?: return@withTransaction job.stage

            jobCardDao.updateStage(jobCardId, bizId, next)
            jobStageTransitionDao.insertTransition(
                JobStageTransitionEntity(
                    id = UUID.randomUUID().toString(),
                    jobCardId = jobCardId,
                    fromStage = job.stage.name,
                    toStage = next.name,
                    notes = note.orEmpty(),
                    userId = userId
                )
            )

            // If entering INVOICED stage, create garage invoice
            if (next == JobStage.INVOICED && job.invoiceId == null) {
                val invRes = createInvoice(
                    customerId = null,
                    customerName = job.customerName,
                    customerPhone = job.customerPhone,
                    items = listOf(
                        InvoiceLineItem(
                            description = "Service/Labor for ${job.vehiclePlate} (${job.vehicleMake} ${job.vehicleModel})",
                            unitPrice = Money(job.laborCostCents)
                        )
                    ),
                    side = BusinessSide.GARAGE
                )
                jobCardDao.linkInvoice(jobCardId, bizId, invRes.getOrThrow().id)
            }
            next
        }
    }

    suspend fun reopenJobStage(jobCardId: String, targetStage: JobStage, reason: String?): Result<Unit> = runCatching {
        checkPermission(Permission.VOID_RECORD)
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()
        database.withTransaction {
            val job = jobCardDao.getJobCardById(jobCardId, bizId)
                ?: throw IllegalArgumentException("Job card not found")
            jobCardDao.updateStage(jobCardId, bizId, targetStage)
            jobStageTransitionDao.insertTransition(
                JobStageTransitionEntity(
                    id = UUID.randomUUID().toString(),
                    jobCardId = jobCardId,
                    fromStage = job.stage.name,
                    toStage = targetStage.name,
                    notes = reason.orEmpty(),
                    userId = userId
                )
            )
        }
    }

    // ── Expense Engine & Voids ───────────────────────────────────────────────

    suspend fun recordExpense(
        category: String,
        title: String,
        amount: Money,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        notes: String? = null,
        side: BusinessSide = BusinessSide.SHOP
    ): Result<ExpenseEntity> = runCatching {
        checkPermission(Permission.RECORD_EXPENSE)
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()

        database.withTransaction {
            val expNum = documentCounterDao.nextNumber(bizId, DocumentType.EXPENSE, "EXP-")
            val expId = UUID.randomUUID().toString()

            val expense = ExpenseEntity(
                id = expId,
                businessId = bizId,
                expenseNumber = expNum,
                category = category.trim(),
                title = title.trim(),
                amountCents = amount.cents,
                paymentMethod = paymentMethod,
                notes = notes?.trim()?.ifBlank { null },
                side = side,
                userId = userId,
                createdAt = System.currentTimeMillis()
            )
            expenseDao.insertExpense(expense)

            val expAccount = accountDao.getAccountByCode(expenseAccountCode(category), bizId)
            val cashAccount = accountDao.getAccountByCode(if (paymentMethod == PaymentMethod.MPESA) "1020" else "1010", bizId)

            if (expAccount != null && cashAccount != null && amount > Money.ZERO) {
                postJournalEntry(
                    businessId = bizId,
                    date = expense.createdAt,
                    description = "Expense $expNum: ${expense.title}",
                    sourceType = JournalSourceType.EXPENSE,
                    sourceId = expId,
                    side = side,
                    lines = listOf(
                        JournalLineItem(expAccount.id, expAccount.code, expAccount.name, debit = amount),
                        JournalLineItem(cashAccount.id, cashAccount.code, cashAccount.name, credit = amount)
                    )
                )
            }

            expense
        }
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> = voidExpense(expenseId, "User deleted expense")

    suspend fun voidExpense(expenseId: String, reason: String): Result<Unit> = runCatching {
        checkPermission(Permission.VOID_RECORD)
        if (reason.isBlank()) throw IllegalArgumentException("A reason is required to void an expense")
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()

        database.withTransaction {
            val expense = expenseDao.getExpenseById(expenseId, bizId)
                ?: throw IllegalArgumentException("Expense not found")
            if (expense.isVoided) throw IllegalStateException("Expense is already voided")

            expenseDao.markVoided(expenseId, bizId, reason.trim(), userId)

            // Balanced reversal of the original posting (debit cash, credit expense)
            val amount = Money(expense.amountCents)
            val expAccount = accountDao.getAccountByCode(expenseAccountCode(expense.category), bizId)
            val cashAccount = accountDao.getAccountByCode(
                if (expense.paymentMethod == PaymentMethod.MPESA) "1020" else "1010", bizId
            )
            if (expAccount != null && cashAccount != null && amount > Money.ZERO) {
                postJournalEntry(
                    businessId = bizId,
                    date = System.currentTimeMillis(),
                    description = "VOID Expense ${expense.expenseNumber} ($reason)",
                    sourceType = JournalSourceType.VOID_REVERSAL,
                    sourceId = expenseId,
                    side = expense.side,
                    lines = listOf(
                        JournalLineItem(cashAccount.id, cashAccount.code, cashAccount.name, debit = amount),
                        JournalLineItem(expAccount.id, expAccount.code, expAccount.name, credit = amount)
                    )
                )
            }

            writeAudit(
                bizId, ActionType.EXPENSE_VOIDED, userId,
                details = "Voided expense ${expense.expenseNumber}. Reason: $reason",
                before = "${expense.expenseNumber}: ${amount.formatted}"
            )
        }
    }

    // ── Wages & Layaways ─────────────────────────────────────────────────────

    suspend fun recordWagePayment(
        staffUserId: String,
        staffName: String,
        amount: Money,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        notes: String? = null
    ): Result<WagePaymentEntity> = runCatching {
        checkPermission(Permission.PAY_WAGES)
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()

        database.withTransaction {
            val wageNum = documentCounterDao.nextNumber(bizId, DocumentType.WAGE, "WAG-")
            val wageId = UUID.randomUUID().toString()

            val wage = WagePaymentEntity(
                id = wageId,
                businessId = bizId,
                paymentNumber = wageNum,
                staffUserId = staffUserId,
                staffName = staffName.trim(),
                amountCents = amount.cents,
                paymentMethod = paymentMethod,
                notes = notes?.trim()?.ifBlank { null },
                paidByUserId = userId,
                createdAt = System.currentTimeMillis()
            )
            wagePaymentDao.insertWagePayment(wage)

            val wageAccount = accountDao.getAccountByCode("6030", bizId)
            val cashAccount = accountDao.getAccountByCode(if (paymentMethod == PaymentMethod.MPESA) "1020" else "1010", bizId)

            if (wageAccount != null && cashAccount != null && amount > Money.ZERO) {
                postJournalEntry(
                    businessId = bizId,
                    date = wage.createdAt,
                    description = "Wage payment $wageNum to $staffName",
                    sourceType = JournalSourceType.WAGE,
                    sourceId = wageId,
                    side = BusinessSide.SHOP,
                    lines = listOf(
                        JournalLineItem(wageAccount.id, wageAccount.code, wageAccount.name, debit = amount),
                        JournalLineItem(cashAccount.id, cashAccount.code, cashAccount.name, credit = amount)
                    )
                )
            }

            writeAudit(
                bizId, ActionType.WAGE_PAID, userId,
                details = "Paid ${amount.formatted} to $staffName ($wageNum)"
            )

            wage
        }
    }

    suspend fun createLayaway(
        customerName: String,
        customerPhone: String,
        itemDescription: String,
        totalAmount: Money,
        depositAmount: Money,
        dueDate: Long
    ): Result<LayawayEntity> = runCatching {
        checkPermission(Permission.MAKE_SALE)
        val bizId = getActiveBusinessId()
        val balanceDue = (totalAmount - depositAmount).coerceAtLeast(Money.ZERO)

        database.withTransaction {
            val layNum = documentCounterDao.nextNumber(bizId, DocumentType.LAYAWAY, "LAY-")
            val layaway = LayawayEntity(
                id = UUID.randomUUID().toString(),
                businessId = bizId,
                layawayNumber = layNum,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                itemDescription = itemDescription.trim(),
                totalAmountCents = totalAmount.cents,
                depositAmountCents = depositAmount.cents,
                balanceDueCents = balanceDue.cents,
                dueDate = dueDate,
                status = if (balanceDue == Money.ZERO) "COMPLETED" else "ACTIVE"
            )
            layawayDao.insertLayaway(layaway)
            layaway
        }
    }

    suspend fun recordLayawayPayment(
        layawayId: String,
        paymentAmount: Money,
        paymentMethod: PaymentMethod = PaymentMethod.CASH
    ): Result<Unit> = runCatching {
        checkPermission(Permission.TAKE_PAYMENT)
        database.withTransaction {
            val layaway = layawayDao.getLayawayById(layawayId)
                ?: throw IllegalArgumentException("Layaway not found")
            val newDeposit = Money(layaway.depositAmountCents) + paymentAmount
            val newBalance = (Money(layaway.totalAmountCents) - newDeposit).coerceAtLeast(Money.ZERO)
            val updated = layaway.copy(
                depositAmountCents = newDeposit.cents,
                balanceDueCents = newBalance.cents,
                status = if (newBalance == Money.ZERO) "COMPLETED" else "ACTIVE"
            )
            layawayDao.updateLayaway(updated)
        }
    }

    // ── Invoicing & Accounts Receivable ──────────────────────────────────────

    suspend fun createInvoice(
        customerId: String?,
        customerName: String,
        customerPhone: String,
        items: List<InvoiceLineItem>,
        side: BusinessSide = BusinessSide.SHOP
    ): Result<InvoiceEntity> = runCatching {
        val bizId = getActiveBusinessId()
        val total = items.fold(Money.ZERO) { acc, i -> acc + i.total }

        database.withTransaction {
            val invNum = documentCounterDao.nextNumber(bizId, DocumentType.INVOICE, "INV-")
            val invId = UUID.randomUUID().toString()

            val invoice = InvoiceEntity(
                id = invId,
                businessId = bizId,
                invoiceNumber = invNum,
                customerId = customerId,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                issueDate = System.currentTimeMillis(),
                dueDate = System.currentTimeMillis() + (30L * 24 * 3600 * 1000),
                totalAmountCents = total.cents,
                paidAmountCents = 0L,
                balanceDueCents = total.cents,
                status = InvoiceStatus.UNPAID,
                side = side
            )
            invoiceDao.insertInvoice(invoice)

            val childItems = items.map { i ->
                InvoiceItemEntity(
                    id = UUID.randomUUID().toString(),
                    invoiceId = invId,
                    itemId = i.itemId,
                    description = i.description,
                    quantity = i.qty,
                    unitPriceCents = i.unitPrice.cents,
                    costPriceCents = i.costPrice.cents,
                    subtotalCents = i.total.cents
                )
            }
            invoiceItemDao.insertInvoiceItems(childItems)

            if (customerId != null) {
                customerDao.updateBalanceCents(customerId, bizId, total.cents)
            }

            val arAccount = accountDao.getAccountByCode("1200", bizId)
            val revAccount = accountDao.getAccountByCode(if (side == BusinessSide.GARAGE) "4020" else "4010", bizId)

            if (arAccount != null && revAccount != null && total > Money.ZERO) {
                postJournalEntry(
                    businessId = bizId,
                    date = invoice.issueDate,
                    description = "Invoice $invNum for $customerName",
                    sourceType = JournalSourceType.INVOICE,
                    sourceId = invId,
                    side = side,
                    lines = listOf(
                        JournalLineItem(arAccount.id, arAccount.code, arAccount.name, debit = total),
                        JournalLineItem(revAccount.id, revAccount.code, revAccount.name, credit = total)
                    )
                )
            }

            invoice
        }
    }

    suspend fun recordInvoicePayment(
        invoiceId: String,
        amount: Money,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        reference: String? = null,
        description: String? = null
    ): Result<Unit> = runCatching {
        checkPermission(Permission.TAKE_PAYMENT)
        val bizId = getActiveBusinessId()
        database.withTransaction {
            val inv = invoiceDao.getInvoiceById(invoiceId, bizId)
                ?: throw IllegalArgumentException("Invoice not found")

            val newPaid = Money(inv.paidAmountCents) + amount
            val newBalance = (Money(inv.totalAmountCents) - newPaid).coerceAtLeast(Money.ZERO)
            val newStatus = if (newBalance == Money.ZERO) InvoiceStatus.PAID else InvoiceStatus.PARTIALLY_PAID

            invoiceDao.recordInvoicePayment(invoiceId, bizId, amount.cents, newStatus)

            if (inv.customerId != null) {
                customerDao.updateBalanceCents(inv.customerId, bizId, -amount.cents)
            }

            val cashAccount = accountDao.getAccountByCode(if (paymentMethod == PaymentMethod.MPESA) "1020" else "1010", bizId)
            val arAccount = accountDao.getAccountByCode("1200", bizId)

            if (cashAccount != null && arAccount != null && amount > Money.ZERO) {
                postJournalEntry(
                    businessId = bizId,
                    date = System.currentTimeMillis(),
                    description = "Payment on ${inv.invoiceNumber} (${reference ?: paymentMethod.name})",
                    sourceType = JournalSourceType.CUSTOMER_PAYMENT,
                    sourceId = invoiceId,
                    side = inv.side,
                    lines = listOf(
                        JournalLineItem(cashAccount.id, cashAccount.code, cashAccount.name, debit = amount),
                        JournalLineItem(arAccount.id, arAccount.code, arAccount.name, credit = amount)
                    )
                )
            }
        }
    }

    // ── Supplier Management ──────────────────────────────────────────────────

    suspend fun createSupplier(name: String, phone: String, email: String?): Result<SupplierEntity> = runCatching {
        val bizId = getActiveBusinessId()
        val supplier = SupplierEntity(
            id = UUID.randomUUID().toString(),
            businessId = bizId,
            name = name.trim(),
            phone = phone.trim(),
            balanceCents = 0L
        )
        supplierDao.insertSupplier(supplier)
        supplier
    }

    // ── Staff Management & Security ──────────────────────────────────────────

    suspend fun inviteOrAddStaff(name: String, email: String, phone: String, role: Role): Result<Unit> = runCatching {
        checkPermission(Permission.MANAGE_STAFF)
        if (role == Role.OWNER) throw IllegalArgumentException("A business can only have one owner")
        val bizId = getActiveBusinessId()
        val cleanPhone = phone.trim()

        var user = userDao.getUserByPhone(cleanPhone)
        if (user == null) {
            val newUserId = UUID.randomUUID().toString()
            user = UserEntity(
                id = newUserId,
                name = name.trim(),
                phone = cleanPhone,
                pinSalt = "",
                pinHash = ""
            )
            userDao.insertUser(user)
        }

        val existingMember = businessMemberDao.getMember(bizId, user.id)
        if (existingMember != null) {
            throw IllegalStateException("User is already a member of this business")
        }

        val member = BusinessMemberEntity(
            businessId = bizId,
            userId = user.id,
            role = role,
            canSellNegativeStock = (role == Role.OWNER || role == Role.MANAGER)
        )
        businessMemberDao.insertMember(member)
    }

    suspend fun updateStaffRole(userId: String, newRole: Role): Result<Unit> = runCatching {
        checkPermission(Permission.MANAGE_STAFF)
        if (newRole == Role.OWNER) throw IllegalArgumentException("A business can only have one owner")
        val bizId = getActiveBusinessId()
        val member = businessMemberDao.getMember(bizId, userId)
            ?: throw IllegalArgumentException("Staff member not found")
        if (member.role == Role.OWNER) throw IllegalStateException("The owner's role cannot be changed")
        businessMemberDao.updateMember(
            member.copy(role = newRole, updatedAt = System.currentTimeMillis())
        )
        writeAudit(
            bizId, ActionType.ROLE_CHANGED, getActiveUserId(),
            details = "Role changed to ${newRole.name}",
            before = member.role.name
        )
    }

    suspend fun removeStaff(userId: String): Result<Unit> = runCatching {
        checkPermission(Permission.MANAGE_STAFF)
        val bizId = getActiveBusinessId()
        val member = businessMemberDao.getMember(bizId, userId)
            ?: throw IllegalArgumentException("Staff member not found")
        if (member.role == Role.OWNER) throw IllegalStateException("The owner cannot be removed")
        businessMemberDao.deleteMember(bizId, userId)
    }

    // ── Financial Reports Engine (Computed via Money) ─────────────────────────

    suspend fun generateTrialBalance(): Result<TrialBalanceReport> = runCatching {
        val bizId = getActiveBusinessId()
        val accounts = accountDao.getAccounts(bizId)

        var totalDebit = Money.ZERO
        var totalCredit = Money.ZERO
        val items = accounts.map { acc ->
            val moneyBalance = Money(acc.balanceCents)
            val debit: Money
            val credit: Money
            when (acc.category) {
                AccountCategory.ASSET, AccountCategory.EXPENSE -> {
                    debit = moneyBalance
                    credit = Money.ZERO
                    totalDebit += debit
                }
                AccountCategory.LIABILITY, AccountCategory.EQUITY, AccountCategory.INCOME -> {
                    debit = Money.ZERO
                    credit = moneyBalance
                    totalCredit += credit
                }
            }
            TrialBalanceItem(acc.code, acc.name, acc.category, debit, credit)
        }

        TrialBalanceReport(
            items = items,
            totalDebit = totalDebit,
            totalCredit = totalCredit,
            isBalanced = totalDebit == totalCredit
        )
    }

    suspend fun generateProfitAndLoss(): Result<ProfitAndLossReport> = runCatching {
        val bizId = getActiveBusinessId()
        val accounts = accountDao.getAccounts(bizId)

        val revenues = accounts.filter { it.category == AccountCategory.INCOME }
            .map { AccountAmountItem(it.code, it.name, Money(it.balanceCents)) }
        val totalRevenue = revenues.fold(Money.ZERO) { acc, i -> acc + i.amount }

        val cogs = accounts.filter { it.category == AccountCategory.EXPENSE && it.subCategory == AccountSubCategory.DIRECT_COST }
            .map { AccountAmountItem(it.code, it.name, Money(it.balanceCents)) }
        val totalCogs = cogs.fold(Money.ZERO) { acc, i -> acc + i.amount }

        val opEx = accounts.filter { it.category == AccountCategory.EXPENSE && it.subCategory != AccountSubCategory.DIRECT_COST }
            .map { AccountAmountItem(it.code, it.name, Money(it.balanceCents)) }
        val totalOpEx = opEx.fold(Money.ZERO) { acc, i -> acc + i.amount }

        val gross = totalRevenue - totalCogs
        val net = gross - totalOpEx

        ProfitAndLossReport(
            revenueItems = revenues,
            totalRevenue = totalRevenue,
            costOfSalesItems = cogs,
            totalCostOfSales = totalCogs,
            grossProfit = gross,
            operatingExpenses = opEx,
            totalOperatingExpenses = totalOpEx,
            netProfit = net
        )
    }

    suspend fun generateBalanceSheet(): Result<BalanceSheetReport> = runCatching {
        val bizId = getActiveBusinessId()
        val accounts = accountDao.getAccounts(bizId)

        val curAssets = accounts.filter { it.category == AccountCategory.ASSET && it.subCategory == AccountSubCategory.CURRENT_ASSET }
            .map { AccountAmountItem(it.code, it.name, Money(it.balanceCents)) }
        val totCurAssets = curAssets.fold(Money.ZERO) { acc, i -> acc + i.amount }

        val fixAssets = accounts.filter { it.category == AccountCategory.ASSET && it.subCategory == AccountSubCategory.FIXED_ASSET }
            .map { AccountAmountItem(it.code, it.name, Money(it.balanceCents)) }
        val totFixAssets = fixAssets.fold(Money.ZERO) { acc, i -> acc + i.amount }

        val curLiab = accounts.filter { it.category == AccountCategory.LIABILITY && it.subCategory == AccountSubCategory.CURRENT_LIABILITY }
            .map { AccountAmountItem(it.code, it.name, Money(it.balanceCents)) }
        val totCurLiab = curLiab.fold(Money.ZERO) { acc, i -> acc + i.amount }

        val ltLiab = accounts.filter { it.category == AccountCategory.LIABILITY && it.subCategory == AccountSubCategory.LONG_TERM_LIABILITY }
            .map { AccountAmountItem(it.code, it.name, Money(it.balanceCents)) }
        val totLtLiab = ltLiab.fold(Money.ZERO) { acc, i -> acc + i.amount }

        val equity = accounts.filter { it.category == AccountCategory.EQUITY }
            .map { AccountAmountItem(it.code, it.name, Money(it.balanceCents)) }
        val totEquity = equity.fold(Money.ZERO) { acc, i -> acc + i.amount }

        val pnl = generateProfitAndLoss().getOrNull()
        val currentNetProfit = pnl?.netProfit ?: Money.ZERO

        BalanceSheetReport(
            currentAssets = curAssets,
            totalCurrentAssets = totCurAssets,
            fixedAssets = fixAssets,
            totalFixedAssets = totFixAssets,
            currentLiabilities = curLiab,
            totalCurrentLiabilities = totCurLiab,
            longTermLiabilities = ltLiab,
            totalLongTermLiabilities = totLtLiab,
            equityItems = equity,
            currentPeriodNetProfit = currentNetProfit,
            totalEquity = totEquity + currentNetProfit
        )
    }

    suspend fun generateAccountsReceivableAgeing(): Result<AccountsReceivableAgeingReport> = runCatching {
        val bizId = getActiveBusinessId()
        val customers = customerDao.getCustomers(bizId).filter { it.balanceCents > 0 }
        val items = customers.map { c ->
            val b = AgeingBucket(current = Money(c.balanceCents))
            EntityAgeingItem(c.id, c.name, c.phone, b)
        }
        val total = items.fold(Money.ZERO) { acc, i -> acc + i.bucket.total }
        AccountsReceivableAgeingReport(
            overall = AgeingBucket(current = total),
            customers = items
        )
    }

    suspend fun generateAccountsPayableAgeing(): Result<AccountsPayableAgeingReport> = runCatching {
        val bizId = getActiveBusinessId()
        val suppliers = supplierDao.getSuppliers(bizId).filter { it.balanceCents > 0 }
        val items = suppliers.map { s ->
            val b = AgeingBucket(current = Money(s.balanceCents))
            EntityAgeingItem(s.id, s.name, s.phone, b)
        }
        val total = items.fold(Money.ZERO) { acc, i -> acc + i.bucket.total }
        AccountsPayableAgeingReport(
            overall = AgeingBucket(current = total),
            suppliers = items
        )
    }

    suspend fun recordAccountTransfer(
        fromAccountCode: String,
        toAccountCode: String,
        amount: Money,
        reference: String?,
        description: String?
    ): Result<Unit> = runCatching {
        checkPermission(Permission.RECORD_EXPENSE)
        val bizId = getActiveBusinessId()
        val from = accountDao.getAccountByCode(fromAccountCode, bizId)
            ?: throw IllegalArgumentException("Source account $fromAccountCode not found")
        val to = accountDao.getAccountByCode(toAccountCode, bizId)
            ?: throw IllegalArgumentException("Destination account $toAccountCode not found")

        postJournalEntry(
            businessId = bizId,
            date = System.currentTimeMillis(),
            description = description ?: "Transfer from ${from.name} to ${to.name}",
            sourceType = JournalSourceType.TRANSFER,
            sourceId = reference,
            side = BusinessSide.SHOP,
            lines = listOf(
                JournalLineItem(to.id, to.code, to.name, debit = amount),
                JournalLineItem(from.id, from.code, from.name, credit = amount)
            )
        )
        Unit
    }

    // ── Backwards-compatibility and No-op Stubs ──────────────────────────────

    suspend fun ensureDefaultAccounts(businessId: String) {
        val existing = accountDao.getAccounts(businessId)
        if (existing.isEmpty()) {
            val defs = com.example.businessos.data.local.DefaultAccounts.createDefaultAccounts(businessId)
            accountDao.insertAccounts(defs)
        }
    }

    suspend fun seedDemoData(type: BusinessType? = null) {
        // Safe no-op in production baseline; Demo data is isolated to DEMO workspace
    }
}
