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
                    StockMovementEntity(
                        businessId = bizId,
                        itemId = itemId,
                        movementType = StockMovementType.OPENING,
                        deltaQty = initialStock,
                        balanceAfter = initialStock,
                        reference = "Initial opening stock",
                        recordedByUserId = getActiveUserId()
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
        val bizId = getActiveBusinessId()
        database.withTransaction {
            val item = itemDao.getItemById(itemId, bizId)
                ?: throw IllegalArgumentException("Item not found")
            val delta = physicalCount - item.stockQty
            itemDao.adjustStock(itemId, bizId, delta)
            stockMovementDao.insertMovement(
                StockMovementEntity(
                    businessId = bizId,
                    itemId = itemId,
                    movementType = StockMovementType.ADJUSTMENT,
                    deltaQty = delta,
                    balanceAfter = physicalCount,
                    reference = reason.ifBlank { "Stock count adjustment" },
                    recordedByUserId = getActiveUserId()
                )
            )
            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = bizId,
                    userId = getActiveUserId(),
                    userName = getSession().name,
                    actionType = ActionType.STOCK_ADJUSTED,
                    entityId = itemId,
                    details = "Adjusted ${item.name} stock from ${item.stockQty} to $physicalCount. Reason: $reason"
                )
            )
        }
    }

    // ── Journal Posting Engine (Debits == Credits Invariant) ──────────────────

    suspend fun postJournalEntry(
        businessId: String,
        date: Long,
        description: String,
        sourceType: JournalSourceType,
        sourceId: String?,
        side: BusinessSide,
        lines: List<JournalLineItem>
    ): Result<JournalEntryEntity> = runCatching {
        val totalDebit = lines.fold(Money.ZERO) { acc, l -> acc + l.debit }
        val totalCredit = lines.fold(Money.ZERO) { acc, l -> acc + l.credit }

        if (totalDebit != totalCredit || totalDebit <= Money.ZERO) {
            throw IllegalArgumentException(
                "Unbalanced journal: total debits (${totalDebit.formatted}) must equal total credits (${totalCredit.formatted}) and be greater than 0"
            )
        }

        database.withTransaction {
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
                    entryId = entryId,
                    accountId = l.accountId,
                    debitCents = l.debit.cents,
                    creditCents = l.credit.cents,
                    narration = l.narration
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
                    saleId = saleId,
                    itemId = l.itemId,
                    itemName = l.itemName,
                    qty = l.qty,
                    unitPriceCents = l.unitPrice.cents,
                    costPriceCents = l.costPrice.cents,
                    subtotalCents = l.subtotal.cents,
                    priceType = l.priceType
                )
            }
            saleItemDao.insertSaleItems(childItems)

            // Deduct stock and write stock movements
            for (line in items) {
                itemDao.adjustStock(line.itemId, bizId, -line.qty.toLong())
                val currentItem = itemDao.getItemById(line.itemId, bizId)
                val balanceAfter = currentItem?.stockQty ?: 0L
                stockMovementDao.insertMovement(
                    StockMovementEntity(
                        businessId = bizId,
                        itemId = line.itemId,
                        movementType = StockMovementType.SALE,
                        deltaQty = -line.qty.toLong(),
                        balanceAfter = balanceAfter,
                        reference = "Sale $saleNum",
                        recordedByUserId = userId
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

            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = bizId,
                    userId = userId,
                    userName = getSession().name,
                    actionType = ActionType.PRICE_OVERRIDE,
                    entityId = saleId,
                    details = "Recorded sale $saleNum for ${total.formatted} via $paymentMethod"
                )
            )

            sale
        }
    }

    suspend fun voidSale(saleId: String, reason: String): Result<Unit> = runCatching {
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
                itemDao.adjustStock(item.itemId, bizId, item.qty.toLong())
                val current = itemDao.getItemById(item.itemId, bizId)
                stockMovementDao.insertMovement(
                    StockMovementEntity(
                        businessId = bizId,
                        itemId = item.itemId,
                        movementType = StockMovementType.VOID_SALE,
                        deltaQty = item.qty.toLong(),
                        balanceAfter = current?.stockQty ?: 0L,
                        reference = "Void Sale ${sale.saleNumber}: $reason",
                        recordedByUserId = userId
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

            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = bizId,
                    userId = userId,
                    userName = getSession().name,
                    actionType = ActionType.SALE_VOIDED,
                    entityId = saleId,
                    details = "Voided sale ${sale.saleNumber}. Reason: $reason"
                )
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
        val bizId = getActiveBusinessId()
        val customerId = UUID.randomUUID().toString()
        val customer = CustomerEntity(
            id = customerId,
            businessId = bizId,
            name = name.trim(),
            phone = phone.trim(),
            balanceCents = initialCredit.cents,
            dueDate = dueDate,
            notes = notes?.trim()?.ifBlank { null }
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
            customer.copy(dueDate = dueDate, notes = notes?.trim()?.ifBlank { null })
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
        targetDeliveryDate: Long? = null
    ): Result<JobCardEntity> = runCatching {
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()
        val jobId = UUID.randomUUID().toString()

        database.withTransaction {
            val jobNum = documentCounterDao.nextNumber(bizId, DocumentType.INVOICE, "JOB-")
            val jobCard = JobCardEntity(
                id = jobId,
                businessId = bizId,
                jobNumber = jobNum,
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
                    jobCardId = jobId,
                    toStage = JobStage.INTAKE,
                    note = "Initial intake"
                )
            )
            jobCard
        }
    }

    suspend fun updateJobParts(jobCardId: String, newParts: List<PartItem>): Result<Unit> = runCatching {
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()

        database.withTransaction {
            val job = jobCardDao.getJobCardById(jobCardId, bizId)
                ?: throw IllegalArgumentException("Job card not found")

            val existingParts = jobPartDao.getByJob(jobCardId)
            // Restore stock for removed parts that had stock deducted
            for (p in existingParts) {
                if (p.stockDeducted && p.itemId != null && newParts.none { it.id == p.id }) {
                    itemDao.adjustStock(p.itemId, bizId, p.qty.toLong())
                    val cur = itemDao.getItemById(p.itemId, bizId)
                    stockMovementDao.insertMovement(
                        StockMovementEntity(
                            businessId = bizId,
                            itemId = p.itemId,
                            movementType = StockMovementType.VOID_JOB,
                            deltaQty = p.qty.toLong(),
                            balanceAfter = cur?.stockQty ?: 0L,
                            reference = "Removed from Job ${job.jobNumber}",
                            recordedByUserId = userId
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
                        StockMovementEntity(
                            businessId = bizId,
                            itemId = p.itemId,
                            movementType = StockMovementType.JOB_PART,
                            deltaQty = -p.quantity.toLong(),
                            balanceAfter = cur?.stockQty ?: 0L,
                            reference = "Assigned to Job ${job.jobNumber}",
                            recordedByUserId = userId
                        )
                    )
                    stockDeducted = true
                }

                JobPartEntity(
                    id = p.id,
                    jobCardId = jobCardId,
                    itemId = p.itemId,
                    name = p.name,
                    unitPriceCents = p.unitPrice.cents,
                    costPriceCents = p.costPrice.cents,
                    qty = p.quantity,
                    stockDeducted = stockDeducted
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
        val bizId = getActiveBusinessId()
        database.withTransaction {
            val job = jobCardDao.getJobCardById(jobCardId, bizId)
                ?: throw IllegalArgumentException("Job card not found")
            val next = job.stage.next() ?: return@withTransaction job.stage

            jobCardDao.updateStage(jobCardId, bizId, next)
            jobStageTransitionDao.insertTransition(
                JobStageTransitionEntity(
                    jobCardId = jobCardId,
                    toStage = next,
                    note = note
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
                invRes.onSuccess { inv ->
                    jobCardDao.linkInvoice(jobCardId, bizId, inv.id)
                }
            }
            next
        }
    }

    suspend fun reopenJobStage(jobCardId: String, targetStage: JobStage, reason: String?): Result<Unit> = runCatching {
        val bizId = getActiveBusinessId()
        database.withTransaction {
            jobCardDao.updateStage(jobCardId, bizId, targetStage)
            jobStageTransitionDao.insertTransition(
                JobStageTransitionEntity(
                    jobCardId = jobCardId,
                    toStage = targetStage,
                    note = reason
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

            val expAccCode = when (category.lowercase()) {
                "rent" -> "6010"
                "utilities", "electricity", "water" -> "6020"
                "salary", "wage" -> "6030"
                "transport", "delivery" -> "6040"
                "food", "meals" -> "6050"
                else -> "6070"
            }
            val expAccount = accountDao.getAccountByCode(expAccCode, bizId)
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

            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = bizId,
                    userId = userId,
                    userName = getSession().name,
                    actionType = ActionType.PRICE_OVERRIDE,
                    entityId = expId,
                    details = "Recorded expense $expNum for ${amount.formatted}"
                )
            )

            expense
        }
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> = voidExpense(expenseId, "User deleted expense")

    suspend fun voidExpense(expenseId: String, reason: String): Result<Unit> = runCatching {
        val bizId = getActiveBusinessId()
        val userId = getActiveUserId()

        database.withTransaction {
            val expense = expenseDao.observeExpenses(bizId) // check existence
            expenseDao.markVoided(expenseId, bizId, reason.trim(), userId)

            auditLogDao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = bizId,
                    userId = userId,
                    userName = getSession().name,
                    actionType = ActionType.EXPENSE_VOIDED,
                    entityId = expenseId,
                    details = "Voided expense $expenseId. Reason: $reason"
                )
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
                    invoiceId = invId,
                    itemId = i.itemId,
                    description = i.description,
                    qty = i.qty,
                    unitPriceCents = i.unitPrice.cents,
                    costPriceCents = i.costPrice.cents,
                    totalCents = i.total.cents
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
        val bizId = getActiveBusinessId()
        val member = businessMemberDao.getMember(bizId, userId)
            ?: throw IllegalArgumentException("Staff member not found")
        businessMemberDao.updateMember(member.copy(role = newRole))
    }

    suspend fun removeStaff(userId: String): Result<Unit> = runCatching {
        val bizId = getActiveBusinessId()
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
