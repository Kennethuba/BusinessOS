package com.example.businessos.data.repository

import com.example.businessos.data.local.dao.AuditLogDao
import com.example.businessos.data.local.dao.BusinessDao
import com.example.businessos.data.local.dao.BusinessMemberDao
import com.example.businessos.data.local.dao.CustomerDao
import com.example.businessos.data.local.dao.ExpenseDao
import com.example.businessos.data.local.dao.ItemDao
import com.example.businessos.data.local.dao.JobCardDao
import com.example.businessos.data.local.dao.MemberWithUser
import com.example.businessos.data.local.dao.SaleDao
import com.example.businessos.data.local.dao.UserDao
import com.example.businessos.data.local.entity.AuditLogEntity
import com.example.businessos.data.local.entity.BusinessEntity
import com.example.businessos.data.local.entity.BusinessMemberEntity
import com.example.businessos.data.local.entity.CustomerEntity
import com.example.businessos.data.local.entity.ExpenseEntity
import com.example.businessos.data.local.entity.ItemEntity
import com.example.businessos.data.local.entity.JobCardEntity
import com.example.businessos.data.local.entity.SaleEntity
import com.example.businessos.data.local.entity.UserEntity
import com.example.businessos.data.model.ActionType
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.model.JobStage
import com.example.businessos.data.model.PartItem
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.Role
import com.example.businessos.data.model.SaleLineItem
import com.example.businessos.data.model.StageTransition
import com.example.businessos.data.model.SubscriptionPlan
import com.example.businessos.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

import com.example.businessos.data.local.dao.AccountDao
import com.example.businessos.data.local.dao.AssetDao
import com.example.businessos.data.local.dao.BillDao
import com.example.businessos.data.local.dao.InvoiceDao
import com.example.businessos.data.local.dao.JournalEntryDao
import com.example.businessos.data.local.dao.LiabilityDao
import com.example.businessos.data.local.dao.PaymentDao
import com.example.businessos.data.local.dao.SupplierDao
import com.example.businessos.data.local.dao.WagePaymentDao
import com.example.businessos.data.local.dao.LayawayDao
import com.example.businessos.data.local.entity.AccountEntity
import com.example.businessos.data.local.entity.AssetEntity
import com.example.businessos.data.local.entity.BillEntity
import com.example.businessos.data.local.entity.InvoiceEntity
import com.example.businessos.data.local.entity.JournalEntryEntity
import com.example.businessos.data.local.entity.LiabilityEntity
import com.example.businessos.data.local.entity.PaymentEntity
import com.example.businessos.data.local.entity.SupplierEntity
import com.example.businessos.data.local.entity.WagePaymentEntity
import com.example.businessos.data.local.entity.LayawayEntity
import com.example.businessos.data.model.AccountAmountItem
import com.example.businessos.data.model.AccountCategory
import com.example.businessos.data.model.AccountSubCategory
import com.example.businessos.data.model.AccountsPayableAgeingReport
import com.example.businessos.data.model.AccountsReceivableAgeingReport
import com.example.businessos.data.model.AgeingBucket
import com.example.businessos.data.model.BalanceSheetReport
import com.example.businessos.data.model.BillStatus
import com.example.businessos.data.model.DepreciationMethod
import com.example.businessos.data.model.EntityAgeingItem
import com.example.businessos.data.model.InvoiceLineItem
import com.example.businessos.data.model.InvoiceStatus
import com.example.businessos.data.model.JournalLineItem
import com.example.businessos.data.model.JournalSourceType
import com.example.businessos.data.model.PaymentType
import com.example.businessos.data.model.ProfitAndLossReport
import com.example.businessos.data.model.TrialBalanceItem
import com.example.businessos.data.model.TrialBalanceReport

class BusinessOsRepository(
    private val authRepository: AuthRepository,
    private val businessDao: BusinessDao,
    private val businessMemberDao: BusinessMemberDao,
    private val userDao: UserDao,
    private val itemDao: ItemDao,
    private val saleDao: SaleDao,
    private val customerDao: CustomerDao,
    private val jobCardDao: JobCardDao,
    private val auditLogDao: AuditLogDao,
    private val expenseDao: ExpenseDao,
    private val accountDao: AccountDao,
    private val journalEntryDao: JournalEntryDao,
    private val invoiceDao: InvoiceDao,
    private val supplierDao: SupplierDao,
    private val billDao: BillDao,
    private val paymentDao: PaymentDao,
    private val assetDao: AssetDao,
    private val liabilityDao: LiabilityDao,
    private val wagePaymentDao: WagePaymentDao,
    private val layawayDao: LayawayDao
) {
    private fun getSession(): CurrentUserSession {
        return authRepository.session.value
            ?: throw SecurityException("User is not authenticated")
    }

    private fun getActiveBusinessId(): String {
        return getSession().activeBusinessId
            ?: throw IllegalStateException("No active business selected")
    }

    private suspend fun getActiveRole(businessId: String, userId: String): Role {
        val member = businessMemberDao.getMember(businessId, userId)
            ?: throw SecurityException("User is not a member of this business")
        return member.role
    }

    private suspend fun logAudit(
        businessId: String,
        actionType: ActionType,
        beforeValue: String,
        afterValue: String
    ) {
        val session = getSession()
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                businessId = businessId,
                actorUserId = session.userId,
                actorName = session.name,
                actionType = actionType,
                beforeValue = beforeValue,
                afterValue = afterValue,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // ==========================================
    // AUTO GARAGE REPOSITORY
    // ==========================================

    fun observeJobCards(stage: JobStage? = null): Flow<List<JobCardEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return if (stage != null) {
            jobCardDao.observeJobCardsByStage(businessId, stage)
        } else {
            jobCardDao.observeJobCardsForBusiness(businessId)
        }
    }

    fun observeJobCard(id: String): Flow<JobCardEntity?> {
        val businessId = getSession().activeBusinessId ?: return flowOf(null)
        return jobCardDao.observeJobCardById(id, businessId)
    }

    fun observeVehicleHistory(vehiclePlate: String): Flow<List<JobCardEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return jobCardDao.observeVehicleHistory(businessId, vehiclePlate)
    }

    suspend fun createJobCard(
        vehiclePlate: String,
        customerName: String,
        customerPhone: String,
        description: String,
        assignedStaffId: String? = null,
        initialParts: List<PartItem> = emptyList()
    ): Result<JobCardEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val total = initialParts.sumOf { it.cost }
            val card = JobCardEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                vehiclePlate = vehiclePlate.trim().uppercase(),
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                description = description.trim(),
                stage = JobStage.INTAKE,
                assignedStaffId = assignedStaffId,
                partsList = initialParts,
                estimateTotal = total,
                stageHistory = listOf(StageTransition(JobStage.INTAKE, note = "Vehicle checked into intake"))
            )
            jobCardDao.insertJobCard(card)
            Result.success(card)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun advanceJobStage(jobCardId: String, note: String? = null): Result<JobCardEntity> {
        return try {
            val businessId = getActiveBusinessId()
            val card = jobCardDao.getJobCardById(jobCardId, businessId)
                ?: return Result.failure(Exception("Job card not found"))

            val nextStage = card.stage.next()
                ?: return Result.failure(Exception("Job card is already at final stage (${card.stage.title})"))

            val updatedHistory = card.stageHistory + StageTransition(nextStage, note = note)
            val updated = card.copy(
                stage = nextStage,
                stageHistory = updatedHistory,
                syncStatus = SyncStatus.PENDING,
                updatedAt = System.currentTimeMillis()
            )
            jobCardDao.updateJobCard(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reopenJobStage(jobCardId: String, targetStage: JobStage, reason: String): Result<JobCardEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val role = getActiveRole(businessId, session.userId)

            if (role == Role.STAFF) {
                return Result.failure(SecurityException("Staff cannot reopen or roll back job cards"))
            }

            val card = jobCardDao.getJobCardById(jobCardId, businessId)
                ?: return Result.failure(Exception("Job card not found"))

            val beforeStage = card.stage
            val updatedHistory = card.stageHistory + StageTransition(
                targetStage,
                note = "Reopened backwards: $reason by ${session.name}"
            )
            val updated = card.copy(
                stage = targetStage,
                stageHistory = updatedHistory,
                syncStatus = SyncStatus.PENDING,
                updatedAt = System.currentTimeMillis()
            )
            jobCardDao.updateJobCard(updated)

            // Audit log JOB_CANCELLED / Rollback
            logAudit(
                businessId = businessId,
                actionType = ActionType.JOB_CANCELLED,
                beforeValue = "Job ${card.vehiclePlate} at ${beforeStage.title}",
                afterValue = "Rolled back to ${targetStage.title}. Reason: $reason"
            )

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJobParts(jobCardId: String, parts: List<PartItem>): Result<JobCardEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val role = getActiveRole(businessId, session.userId)

            val card = jobCardDao.getJobCardById(jobCardId, businessId)
                ?: return Result.failure(Exception("Job card not found"))

            val oldTotal = card.estimateTotal
            val newTotal = parts.sumOf { it.cost }

            if (role == Role.STAFF && oldTotal > 0 && oldTotal != newTotal) {
                return Result.failure(SecurityException("Staff cannot modify job estimate totals"))
            }

            val updated = card.copy(
                partsList = parts,
                estimateTotal = newTotal,
                syncStatus = SyncStatus.PENDING,
                updatedAt = System.currentTimeMillis()
            )
            jobCardDao.updateJobCard(updated)

            if (oldTotal != newTotal && (role == Role.MANAGER || role == Role.OWNER)) {
                logAudit(
                    businessId = businessId,
                    actionType = ActionType.PRICE_OVERRIDE,
                    beforeValue = "Job ${card.vehiclePlate} Estimate: KSh $oldTotal",
                    afterValue = "New Estimate: KSh $newTotal (${parts.size} parts)"
                )
            }

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // KARIAKOR STALL (POS) REPOSITORY
    // ==========================================

    fun observeItems(): Flow<List<ItemEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return itemDao.observeItemsForBusiness(businessId)
    }

    suspend fun createItem(
        name: String,
        retailPrice: Double,
        wholesalePrice: Double,
        stockQty: Int,
        minStockLevel: Int = 3
    ): Result<ItemEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val role = getActiveRole(businessId, session.userId)

            if (role == Role.STAFF) {
                return Result.failure(SecurityException("Staff cannot create new inventory items"))
            }

            val item = ItemEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                name = name.trim(),
                retailPrice = retailPrice,
                wholesalePrice = wholesalePrice,
                stockQty = stockQty,
                minStockLevel = minStockLevel,
                syncStatus = SyncStatus.PENDING
            )
            itemDao.insertItem(item)

            logAudit(
                businessId = businessId,
                actionType = ActionType.STOCK_EDITED,
                beforeValue = "Created item ${item.name}",
                afterValue = "Retail: $retailPrice, Wholesale: $wholesalePrice, Stock: $stockQty"
            )

            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordSale(
        items: List<SaleLineItem>,
        paymentMethod: PaymentMethod,
        customerId: String? = null,
        mpesaCode: String? = null,
        priceOverridden: Boolean = false
    ): Result<SaleEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val role = getActiveRole(businessId, session.userId)

            if (items.isEmpty()) {
                return Result.failure(IllegalArgumentException("Cart cannot be empty"))
            }

            if (paymentMethod == PaymentMethod.CREDIT && customerId.isNullOrEmpty()) {
                return Result.failure(IllegalArgumentException("Customer is required for credit (Deni) sales"))
            }

            if (priceOverridden && role == Role.STAFF) {
                return Result.failure(SecurityException("Staff members are not permitted to override prices"))
            }

            val total = items.sumOf { it.subtotal }

            val sale = SaleEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                items = items,
                total = total,
                paymentMethod = paymentMethod,
                customerId = customerId,
                mpesaCode = mpesaCode,
                createdByUserId = session.userId,
                syncStatus = SyncStatus.PENDING
            )
            saleDao.insertSale(sale)

            for (line in items) {
                itemDao.decrementStock(line.itemId, businessId, line.qty)
            }

            if (paymentMethod == PaymentMethod.CREDIT && !customerId.isNullOrEmpty()) {
                customerDao.updateBalance(customerId, businessId, total)
            }

            if (priceOverridden) {
                logAudit(
                    businessId = businessId,
                    actionType = ActionType.PRICE_OVERRIDE,
                    beforeValue = "Standard Line items",
                    afterValue = "Custom override sale total: KSh $total"
                )
            }

            // Post double entry journal entry to General Ledger
            ensureDefaultAccounts(businessId)
            val revAccount = accountDao.getAccountByCode("4010", businessId)
            val cashAccount = accountDao.getAccountByCode("1010", businessId)
            val mpesaAccount = accountDao.getAccountByCode("1020", businessId)
            val bankAccount = accountDao.getAccountByCode("1030", businessId)
            val arAccount = accountDao.getAccountByCode("1200", businessId)

            val debitAccount = when (paymentMethod) {
                PaymentMethod.CREDIT -> arAccount
                PaymentMethod.MPESA -> mpesaAccount ?: cashAccount
                PaymentMethod.BANK -> bankAccount ?: cashAccount
                else -> cashAccount
            }

            if (debitAccount != null && revAccount != null) {
                postJournalEntry(
                    date = sale.createdAt,
                    description = "POS Sale - ${items.size} item(s) (${paymentMethod.name})",
                    sourceType = JournalSourceType.SALE,
                    sourceReferenceId = sale.id,
                    lines = listOf(
                        JournalLineItem(debitAccount.id, debitAccount.code, debitAccount.name, debit = total, credit = 0.0),
                        JournalLineItem(revAccount.id, revAccount.code, revAccount.name, debit = 0.0, credit = total)
                    )
                )
            }

            Result.success(sale)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSale(saleId: String): Result<Unit> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val role = getActiveRole(businessId, session.userId)

            if (role != Role.OWNER) {
                return Result.failure(SecurityException("Only business owners can delete sales"))
            }

            val sale = saleDao.getSaleById(saleId, businessId)
                ?: return Result.failure(Exception("Sale not found"))

            if (sale.paymentMethod == PaymentMethod.CREDIT && !sale.customerId.isNullOrEmpty()) {
                customerDao.updateBalance(sale.customerId, businessId, -sale.total)
            }

            for (item in sale.items) {
                itemDao.decrementStock(item.itemId, businessId, -item.qty)
            }

            saleDao.deleteSale(saleId, businessId)

            logAudit(
                businessId = businessId,
                actionType = ActionType.SALE_DELETED,
                beforeValue = "Sale $saleId for KSh ${sale.total} (${sale.paymentMethod})",
                afterValue = "Deleted by Owner ${session.name}"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeSales(): Flow<List<SaleEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return saleDao.observeSalesForBusiness(businessId)
    }

    // ==========================================
    // DENI (CREDIT) LEDGER
    // ==========================================

    fun observeDeniLedger(): Flow<List<CustomerEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return customerDao.observeDeniLedger(businessId)
    }

    fun observeCustomers(): Flow<List<CustomerEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return customerDao.observeCustomersForBusiness(businessId)
    }

    suspend fun createCustomer(
        name: String,
        phone: String,
        initialBalance: Double = 0.0,
        dueDate: Long? = null,
        notes: String = ""
    ): Result<CustomerEntity> {
        return try {
            val businessId = getActiveBusinessId()
            val customer = CustomerEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                name = name.trim(),
                phone = phone.trim(),
                balance = initialBalance,
                dueDate = dueDate,
                notes = notes,
                syncStatus = SyncStatus.PENDING
            )
            customerDao.insertCustomer(customer)
            Result.success(customer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordDeniPayment(
        customerId: String,
        amountPaid: Double,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        mpesaCode: String? = null
    ): Result<Unit> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val customer = customerDao.getCustomerById(customerId, businessId)
            customerDao.updateBalance(customerId, businessId, -amountPaid)

            // Double entry: Dr Cash/Mpesa, Cr Accounts Receivable (1040)
            val debitAccount = when (paymentMethod) {
                PaymentMethod.MPESA -> accountDao.getAccountByCode("1020", businessId)
                PaymentMethod.BANK -> accountDao.getAccountByCode("1030", businessId)
                else -> accountDao.getAccountByCode("1010", businessId)
            }
            val arAccount = accountDao.getAccountByCode("1040", businessId)
            if (debitAccount != null && arAccount != null) {
                postJournalEntry(
                    date = System.currentTimeMillis(),
                    description = "Malipo ya Deni: ${customer?.name ?: "Customer"} (KSh ${amountPaid.toInt()}${if (!mpesaCode.isNullOrBlank()) " - $mpesaCode" else ""})",
                    sourceType = JournalSourceType.CUSTOMER_PAYMENT,
                    sourceReferenceId = customerId,
                    lines = listOf(
                        JournalLineItem(debitAccount.id, debitAccount.code, debitAccount.name, debit = amountPaid, credit = 0.0),
                        JournalLineItem(arAccount.id, arAccount.code, arAccount.name, debit = 0.0, credit = amountPaid)
                    )
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // STAFF MANAGEMENT
    // ==========================================

    fun observeStaffMembers(): Flow<List<MemberWithUser>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return businessMemberDao.observeMembersWithUser(businessId)
    }

    suspend fun inviteOrAddStaff(
        name: String,
        email: String,
        phone: String,
        role: Role
    ): Result<Unit> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val myRole = getActiveRole(businessId, session.userId)

            if (myRole != Role.OWNER) {
                return Result.failure(SecurityException("Only business owners can invite staff"))
            }

            val business = businessDao.getBusinessById(businessId)
                ?: return Result.failure(Exception("Business not found"))

            val currentMemberCount = businessMemberDao.getMemberCount(businessId)
            if (currentMemberCount >= business.subscriptionPlan.maxUsers) {
                return Result.failure(
                    IllegalStateException(
                        "Plan limit reached! ${business.subscriptionPlan.name} allows up to ${business.subscriptionPlan.maxUsers} users. Upgrade your plan."
                    )
                )
            }

            var user = userDao.getUserByAuthId(email)
            if (user == null) {
                val newUserId = UUID.randomUUID().toString()
                user = UserEntity(
                    id = newUserId,
                    name = name,
                    email = email,
                    phone = phone,
                    authId = email
                )
                userDao.insertUser(user)
            }

            val member = BusinessMemberEntity(
                businessId = businessId,
                userId = user.id,
                role = role
            )
            businessMemberDao.insertMember(member)

            logAudit(
                businessId = businessId,
                actionType = ActionType.ROLE_CHANGED,
                beforeValue = "No membership",
                afterValue = "Added ${user.name} ($email) as ${role.name}"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStaffRole(userId: String, newRole: Role): Result<Unit> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val myRole = getActiveRole(businessId, session.userId)

            if (myRole != Role.OWNER) {
                return Result.failure(SecurityException("Only business owners can change staff roles"))
            }

            val member = businessMemberDao.getMember(businessId, userId)
                ?: return Result.failure(Exception("Member not found"))

            val oldRole = member.role
            businessMemberDao.updateMember(member.copy(role = newRole))

            logAudit(
                businessId = businessId,
                actionType = ActionType.ROLE_CHANGED,
                beforeValue = "User $userId was ${oldRole.name}",
                afterValue = "Changed to ${newRole.name} by Owner ${session.name}"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeStaff(userId: String): Result<Unit> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val myRole = getActiveRole(businessId, session.userId)

            if (myRole != Role.OWNER) {
                return Result.failure(SecurityException("Only business owners can remove staff"))
            }

            if (userId == session.userId) {
                return Result.failure(IllegalArgumentException("Cannot remove yourself as owner"))
            }

            businessMemberDao.deleteMember(businessId, userId)
            logAudit(
                businessId = businessId,
                actionType = ActionType.ROLE_CHANGED,
                beforeValue = "Active member $userId",
                afterValue = "Removed by Owner ${session.name}"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // AUDIT LOG & SUBSCRIPTIONS
    // ==========================================

    fun observeAuditLogs(): Flow<List<AuditLogEntity>> {
        val session = getSession()
        val businessId = session.activeBusinessId ?: return flowOf(emptyList())
        if (session.activeRole != Role.OWNER) {
            return flowOf(emptyList())
        }
        return auditLogDao.observeAuditLogsForBusiness(businessId)
    }

    fun observeCurrentBusiness(): Flow<BusinessEntity?> {
        val businessId = getSession().activeBusinessId ?: return flowOf(null)
        return businessDao.observeBusinessById(businessId)
    }

    suspend fun updateSubscriptionPlan(newPlan: SubscriptionPlan): Result<Unit> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val myRole = getActiveRole(businessId, session.userId)

            if (myRole != Role.OWNER) {
                return Result.failure(SecurityException("Only business owners can manage billing/subscriptions"))
            }

            val business = businessDao.getBusinessById(businessId)
                ?: return Result.failure(Exception("Business not found"))

            val oldPlan = business.subscriptionPlan
            businessDao.updateBusiness(business.copy(subscriptionPlan = newPlan))

            logAudit(
                businessId = businessId,
                actionType = ActionType.ROLE_CHANGED,
                beforeValue = "Plan: ${oldPlan.name}",
                afterValue = "Upgraded to ${newPlan.name}"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBusinessById(businessId: String): BusinessEntity? {
        return businessDao.getBusinessById(businessId)
    }

    suspend fun getMemberCount(businessId: String): Int {
        return businessMemberDao.getMemberCount(businessId)
    }

    suspend fun seedDemoData(type: com.example.businessos.data.model.BusinessType): Result<Unit> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()

            if (type == com.example.businessos.data.model.BusinessType.GARAGE) {
                // Seed realistic sample vehicles covering bodywork, paint, torn leather, tyres & engine
                val plates = listOf("KDA 124X", "KCF 889Y", "KBL 552P")
                val owners = listOf("Mwangi Maina", "Faith Achieng", "Brian Omondi")
                val descriptions = listOf(
                    "Front bumper dent removal, panel beating & metallic respray (Rangi)",
                    "Torn driver leather seat stitching & sagging roof lining repair (Ndani & Viti)",
                    "Tyre burst, rim bend straightening & radiator overheating flush (Matairi & Injini)"
                )
                val stages = listOf(JobStage.DIAGNOSIS, JobStage.IN_PROGRESS, JobStage.AWAITING_APPROVAL)
                val sampleParts = listOf(
                    listOf(
                        PartItem(name = "Bumper Dent Removal & Plastic Welding", cost = 3500.0),
                        PartItem(name = "Full Bumper Respray & Buffing", cost = 4500.0)
                    ),
                    listOf(
                        PartItem(name = "Torn Leather Seat Stitching", cost = 3000.0),
                        PartItem(name = "Roof Lining Sagging Repair (Dari)", cost = 4500.0)
                    ),
                    listOf(
                        PartItem(name = "Rim Bend Straightening", cost = 2500.0),
                        PartItem(name = "Wheel Alignment & Balancing", cost = 2000.0),
                        PartItem(name = "Radiator Flush & Leak Sealant", cost = 3500.0)
                    )
                )

                plates.forEachIndexed { i, plate ->
                    val parts = sampleParts[i]
                    jobCardDao.insertJobCard(
                        JobCardEntity(
                            id = UUID.randomUUID().toString(),
                            businessId = businessId,
                            vehiclePlate = plate,
                            customerName = owners[i],
                            customerPhone = "+25471200000$i",
                            description = descriptions[i],
                            stage = stages[i],
                            estimateTotal = parts.sumOf { it.cost },
                            partsList = parts
                        )
                    )
                }
            } else {
                // Seed sample Kariakor items with categories and customers
                val sampleItems = listOf(
                    Triple("Safari Combat Boots (Size 42)", 3500.0, 2700.0),
                    Triple("Handcrafted Leather Belt (Brown)", 800.0, 500.0),
                    Triple("Men Leather Oxford Shoes (Black)", 2800.0, 2100.0),
                    Triple("Engine Oil 20W-50 (4 Litre)", 3200.0, 2600.0),
                    Triple("Front Disc Brake Pads Heavy Duty", 3500.0, 2400.0),
                    Triple("Women Leather Handbag Black", 2200.0, 1600.0)
                )
                sampleItems.forEach { (name, ret, whl) ->
                    itemDao.insertItem(
                        ItemEntity(
                            id = UUID.randomUUID().toString(),
                            businessId = businessId,
                            name = name,
                            retailPrice = ret,
                            wholesalePrice = whl,
                            stockQty = 25
                        )
                    )
                }

                // Seed sample Deni customer
                customerDao.insertCustomer(
                    CustomerEntity(
                        id = UUID.randomUUID().toString(),
                        businessId = businessId,
                        name = "Hassan Kariuki",
                        phone = "+254722334455",
                        balance = 3200.0,
                        dueDate = System.currentTimeMillis() + (5L * 24 * 60 * 60 * 1000),
                        notes = "Aliahidi kulipa Ijumaa baada ya kupokea mshahara"
                    )
                )
            }

            // Seed enterprise accounting records
            ensureDefaultAccounts(businessId)

            // Seed sample supplier
            val supplierId = UUID.randomUUID().toString()
            val supplierName = if (type == com.example.businessos.data.model.BusinessType.GARAGE) "AutoSpares Kenya Ltd" else "Kariakor Leather Tannery"
            supplierDao.insertSupplier(
                SupplierEntity(
                    id = supplierId,
                    businessId = businessId,
                    name = supplierName,
                    phone = "+254701234567",
                    email = "sales@supplier.co.ke",
                    totalPayable = 18500.0
                )
            )

            // Seed sample supplier bill
            billDao.insertBill(
                BillEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = businessId,
                    billNumber = "BILL-0012",
                    supplierId = supplierId,
                    supplierName = supplierName,
                    issueDate = System.currentTimeMillis() - (4L * 24 * 60 * 60 * 1000),
                    dueDate = System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000),
                    totalAmount = 18500.0,
                    paidAmount = 0.0,
                    balanceDue = 18500.0,
                    status = BillStatus.UNPAID,
                    category = "Mzigo / Stock",
                    description = "Monthly stock inventory delivery",
                    createdByUserId = session.userId
                )
            )

            // Seed sample fixed asset
            val assetName = if (type == com.example.businessos.data.model.BusinessType.GARAGE) "Hydraulic 2-Post Car Lift" else "Heavy Duty Shoe Stitching Machine"
            assetDao.insertAsset(
                AssetEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = businessId,
                    assetNumber = "AST-001",
                    name = assetName,
                    category = "Machinery & Equipment",
                    acquisitionCost = 145000.0,
                    purchaseDate = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000),
                    usefulLifeYears = 5,
                    salvageValue = 15000.0,
                    accumulatedDepreciation = 13000.0,
                    currentValue = 132000.0,
                    notes = "Installed and operational in main bay"
                )
            )

            // Seed sample liability / loan
            liabilityDao.insertLiability(
                LiabilityEntity(
                    id = UUID.randomUUID().toString(),
                    businessId = businessId,
                    lenderName = "Equity Bank Biashara Loan",
                    principalAmount = 100000.0,
                    interestRatePercent = 12.0,
                    totalRepayable = 112000.0,
                    totalPaid = 35000.0,
                    balanceDue = 77000.0,
                    startDate = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000),
                    dueDate = System.currentTimeMillis() + (300L * 24 * 60 * 60 * 1000),
                    status = "ACTIVE",
                    notes = "Monthly repayment via standing order"
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // ACCOUNTING: EXPENSES & CASH-OUT
    // ==========================================

    fun observeExpenses(): Flow<List<ExpenseEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return expenseDao.observeExpenses(businessId)
    }

    fun observeTotalExpenses(): Flow<Double> {
        val businessId = getSession().activeBusinessId ?: return flowOf(0.0)
        return expenseDao.observeTotalExpenses(businessId)
    }

    fun observeTotalSales(): Flow<Double?> {
        val businessId = getSession().activeBusinessId ?: return flowOf(0.0)
        return saleDao.observeTotalSales(businessId)
    }

    suspend fun recordExpense(
        category: String,
        title: String,
        amount: Double,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        note: String = ""
    ): Result<ExpenseEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val expense = ExpenseEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                category = category,
                title = title,
                amount = amount,
                paymentMethod = paymentMethod,
                note = note,
                createdAt = System.currentTimeMillis(),
                createdByUserId = session.userId
            )
            expenseDao.insertExpense(expense)

            // Post double-entry journal entry to General Ledger
            ensureDefaultAccounts(businessId)
            val expenseCode = when {
                category.contains("Rent", ignoreCase = true) || category.contains("Kodi", ignoreCase = true) -> "6010"
                category.contains("Tokeni", ignoreCase = true) || category.contains("Umeme", ignoreCase = true) -> "6020"
                category.contains("Mishahara", ignoreCase = true) || category.contains("Wages", ignoreCase = true) -> "6030"
                category.contains("Nauli", ignoreCase = true) || category.contains("Transport", ignoreCase = true) -> "6040"
                category.contains("Chakula", ignoreCase = true) || category.contains("Lunch", ignoreCase = true) -> "6050"
                category.contains("Mzigo", ignoreCase = true) || category.contains("Stock", ignoreCase = true) -> "5010"
                else -> "6070"
            }
            val expAccount = accountDao.getAccountByCode(expenseCode, businessId)
                ?: accountDao.getAccountByCode("6070", businessId)
            val cashAccount = accountDao.getAccountByCode("1010", businessId)
            val mpesaAccount = accountDao.getAccountByCode("1020", businessId)
            val bankAccount = accountDao.getAccountByCode("1030", businessId)

            val creditAccount = when (paymentMethod) {
                PaymentMethod.MPESA -> mpesaAccount ?: cashAccount
                PaymentMethod.BANK -> bankAccount ?: cashAccount
                else -> cashAccount
            }

            if (expAccount != null && creditAccount != null) {
                postJournalEntry(
                    date = expense.createdAt,
                    description = "Expense: $title ($category)",
                    sourceType = JournalSourceType.EXPENSE,
                    sourceReferenceId = expense.id,
                    lines = listOf(
                        JournalLineItem(expAccount.id, expAccount.code, expAccount.name, debit = amount, credit = 0.0),
                        JournalLineItem(creditAccount.id, creditAccount.code, creditAccount.name, debit = 0.0, credit = amount)
                    )
                )
            }

            Result.success(expense)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(id: String): Result<Unit> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val role = getActiveRole(businessId, session.userId)
            if (role != Role.OWNER) {
                return Result.failure(SecurityException("Only business owners can delete expenses"))
            }
            expenseDao.deleteExpense(id, businessId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCustomerReminder(customerId: String, dueDate: Long?, notes: String): Result<Unit> {
        return try {
            val businessId = getActiveBusinessId()
            val customer = customerDao.getCustomerById(customerId, businessId)
                ?: return Result.failure(Exception("Customer not found"))
            customerDao.updateCustomer(
                customer.copy(
                    dueDate = dueDate,
                    notes = notes,
                    lastReminderSent = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // ENTERPRISE ACCOUNTING: CHART OF ACCOUNTS & GENERAL LEDGER
    // ==========================================

    suspend fun ensureDefaultAccounts(businessId: String) {
        val existing = accountDao.getAccounts(businessId)
        if (existing.isNotEmpty()) return

        val defaultAccounts = listOf(
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
        accountDao.insertAccounts(defaultAccounts)
    }

    fun observeAccounts(): Flow<List<AccountEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return accountDao.observeAccounts(businessId)
    }

    suspend fun createAccount(
        code: String,
        name: String,
        category: AccountCategory,
        subCategory: AccountSubCategory
    ): Result<AccountEntity> {
        return try {
            val businessId = getActiveBusinessId()
            val existing = accountDao.getAccountByCode(code.trim(), businessId)
            if (existing != null) {
                return Result.failure(IllegalArgumentException("Account code $code already exists"))
            }

            val account = AccountEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                code = code.trim(),
                name = name.trim(),
                category = category,
                subCategory = subCategory,
                isSystemAccount = false,
                balance = 0.0
            )
            accountDao.insertAccount(account)
            Result.success(account)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeJournalEntries(): Flow<List<JournalEntryEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return journalEntryDao.observeJournalEntries(businessId)
    }

    suspend fun postJournalEntry(
        date: Long,
        description: String,
        sourceType: JournalSourceType,
        sourceReferenceId: String?,
        lines: List<JournalLineItem>
    ): Result<JournalEntryEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()

            if (lines.isEmpty()) {
                return Result.failure(IllegalArgumentException("Journal entry must contain lines"))
            }

            val totalDebit = lines.sumOf { it.debit }
            val totalCredit = lines.sumOf { it.credit }

            if (Math.abs(totalDebit - totalCredit) > 0.001) {
                return Result.failure(IllegalArgumentException("Unbalanced journal entry! Total Debit: KSh $totalDebit != Total Credit: KSh $totalCredit"))
            }

            val count = journalEntryDao.countEntries(businessId)
            val entryNumber = "JE-${String.format("%04d", count + 1)}"

            val entry = JournalEntryEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                entryNumber = entryNumber,
                date = date,
                description = description,
                sourceType = sourceType,
                sourceReferenceId = sourceReferenceId,
                totalDebit = totalDebit,
                totalCredit = totalCredit,
                createdByUserId = session.userId,
                lines = lines
            )
            journalEntryDao.insertEntry(entry)

            // Update account balances based on fundamental double-entry accounting rules
            for (line in lines) {
                val account = accountDao.getAccountById(line.accountId, businessId)
                    ?: accountDao.getAccountByCode(line.accountCode, businessId)
                if (account != null) {
                    val delta = when (account.category) {
                        AccountCategory.ASSET, AccountCategory.EXPENSE -> line.debit - line.credit
                        AccountCategory.LIABILITY, AccountCategory.EQUITY, AccountCategory.INCOME -> line.credit - line.debit
                    }
                    accountDao.adjustBalance(account.id, businessId, delta)
                }
            }

            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // INVOICES & ACCOUNTS RECEIVABLE (SALES CREDIT)
    // ==========================================

    fun observeInvoices(): Flow<List<InvoiceEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return invoiceDao.observeInvoices(businessId)
    }

    fun observeTotalReceivables(): Flow<Double?> {
        val businessId = getSession().activeBusinessId ?: return flowOf(0.0)
        return invoiceDao.observeTotalReceivables(businessId)
    }

    suspend fun createInvoice(
        customerId: String,
        customerName: String,
        customerPhone: String,
        items: List<InvoiceLineItem>,
        discountAmount: Double = 0.0,
        taxAmount: Double = 0.0,
        dueDate: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000),
        notes: String = "",
        paymentMethod: PaymentMethod = PaymentMethod.CREDIT
    ): Result<InvoiceEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            ensureDefaultAccounts(businessId)

            val subtotal = items.sumOf { it.total }
            val total = subtotal - discountAmount + taxAmount
            val count = invoiceDao.countInvoices(businessId)
            val invoiceNumber = "INV-${String.format("%04d", count + 1)}"

            val isFullyPaidNow = (paymentMethod == PaymentMethod.CASH || paymentMethod == PaymentMethod.MPESA || paymentMethod == PaymentMethod.BANK)
            val initialPaid = if (isFullyPaidNow) total else 0.0
            val initialBalance = total - initialPaid
            val initialStatus = if (isFullyPaidNow) InvoiceStatus.PAID else InvoiceStatus.UNPAID

            val invoice = InvoiceEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                invoiceNumber = invoiceNumber,
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                issueDate = System.currentTimeMillis(),
                dueDate = dueDate,
                subtotal = subtotal,
                discountAmount = discountAmount,
                taxAmount = taxAmount,
                totalAmount = total,
                paidAmount = initialPaid,
                balanceDue = initialBalance,
                status = initialStatus,
                paymentMethod = paymentMethod,
                items = items,
                notes = notes,
                createdByUserId = session.userId
            )
            invoiceDao.insertInvoice(invoice)

            // Update customer debt if credit
            if (!isFullyPaidNow) {
                customerDao.updateBalance(customerId, businessId, total)
            }

            // Post General Ledger Double-Entry
            val arAccount = accountDao.getAccountByCode("1200", businessId)
            val revenueAccount = accountDao.getAccountByCode("4010", businessId)
            val cashAccount = accountDao.getAccountByCode("1010", businessId)
            val mpesaAccount = accountDao.getAccountByCode("1020", businessId)
            val bankAccount = accountDao.getAccountByCode("1030", businessId)

            val debitAccount = when {
                !isFullyPaidNow -> arAccount
                paymentMethod == PaymentMethod.MPESA -> mpesaAccount
                paymentMethod == PaymentMethod.BANK -> bankAccount
                else -> cashAccount
            }

            if (debitAccount != null && revenueAccount != null) {
                postJournalEntry(
                    date = invoice.issueDate,
                    description = "Invoice $invoiceNumber for $customerName",
                    sourceType = JournalSourceType.SALE,
                    sourceReferenceId = invoice.id,
                    lines = listOf(
                        JournalLineItem(debitAccount.id, debitAccount.code, debitAccount.name, debit = total, credit = 0.0),
                        JournalLineItem(revenueAccount.id, revenueAccount.code, revenueAccount.name, debit = 0.0, credit = total)
                    )
                )
            }

            Result.success(invoice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordInvoicePayment(
        invoiceId: String,
        amountPaid: Double,
        paymentMethod: PaymentMethod,
        reference: String = "",
        description: String = ""
    ): Result<PaymentEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val invoice = invoiceDao.getInvoiceById(invoiceId, businessId)
                ?: return Result.failure(Exception("Invoice not found"))

            if (amountPaid <= 0 || amountPaid > invoice.balanceDue + 0.01) {
                return Result.failure(IllegalArgumentException("Invalid payment amount. Balance due is KSh ${invoice.balanceDue}"))
            }

            val newPaid = invoice.paidAmount + amountPaid
            val newBalance = invoice.totalAmount - newPaid
            val newStatus = if (newBalance <= 0.01) InvoiceStatus.PAID else InvoiceStatus.PARTIALLY_PAID

            invoiceDao.recordInvoicePayment(invoiceId, businessId, amountPaid, newStatus)
            customerDao.updateBalance(invoice.customerId, businessId, -amountPaid)

            // Post double entry: Dr Cash/M-Pesa/Bank, Cr Accounts Receivable (1200)
            val arAccount = accountDao.getAccountByCode("1200", businessId)
            val cashAccount = accountDao.getAccountByCode("1010", businessId)
            val mpesaAccount = accountDao.getAccountByCode("1020", businessId)
            val bankAccount = accountDao.getAccountByCode("1030", businessId)

            val debitAccount = when (paymentMethod) {
                PaymentMethod.MPESA -> mpesaAccount ?: cashAccount
                PaymentMethod.BANK -> bankAccount ?: cashAccount
                else -> cashAccount
            }

            val payCount = paymentDao.countPayments(businessId)
            val paymentNumber = "REC-${String.format("%04d", payCount + 1)}"

            val payment = PaymentEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                paymentNumber = paymentNumber,
                type = PaymentType.CUSTOMER_RECEIPT,
                fromAccountId = arAccount?.id ?: "1200",
                toAccountId = debitAccount?.id ?: "1010",
                amount = amountPaid,
                paymentMethod = paymentMethod,
                reference = reference,
                targetInvoiceId = invoiceId,
                description = description.ifBlank { "Receipt for Invoice ${invoice.invoiceNumber}" },
                date = System.currentTimeMillis(),
                createdByUserId = session.userId
            )
            paymentDao.insertPayment(payment)

            if (debitAccount != null && arAccount != null) {
                postJournalEntry(
                    date = payment.date,
                    description = "Customer Payment: ${invoice.customerName} on Invoice ${invoice.invoiceNumber}",
                    sourceType = JournalSourceType.CUSTOMER_PAYMENT,
                    sourceReferenceId = payment.id,
                    lines = listOf(
                        JournalLineItem(debitAccount.id, debitAccount.code, debitAccount.name, debit = amountPaid, credit = 0.0),
                        JournalLineItem(arAccount.id, arAccount.code, arAccount.name, debit = 0.0, credit = amountPaid)
                    )
                )
            }

            Result.success(payment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // SUPPLIERS & ACCOUNTS PAYABLE (BILLS)
    // ==========================================

    fun observeSuppliers(): Flow<List<SupplierEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return supplierDao.observeSuppliers(businessId)
    }

    suspend fun createSupplier(name: String, phone: String, email: String = ""): Result<SupplierEntity> {
        return try {
            val businessId = getActiveBusinessId()
            val supplier = SupplierEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                name = name.trim(),
                phone = phone.trim(),
                email = email.trim()
            )
            supplierDao.insertSupplier(supplier)
            Result.success(supplier)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeBills(): Flow<List<BillEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return billDao.observeBills(businessId)
    }

    fun observeTotalPayables(): Flow<Double?> {
        val businessId = getSession().activeBusinessId ?: return flowOf(0.0)
        return billDao.observeTotalPayables(businessId)
    }

    suspend fun createBill(
        supplierId: String,
        supplierName: String,
        billNumber: String,
        totalAmount: Double,
        dueDate: Long = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000),
        category: String = "Mzigo / Stock",
        description: String = ""
    ): Result<BillEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            ensureDefaultAccounts(businessId)

            val count = billDao.countBills(businessId)
            val generatedBillNum = if (billNumber.isNotBlank()) billNumber else "BILL-${String.format("%04d", count + 1)}"

            val bill = BillEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                billNumber = generatedBillNum,
                supplierId = supplierId,
                supplierName = supplierName,
                issueDate = System.currentTimeMillis(),
                dueDate = dueDate,
                totalAmount = totalAmount,
                paidAmount = 0.0,
                balanceDue = totalAmount,
                status = BillStatus.UNPAID,
                category = category,
                description = description,
                createdByUserId = session.userId
            )
            billDao.insertBill(bill)
            supplierDao.adjustPayable(supplierId, businessId, totalAmount)

            // Post double-entry: Dr Inventory/Expense (1300 / 5010), Cr Accounts Payable (2010)
            val apAccount = accountDao.getAccountByCode("2010", businessId)
            val costAccount = accountDao.getAccountByCode("1300", businessId) ?: accountDao.getAccountByCode("5010", businessId)

            if (costAccount != null && apAccount != null) {
                postJournalEntry(
                    date = bill.issueDate,
                    description = "Supplier Bill $generatedBillNum from $supplierName",
                    sourceType = JournalSourceType.PURCHASE_BILL,
                    sourceReferenceId = bill.id,
                    lines = listOf(
                        JournalLineItem(costAccount.id, costAccount.code, costAccount.name, debit = totalAmount, credit = 0.0),
                        JournalLineItem(apAccount.id, apAccount.code, apAccount.name, debit = 0.0, credit = totalAmount)
                    )
                )
            }

            Result.success(bill)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordBillPayment(
        billId: String,
        amountPaid: Double,
        paymentMethod: PaymentMethod,
        reference: String = "",
        description: String = ""
    ): Result<PaymentEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val bill = billDao.getBillById(billId, businessId)
                ?: return Result.failure(Exception("Bill not found"))

            if (amountPaid <= 0 || amountPaid > bill.balanceDue + 0.01) {
                return Result.failure(IllegalArgumentException("Invalid payment amount. Balance due is KSh ${bill.balanceDue}"))
            }

            val newPaid = bill.paidAmount + amountPaid
            val newBalance = bill.totalAmount - newPaid
            val newStatus = if (newBalance <= 0.01) BillStatus.PAID else BillStatus.PARTIALLY_PAID

            billDao.recordBillPayment(billId, businessId, amountPaid, newStatus)
            supplierDao.adjustPayable(bill.supplierId, businessId, -amountPaid)

            // Post double-entry: Dr Accounts Payable (2010), Cr Cash/M-Pesa/Bank
            val apAccount = accountDao.getAccountByCode("2010", businessId)
            val cashAccount = accountDao.getAccountByCode("1010", businessId)
            val mpesaAccount = accountDao.getAccountByCode("1020", businessId)
            val bankAccount = accountDao.getAccountByCode("1030", businessId)

            val creditAccount = when (paymentMethod) {
                PaymentMethod.MPESA -> mpesaAccount ?: cashAccount
                PaymentMethod.BANK -> bankAccount ?: cashAccount
                else -> cashAccount
            }

            val payCount = paymentDao.countPayments(businessId)
            val paymentNumber = "PAY-${String.format("%04d", payCount + 1)}"

            val payment = PaymentEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                paymentNumber = paymentNumber,
                type = PaymentType.SUPPLIER_PAYMENT,
                fromAccountId = creditAccount?.id ?: "1010",
                toAccountId = apAccount?.id ?: "2010",
                amount = amountPaid,
                paymentMethod = paymentMethod,
                reference = reference,
                targetBillId = billId,
                description = description.ifBlank { "Payment for Bill ${bill.billNumber} to ${bill.supplierName}" },
                date = System.currentTimeMillis(),
                createdByUserId = session.userId
            )
            paymentDao.insertPayment(payment)

            if (apAccount != null && creditAccount != null) {
                postJournalEntry(
                    date = payment.date,
                    description = "Payment to Supplier ${bill.supplierName} on Bill ${bill.billNumber}",
                    sourceType = JournalSourceType.SUPPLIER_PAYMENT,
                    sourceReferenceId = payment.id,
                    lines = listOf(
                        JournalLineItem(apAccount.id, apAccount.code, apAccount.name, debit = amountPaid, credit = 0.0),
                        JournalLineItem(creditAccount.id, creditAccount.code, creditAccount.name, debit = 0.0, credit = amountPaid)
                    )
                )
            }

            Result.success(payment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // CASH & BANK TRANSFERS
    // ==========================================

    fun observePayments(): Flow<List<PaymentEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return paymentDao.observePayments(businessId)
    }

    suspend fun recordAccountTransfer(
        fromAccountCode: String,
        toAccountCode: String,
        amount: Double,
        reference: String = "",
        description: String = ""
    ): Result<PaymentEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            ensureDefaultAccounts(businessId)

            val fromAccount = accountDao.getAccountByCode(fromAccountCode, businessId)
                ?: return Result.failure(Exception("Source account not found"))
            val toAccount = accountDao.getAccountByCode(toAccountCode, businessId)
                ?: return Result.failure(Exception("Destination account not found"))

            if (amount <= 0) {
                return Result.failure(IllegalArgumentException("Transfer amount must be greater than zero"))
            }

            val payCount = paymentDao.countPayments(businessId)
            val paymentNumber = "TRF-${String.format("%04d", payCount + 1)}"

            val payment = PaymentEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                paymentNumber = paymentNumber,
                type = PaymentType.ACCOUNT_TRANSFER,
                fromAccountId = fromAccount.id,
                toAccountId = toAccount.id,
                amount = amount,
                paymentMethod = PaymentMethod.BANK,
                reference = reference,
                description = description.ifBlank { "Transfer from ${fromAccount.name} to ${toAccount.name}" },
                date = System.currentTimeMillis(),
                createdByUserId = session.userId
            )
            paymentDao.insertPayment(payment)

            // Post double-entry: Dr Destination, Cr Source
            postJournalEntry(
                date = payment.date,
                description = "Transfer from ${fromAccount.name} to ${toAccount.name} ($reference)",
                sourceType = JournalSourceType.TRANSFER,
                sourceReferenceId = payment.id,
                lines = listOf(
                    JournalLineItem(toAccount.id, toAccount.code, toAccount.name, debit = amount, credit = 0.0),
                    JournalLineItem(fromAccount.id, fromAccount.code, fromAccount.name, debit = 0.0, credit = amount)
                )
            )

            Result.success(payment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // ASSET REGISTER & DEPRECIATION
    // ==========================================

    fun observeAssets(): Flow<List<AssetEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return assetDao.observeAssets(businessId)
    }

    fun observeTotalAssetValue(): Flow<Double?> {
        val businessId = getSession().activeBusinessId ?: return flowOf(0.0)
        return assetDao.observeTotalAssetValue(businessId)
    }

    suspend fun recordAssetPurchase(
        name: String,
        category: String,
        cost: Double,
        usefulLifeYears: Int = 5,
        salvageValue: Double = 0.0,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        notes: String = ""
    ): Result<AssetEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            ensureDefaultAccounts(businessId)

            val count = assetDao.countAssets(businessId)
            val assetNumber = "AST-${String.format("%03d", count + 1)}"

            val asset = AssetEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                assetNumber = assetNumber,
                name = name.trim(),
                category = category.trim(),
                acquisitionCost = cost,
                purchaseDate = System.currentTimeMillis(),
                usefulLifeYears = usefulLifeYears,
                salvageValue = salvageValue,
                accumulatedDepreciation = 0.0,
                currentValue = cost,
                notes = notes
            )
            assetDao.insertAsset(asset)

            // Post double entry: Dr 1500 Equipment, Cr Cash/M-Pesa/Bank
            val equipmentAccount = accountDao.getAccountByCode("1500", businessId)
            val cashAccount = accountDao.getAccountByCode("1010", businessId)
            val mpesaAccount = accountDao.getAccountByCode("1020", businessId)
            val bankAccount = accountDao.getAccountByCode("1030", businessId)

            val creditAccount = when (paymentMethod) {
                PaymentMethod.MPESA -> mpesaAccount ?: cashAccount
                PaymentMethod.BANK -> bankAccount ?: cashAccount
                else -> cashAccount
            }

            if (equipmentAccount != null && creditAccount != null) {
                postJournalEntry(
                    date = asset.purchaseDate,
                    description = "Asset Purchase: $name ($assetNumber)",
                    sourceType = JournalSourceType.ASSET_PURCHASE,
                    sourceReferenceId = asset.id,
                    lines = listOf(
                        JournalLineItem(equipmentAccount.id, equipmentAccount.code, equipmentAccount.name, debit = cost, credit = 0.0),
                        JournalLineItem(creditAccount.id, creditAccount.code, creditAccount.name, debit = 0.0, credit = cost)
                    )
                )
            }

            Result.success(asset)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun postAssetDepreciation(assetId: String): Result<Unit> {
        return try {
            val businessId = getActiveBusinessId()
            ensureDefaultAccounts(businessId)
            val asset = assetDao.getAssetById(assetId, businessId)
                ?: return Result.failure(Exception("Asset not found"))

            // Annual straight-line depreciation
            val annualDepreciation = (asset.acquisitionCost - asset.salvageValue) / asset.usefulLifeYears.coerceAtLeast(1)
            val remaining = asset.currentValue - asset.salvageValue
            val deprAmount = Math.min(annualDepreciation, Math.max(0.0, remaining))

            if (deprAmount <= 0) {
                return Result.failure(IllegalStateException("Asset is already fully depreciated"))
            }

            val newAccum = asset.accumulatedDepreciation + deprAmount
            val newCurrent = asset.acquisitionCost - newAccum

            assetDao.updateAsset(
                asset.copy(
                    accumulatedDepreciation = newAccum,
                    currentValue = newCurrent
                )
            )

            // Post double-entry: Dr 6060 Depreciation Expense, Cr 1510 Accumulated Depreciation
            val deprExpenseAccount = accountDao.getAccountByCode("6060", businessId)
            val accumDeprAccount = accountDao.getAccountByCode("1510", businessId)

            if (deprExpenseAccount != null && accumDeprAccount != null) {
                postJournalEntry(
                    date = System.currentTimeMillis(),
                    description = "Annual Depreciation for ${asset.name} (${asset.assetNumber})",
                    sourceType = JournalSourceType.DEPRECIATION,
                    sourceReferenceId = asset.id,
                    lines = listOf(
                        JournalLineItem(deprExpenseAccount.id, deprExpenseAccount.code, deprExpenseAccount.name, debit = deprAmount, credit = 0.0),
                        JournalLineItem(accumDeprAccount.id, accumDeprAccount.code, accumDeprAccount.name, debit = 0.0, credit = deprAmount)
                    )
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // LIABILITIES & LOANS
    // ==========================================

    fun observeLiabilities(): Flow<List<LiabilityEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return liabilityDao.observeLiabilities(businessId)
    }

    fun observeTotalLiabilities(): Flow<Double?> {
        val businessId = getSession().activeBusinessId ?: return flowOf(0.0)
        return liabilityDao.observeTotalLiabilities(businessId)
    }

    suspend fun recordLoanDisbursement(
        lenderName: String,
        principalAmount: Double,
        interestRatePercent: Double = 0.0,
        dueDate: Long = System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000),
        depositAccountCode: String = "1030",
        notes: String = ""
    ): Result<LiabilityEntity> {
        return try {
            val businessId = getActiveBusinessId()
            ensureDefaultAccounts(businessId)

            val totalRepayable = principalAmount + (principalAmount * (interestRatePercent / 100.0))

            val liability = LiabilityEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                lenderName = lenderName.trim(),
                principalAmount = principalAmount,
                interestRatePercent = interestRatePercent,
                totalRepayable = totalRepayable,
                totalPaid = 0.0,
                balanceDue = totalRepayable,
                startDate = System.currentTimeMillis(),
                dueDate = dueDate,
                notes = notes
            )
            liabilityDao.insertLiability(liability)

            // Post double-entry: Dr Cash/Bank, Cr 2020 Short-Term Loans
            val depositAccount = accountDao.getAccountByCode(depositAccountCode, businessId)
                ?: accountDao.getAccountByCode("1010", businessId)
            val loanAccount = accountDao.getAccountByCode("2020", businessId)

            if (depositAccount != null && loanAccount != null) {
                postJournalEntry(
                    date = liability.startDate,
                    description = "Loan Disbursement from $lenderName",
                    sourceType = JournalSourceType.LOAN_DISBURSEMENT,
                    sourceReferenceId = liability.id,
                    lines = listOf(
                        JournalLineItem(depositAccount.id, depositAccount.code, depositAccount.name, debit = principalAmount, credit = 0.0),
                        JournalLineItem(loanAccount.id, loanAccount.code, loanAccount.name, debit = 0.0, credit = principalAmount)
                    )
                )
            }

            Result.success(liability)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordLoanRepayment(
        liabilityId: String,
        amountPaid: Double,
        paymentMethod: PaymentMethod = PaymentMethod.BANK,
        notes: String = ""
    ): Result<PaymentEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val liability = liabilityDao.getLiabilityById(liabilityId, businessId)
                ?: return Result.failure(Exception("Loan record not found"))

            if (amountPaid <= 0 || amountPaid > liability.balanceDue + 0.01) {
                return Result.failure(IllegalArgumentException("Invalid repayment amount. Balance due is KSh ${liability.balanceDue}"))
            }

            val newPaid = liability.totalPaid + amountPaid
            val newBalance = liability.totalRepayable - newPaid
            val newStatus = if (newBalance <= 0.01) "PAID" else "ACTIVE"

            liabilityDao.recordRepayment(liabilityId, businessId, amountPaid, newStatus)

            // Post double-entry: Dr 2020 Short-Term Loans, Cr Bank/Cash/M-Pesa
            val loanAccount = accountDao.getAccountByCode("2020", businessId)
            val cashAccount = accountDao.getAccountByCode("1010", businessId)
            val mpesaAccount = accountDao.getAccountByCode("1020", businessId)
            val bankAccount = accountDao.getAccountByCode("1030", businessId)

            val creditAccount = when (paymentMethod) {
                PaymentMethod.MPESA -> mpesaAccount ?: cashAccount
                PaymentMethod.BANK -> bankAccount ?: cashAccount
                else -> cashAccount
            }

            val payCount = paymentDao.countPayments(businessId)
            val paymentNumber = "LOAN-${String.format("%04d", payCount + 1)}"

            val payment = PaymentEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                paymentNumber = paymentNumber,
                type = PaymentType.LOAN_REPAYMENT,
                fromAccountId = creditAccount?.id ?: "1030",
                toAccountId = loanAccount?.id ?: "2020",
                amount = amountPaid,
                paymentMethod = paymentMethod,
                reference = notes,
                description = "Loan Repayment to ${liability.lenderName}",
                date = System.currentTimeMillis(),
                createdByUserId = session.userId
            )
            paymentDao.insertPayment(payment)

            if (loanAccount != null && creditAccount != null) {
                postJournalEntry(
                    date = payment.date,
                    description = "Loan Repayment to ${liability.lenderName}",
                    sourceType = JournalSourceType.LOAN_REPAYMENT,
                    sourceReferenceId = payment.id,
                    lines = listOf(
                        JournalLineItem(loanAccount.id, loanAccount.code, loanAccount.name, debit = amountPaid, credit = 0.0),
                        JournalLineItem(creditAccount.id, creditAccount.code, creditAccount.name, debit = 0.0, credit = amountPaid)
                    )
                )
            }

            Result.success(payment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // FINANCIAL STATEMENTS & REPORTING ENGINE
    // ==========================================

    suspend fun generateTrialBalance(): Result<TrialBalanceReport> {
        return try {
            val businessId = getActiveBusinessId()
            ensureDefaultAccounts(businessId)
            val accounts = accountDao.getAccounts(businessId)

            val items = accounts.map { acc ->
                val (debit, credit) = when (acc.category) {
                    AccountCategory.ASSET, AccountCategory.EXPENSE -> {
                        if (acc.balance >= 0) Pair(acc.balance, 0.0) else Pair(0.0, -acc.balance)
                    }
                    AccountCategory.LIABILITY, AccountCategory.EQUITY, AccountCategory.INCOME -> {
                        if (acc.balance >= 0) Pair(0.0, acc.balance) else Pair(-acc.balance, 0.0)
                    }
                }
                TrialBalanceItem(
                    accountCode = acc.code,
                    accountName = acc.name,
                    category = acc.category,
                    debit = debit,
                    credit = credit
                )
            }

            val totalDebit = items.sumOf { it.debit }
            val totalCredit = items.sumOf { it.credit }

            Result.success(TrialBalanceReport(items, totalDebit, totalCredit))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateProfitAndLoss(startDate: Long = 0L, endDate: Long = System.currentTimeMillis()): Result<ProfitAndLossReport> {
        return try {
            val businessId = getActiveBusinessId()
            ensureDefaultAccounts(businessId)
            val accounts = accountDao.getAccounts(businessId)

            val revenueItems = accounts
                .filter { it.category == AccountCategory.INCOME }
                .map { AccountAmountItem(it.code, it.name, it.balance) }

            val totalRevenue = revenueItems.sumOf { it.amount }

            val cogsItems = accounts
                .filter { it.subCategory == AccountSubCategory.DIRECT_COST }
                .map { AccountAmountItem(it.code, it.name, it.balance) }

            val totalCogs = cogsItems.sumOf { it.amount }
            val grossProfit = totalRevenue - totalCogs

            val expenseItems = accounts
                .filter { it.category == AccountCategory.EXPENSE && it.subCategory != AccountSubCategory.DIRECT_COST }
                .map { AccountAmountItem(it.code, it.name, it.balance) }

            val totalExpenses = expenseItems.sumOf { it.amount }
            val netProfit = grossProfit - totalExpenses

            Result.success(
                ProfitAndLossReport(
                    revenueItems = revenueItems,
                    totalRevenue = totalRevenue,
                    costOfSalesItems = cogsItems,
                    totalCostOfSales = totalCogs,
                    grossProfit = grossProfit,
                    operatingExpenses = expenseItems,
                    totalOperatingExpenses = totalExpenses,
                    netProfit = netProfit
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateBalanceSheet(): Result<BalanceSheetReport> {
        return try {
            val businessId = getActiveBusinessId()
            ensureDefaultAccounts(businessId)
            val accounts = accountDao.getAccounts(businessId)

            val currentAssets = accounts
                .filter { it.category == AccountCategory.ASSET && it.subCategory == AccountSubCategory.CURRENT_ASSET }
                .map { AccountAmountItem(it.code, it.name, it.balance) }
            val totalCurrentAssets = currentAssets.sumOf { it.amount }

            val fixedAssets = accounts
                .filter { it.category == AccountCategory.ASSET && it.subCategory == AccountSubCategory.FIXED_ASSET }
                .map { AccountAmountItem(it.code, it.name, it.balance) }
            val totalFixedAssets = fixedAssets.sumOf { it.amount }

            val currentLiabilities = accounts
                .filter { it.category == AccountCategory.LIABILITY && it.subCategory == AccountSubCategory.CURRENT_LIABILITY }
                .map { AccountAmountItem(it.code, it.name, it.balance) }
            val totalCurrentLiabilities = currentLiabilities.sumOf { it.amount }

            val longTermLiabilities = accounts
                .filter { it.category == AccountCategory.LIABILITY && it.subCategory == AccountSubCategory.LONG_TERM_LIABILITY }
                .map { AccountAmountItem(it.code, it.name, it.balance) }
            val totalLongTermLiabilities = longTermLiabilities.sumOf { it.amount }

            // Current Net Profit flows into Equity
            val pnlResult = generateProfitAndLoss()
            val netProfit = pnlResult.getOrNull()?.netProfit ?: 0.0

            val equityItems = accounts
                .filter { it.category == AccountCategory.EQUITY }
                .map { AccountAmountItem(it.code, it.name, it.balance) }
            val totalEquity = equityItems.sumOf { it.amount } + netProfit

            Result.success(
                BalanceSheetReport(
                    currentAssets = currentAssets,
                    totalCurrentAssets = totalCurrentAssets,
                    fixedAssets = fixedAssets,
                    totalFixedAssets = totalFixedAssets,
                    currentLiabilities = currentLiabilities,
                    totalCurrentLiabilities = totalCurrentLiabilities,
                    longTermLiabilities = longTermLiabilities,
                    totalLongTermLiabilities = totalLongTermLiabilities,
                    equityItems = equityItems,
                    currentPeriodNetProfit = netProfit,
                    totalEquity = totalEquity
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateAccountsReceivableAgeing(): Result<AccountsReceivableAgeingReport> {
        return try {
            val businessId = getActiveBusinessId()
            val customers = customerDao.getCustomers(businessId)
            val now = System.currentTimeMillis()
            val dayMs = 24L * 60 * 60 * 1000

            var totalCurrent = 0.0
            var total1to30 = 0.0
            var total31to60 = 0.0
            var total61to90 = 0.0
            var total90Plus = 0.0

            val customerItems = customers.filter { it.balance > 0 }.map { cust ->
                val due = cust.dueDate ?: cust.updatedAt
                val overdueDays = ((now - due) / dayMs).toInt()

                var c = 0.0
                var d1 = 0.0
                var d31 = 0.0
                var d61 = 0.0
                var d90 = 0.0

                when {
                    overdueDays <= 0 -> { c = cust.balance; totalCurrent += cust.balance }
                    overdueDays in 1..30 -> { d1 = cust.balance; total1to30 += cust.balance }
                    overdueDays in 31..60 -> { d31 = cust.balance; total31to60 += cust.balance }
                    overdueDays in 61..90 -> { d61 = cust.balance; total61to90 += cust.balance }
                    else -> { d90 = cust.balance; total90Plus += cust.balance }
                }

                EntityAgeingItem(
                    entityId = cust.id,
                    entityName = cust.name,
                    phone = cust.phone,
                    bucket = AgeingBucket(c, d1, d31, d61, d90)
                )
            }

            Result.success(
                AccountsReceivableAgeingReport(
                    overall = AgeingBucket(totalCurrent, total1to30, total31to60, total61to90, total90Plus),
                    customers = customerItems
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateAccountsPayableAgeing(): Result<AccountsPayableAgeingReport> {
        return try {
            val businessId = getActiveBusinessId()
            val bills = billDao.getBills(businessId).filter { it.balanceDue > 0 && it.status != BillStatus.CANCELLED }
            val now = System.currentTimeMillis()
            val dayMs = 24L * 60 * 60 * 1000

            var totalCurrent = 0.0
            var total1to30 = 0.0
            var total31to60 = 0.0
            var total61to90 = 0.0
            var total90Plus = 0.0

            val supplierMap = mutableMapOf<String, Triple<String, String, AgeingBucket>>()

            for (bill in bills) {
                val overdueDays = ((now - bill.dueDate) / dayMs).toInt()
                val currentBucket = supplierMap[bill.supplierId]?.third ?: AgeingBucket()

                var c = currentBucket.current
                var d1 = currentBucket.days1to30
                var d31 = currentBucket.days31to60
                var d61 = currentBucket.days61to90
                var d90 = currentBucket.days90Plus

                when {
                    overdueDays <= 0 -> { c += bill.balanceDue; totalCurrent += bill.balanceDue }
                    overdueDays in 1..30 -> { d1 += bill.balanceDue; total1to30 += bill.balanceDue }
                    overdueDays in 31..60 -> { d31 += bill.balanceDue; total31to60 += bill.balanceDue }
                    overdueDays in 61..90 -> { d61 += bill.balanceDue; total61to90 += bill.balanceDue }
                    else -> { d90 += bill.balanceDue; total90Plus += bill.balanceDue }
                }

                supplierMap[bill.supplierId] = Triple(
                    bill.supplierName,
                    "",
                    AgeingBucket(c, d1, d31, d61, d90)
                )
            }

            val supplierItems = supplierMap.map { (id, triple) ->
                EntityAgeingItem(
                    entityId = id,
                    entityName = triple.first,
                    phone = triple.second,
                    bucket = triple.third
                )
            }

            Result.success(
                AccountsPayableAgeingReport(
                    overall = AgeingBucket(totalCurrent, total1to30, total31to60, total61to90, total90Plus),
                    suppliers = supplierItems
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // WAGES & SALARY MANAGEMENT
    // ==========================================

    fun observeWagePayments(): Flow<List<WagePaymentEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return wagePaymentDao.observeWagePayments(businessId)
    }

    suspend fun recordWagePayment(
        staffUserId: String,
        staffName: String,
        amount: Double,
        period: String,
        paymentMethod: String,
        mpesaCode: String? = null,
        notes: String = ""
    ): Result<WagePaymentEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val payment = WagePaymentEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                staffUserId = staffUserId,
                staffName = staffName,
                amount = amount,
                period = period,
                paymentMethod = paymentMethod,
                mpesaCode = mpesaCode,
                notes = notes,
                createdByUserId = session.userId,
                createdAt = System.currentTimeMillis()
            )
            wagePaymentDao.insertWagePayment(payment)

            // Auto-post double-entry: DR Wages Expense (5030), CR Cash/M-Pesa/Bank
            val wagesAccount = accountDao.getAccountByCode("5030", businessId)
            val cashAccount = when (paymentMethod) {
                "MPESA" -> accountDao.getAccountByCode("1020", businessId)
                "BANK" -> accountDao.getAccountByCode("1030", businessId)
                else -> accountDao.getAccountByCode("1010", businessId)
            }
            if (wagesAccount != null && cashAccount != null) {
                postJournalEntry(
                    date = payment.createdAt,
                    description = "Mshahara kwa $staffName ($period)",
                    sourceType = JournalSourceType.EXPENSE,
                    sourceReferenceId = payment.id,
                    lines = listOf(
                        JournalLineItem(wagesAccount.id, wagesAccount.code, wagesAccount.name, debit = amount, credit = 0.0),
                        JournalLineItem(cashAccount.id, cashAccount.code, cashAccount.name, debit = 0.0, credit = amount)
                    )
                )
            }

            logAudit(
                businessId = businessId,
                actionType = ActionType.ROLE_CHANGED,
                beforeValue = "Wage Payment: $period",
                afterValue = "Paid KSh $amount to $staffName ($paymentMethod)"
            )

            Result.success(payment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // LAYAWAY / AWALI (INSTALLMENT SALES)
    // ==========================================

    fun observeLayaways(): Flow<List<LayawayEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return layawayDao.observeLayaways(businessId)
    }

    fun observeActiveLayaways(): Flow<List<LayawayEntity>> {
        val businessId = getSession().activeBusinessId ?: return flowOf(emptyList())
        return layawayDao.observeActiveLayaways(businessId)
    }

    suspend fun createLayaway(
        customerName: String,
        customerPhone: String,
        description: String,
        totalAmount: Double,
        depositPaid: Double,
        dueDate: Long
    ): Result<LayawayEntity> {
        return try {
            val session = getSession()
            val businessId = getActiveBusinessId()
            val layaway = LayawayEntity(
                id = UUID.randomUUID().toString(),
                businessId = businessId,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                description = description.trim(),
                totalAmount = totalAmount,
                depositPaid = depositPaid,
                totalPaid = depositPaid,
                balanceDue = totalAmount - depositPaid,
                dueDate = dueDate,
                status = if (totalAmount - depositPaid <= 0.0) "COMPLETED" else "ACTIVE",
                createdByUserId = session.userId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            layawayDao.insertLayaway(layaway)

            // If deposit > 0, post journal entry: DR Cash (1010), CR Customer Deposits / Layaway Liability (2040)
            if (depositPaid > 0) {
                val cashAccount = accountDao.getAccountByCode("1010", businessId)
                val depositAccount = accountDao.getAccountByCode("2040", businessId) ?: accountDao.getAccountByCode("2010", businessId)
                if (cashAccount != null && depositAccount != null) {
                    postJournalEntry(
                        date = layaway.createdAt,
                        description = "Layaway Deposit: $customerName ($description)",
                        sourceType = JournalSourceType.CUSTOMER_PAYMENT,
                        sourceReferenceId = layaway.id,
                        lines = listOf(
                            JournalLineItem(cashAccount.id, cashAccount.code, cashAccount.name, debit = depositPaid, credit = 0.0),
                            JournalLineItem(depositAccount.id, depositAccount.code, depositAccount.name, debit = 0.0, credit = depositPaid)
                        )
                    )
                }
            }

            Result.success(layaway)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordLayawayPayment(
        layawayId: String,
        amount: Double,
        paymentMethod: PaymentMethod = PaymentMethod.CASH
    ): Result<LayawayEntity> {
        return try {
            val layaway = layawayDao.getLayawayById(layawayId)
                ?: return Result.failure(Exception("Layaway not found"))

            val newTotalPaid = layaway.totalPaid + amount
            val newBalance = layaway.totalAmount - newTotalPaid
            val newStatus = if (newBalance <= 0.01) "COMPLETED" else "ACTIVE"

            val updated = layaway.copy(
                totalPaid = newTotalPaid,
                balanceDue = if (newBalance > 0) newBalance else 0.0,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            layawayDao.updateLayaway(updated)

            // Post double-entry
            val businessId = layaway.businessId
            val cashAccount = when (paymentMethod) {
                PaymentMethod.MPESA -> accountDao.getAccountByCode("1020", businessId)
                PaymentMethod.BANK -> accountDao.getAccountByCode("1030", businessId)
                else -> accountDao.getAccountByCode("1010", businessId)
            }
            val depositAccount = accountDao.getAccountByCode("2040", businessId) ?: accountDao.getAccountByCode("2010", businessId)
            if (cashAccount != null && depositAccount != null) {
                postJournalEntry(
                    date = System.currentTimeMillis(),
                    description = "Layaway Payment: ${layaway.customerName} - KSh ${amount.toInt()}",
                    sourceType = JournalSourceType.CUSTOMER_PAYMENT,
                    sourceReferenceId = layaway.id,
                    lines = listOf(
                        JournalLineItem(cashAccount.id, cashAccount.code, cashAccount.name, debit = amount, credit = 0.0),
                        JournalLineItem(depositAccount.id, depositAccount.code, depositAccount.name, debit = 0.0, credit = amount)
                    )
                )
            }

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // STOCK AUDIT & RECONCILIATION
    // ==========================================

    suspend fun updateStockItemCount(
        itemId: String,
        physicalCount: Int,
        varianceNote: String = ""
    ): Result<ItemEntity> {
        return try {
            val businessId = getActiveBusinessId()
            val item = itemDao.getItemById(itemId, businessId)
                ?: return Result.failure(Exception("Item not found"))

            val oldQty = item.stockQty
            val variance = physicalCount - oldQty

            val updated = item.copy(
                stockQty = physicalCount,
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING
            )
            itemDao.updateItem(updated)

            logAudit(
                businessId = businessId,
                actionType = ActionType.STOCK_EDITED,
                beforeValue = "${item.name}: Qty was $oldQty",
                afterValue = "Counted $physicalCount (Variance: $variance). $varianceNote"
            )

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

