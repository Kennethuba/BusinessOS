package com.example.businessos.data.repository

import androidx.room.withTransaction
import com.example.businessos.data.local.AppDatabase
import com.example.businessos.data.local.DefaultAccounts
import com.example.businessos.data.local.dao.BusinessDao
import com.example.businessos.data.local.dao.BusinessMemberDao
import com.example.businessos.data.local.dao.UserDao
import com.example.businessos.data.local.entity.BusinessEntity
import com.example.businessos.data.local.entity.BusinessMemberEntity
import com.example.businessos.data.local.entity.UserEntity
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.Role
import com.example.businessos.data.security.LockoutPolicy
import com.example.businessos.data.security.PinSecurity
import com.example.businessos.data.security.SessionStore
import com.example.businessos.util.KenyanPhone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/** A person who can unlock the app on this phone. */
data class LockCandidate(val userId: String, val name: String, val role: Role)

class AuthRepository(
    private val database: AppDatabase,
    private val userDao: UserDao,
    private val businessDao: BusinessDao,
    private val businessMemberDao: BusinessMemberDao,
    private val sessionStore: SessionStore = SessionStore.None,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val _session = MutableStateFlow<CurrentUserSession?>(null)
    val session: StateFlow<CurrentUserSession?> = _session.asStateFlow()

    /** Checks the PIN and applies the wrong-PIN lockout rules. */
    private suspend fun checkPin(user: UserEntity, pin: String): Result<Unit> {
        val now = clock()
        if (user.lockoutUntil > now) {
            val seconds = (user.lockoutUntil - now + 999) / 1000
            return Result.failure(Exception("Too many wrong PINs. Try again in ${waitText(seconds)}."))
        }
        if (user.pinHash.isEmpty()) {
            return Result.failure(Exception("This account has no PIN yet. Ask the owner to set one."))
        }
        if (PinSecurity.verifyPin(pin, user.pinSalt, user.pinHash)) {
            if (user.failedAttempts != 0 || user.lockoutUntil != 0L) userDao.clearLockout(user.id)
            return Result.success(Unit)
        }
        val attempts = user.failedAttempts + 1
        val waitMillis = LockoutPolicy.lockoutMillis(attempts)
        userDao.updateLockout(user.id, attempts, if (waitMillis > 0) now + waitMillis else 0L)
        return if (waitMillis > 0) {
            Result.failure(Exception("Wrong PIN. Locked for ${waitText(waitMillis / 1000)}."))
        } else {
            val left = LockoutPolicy.triesLeftBeforeLock(attempts)
            Result.failure(Exception("Wrong PIN. $left tries left before a short lock."))
        }
    }

    private fun waitText(seconds: Long): String =
        if (seconds >= 60) "${(seconds + 59) / 60} min" else "$seconds sec"

    suspend fun login(phone: String, pin: String): Result<UserEntity> {
        val normalized = KenyanPhone.normalize(phone)
            ?: return Result.failure(Exception("Enter a valid phone number, for example 0712345678"))
        val user = userDao.getUserByPhone(normalized)
            ?: return Result.failure(Exception("No account with that phone number"))
        if (!PinSecurity.isValidPin(pin)) {
            return Result.failure(Exception("Enter your ${PinSecurity.PIN_LENGTH}-digit PIN"))
        }
        checkPin(user, pin).onFailure { return Result.failure(it) }

        _session.value = buildSession(user, preferredBusinessId = sessionStore.load()?.second)
        persist()
        return Result.success(user)
    }

    suspend fun signup(name: String, phone: String, pin: String): Result<UserEntity> {
        if (name.isBlank()) return Result.failure(Exception("Please enter your name"))
        val normalized = KenyanPhone.normalize(phone)
            ?: return Result.failure(Exception("Enter a valid phone number, for example 0712345678"))
        if (!PinSecurity.isValidPin(pin)) {
            return Result.failure(Exception("Your PIN must be exactly ${PinSecurity.PIN_LENGTH} digits"))
        }
        if (userDao.getUserByPhone(normalized) != null) {
            return Result.failure(Exception("An account with this phone number already exists"))
        }

        val salt = PinSecurity.generateSalt()
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            phone = normalized,
            pinSalt = salt,
            pinHash = PinSecurity.hashPin(pin, salt),
            createdAt = clock()
        )
        userDao.insertUser(user)

        _session.value = CurrentUserSession(userId = user.id, name = user.name, phone = user.phone)
        persist()
        return Result.success(user)
    }

    /** Brings back the remembered session after the app was closed. The caller must lock the app. */
    suspend fun restoreSession(): Boolean {
        val (userId, businessId) = sessionStore.load() ?: return false
        val user = userDao.getUserById(userId)
        if (user == null) {
            sessionStore.clear()
            return false
        }
        _session.value = buildSession(user, businessId)
        return true
    }

    /** People who can unlock this phone: the members of the active business. */
    suspend fun lockCandidates(): List<LockCandidate> {
        val current = _session.value ?: return emptyList()
        val bizId = current.activeBusinessId
            ?: return listOf(LockCandidate(current.userId, current.name, current.activeRole ?: Role.OWNER))
        return businessMemberDao.getMembers(bizId).mapNotNull { member ->
            userDao.getUserById(member.userId)?.let { LockCandidate(it.id, it.name, member.role) }
        }
    }

    /** Unlocks the app as [userId]. Another staff member can take over by entering their own PIN. */
    suspend fun unlock(userId: String, pin: String): Result<Unit> {
        if (!PinSecurity.isValidPin(pin)) {
            return Result.failure(Exception("Enter your ${PinSecurity.PIN_LENGTH}-digit PIN"))
        }
        val user = userDao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        checkPin(user, pin).onFailure { return Result.failure(it) }

        val current = _session.value
        val bizId = current?.activeBusinessId
        if (current == null || bizId == null) {
            _session.value = buildSession(user, preferredBusinessId = null)
        } else {
            val member = businessMemberDao.getMember(bizId, userId)
                ?: return Result.failure(Exception("This person does not belong to this business"))
            _session.value = current.copy(
                userId = user.id,
                name = user.name,
                phone = user.phone,
                activeRole = member.role
            )
        }
        persist()
        return Result.success(Unit)
    }

    suspend fun changePin(userId: String, oldPin: String, newPin: String): Result<Unit> {
        if (!PinSecurity.isValidPin(newPin)) {
            return Result.failure(Exception("The new PIN must be exactly ${PinSecurity.PIN_LENGTH} digits"))
        }
        val user = userDao.getUserById(userId) ?: return Result.failure(Exception("User not found"))
        checkPin(user, oldPin).onFailure { return Result.failure(it) }
        val salt = PinSecurity.generateSalt()
        userDao.updateUser(user.copy(pinSalt = salt, pinHash = PinSecurity.hashPin(newPin, salt), failedAttempts = 0, lockoutUntil = 0L))
        return Result.success(Unit)
    }

    private suspend fun buildSession(user: UserEntity, preferredBusinessId: String?): CurrentUserSession {
        val businesses = businessDao.getBusinessesForUser(user.id)
        val business = businesses.firstOrNull { it.id == preferredBusinessId } ?: businesses.firstOrNull()
        val role = business?.let { businessMemberDao.getMember(it.id, user.id)?.role }
        return CurrentUserSession(
            userId = user.id,
            name = user.name,
            phone = user.phone,
            activeBusinessId = business?.id,
            activeRole = role,
            activeBusinessName = business?.name,
            isShopEnabled = business?.isShopEnabled ?: true,
            isGarageEnabled = business?.isGarageEnabled ?: false,
            isDemo = business?.isDemo ?: false
        )
    }

    private fun persist() {
        val current = _session.value
        if (current == null) sessionStore.clear() else sessionStore.save(current.userId, current.activeBusinessId)
    }

    suspend fun switchWorkspace(businessId: String): Result<Unit> {
        val current = _session.value ?: return Result.failure(Exception("Not logged in"))
        val business = businessDao.getBusinessById(businessId)
            ?: return Result.failure(Exception("Business not found"))
        val member = businessMemberDao.getMember(businessId, current.userId)
            ?: return Result.failure(Exception("User does not belong to this business"))

        _session.value = current.copy(
            activeBusinessId = business.id,
            activeRole = member.role,
            activeBusinessName = business.name,
            isShopEnabled = business.isShopEnabled,
            isGarageEnabled = business.isGarageEnabled,
            isDemo = business.isDemo
        )
        persist()
        return Result.success(Unit)
    }

    fun observeUserBusinesses(userId: String): Flow<List<BusinessEntity>> {
        return businessDao.observeBusinessesForUser(userId)
    }

    suspend fun createBusiness(
        name: String,
        phone: String = "",
        isShopEnabled: Boolean = true,
        isGarageEnabled: Boolean = false,
        isDemo: Boolean = false,
        openingCash: Money = Money.ZERO,
        openingMpesa: Money = Money.ZERO
    ): Result<BusinessEntity> {
        if (openingCash < Money.ZERO || openingMpesa < Money.ZERO) {
            return Result.failure(IllegalArgumentException("Opening balances cannot be negative"))
        }
        val current = _session.value ?: return Result.failure(Exception("Not logged in"))

        val bizId = UUID.randomUUID().toString()
        val business = BusinessEntity(
            id = bizId,
            name = name.trim(),
            phone = phone.ifBlank { current.phone },
            address = "",
            receiptFooter = "",
            isShopEnabled = isShopEnabled,
            isGarageEnabled = isGarageEnabled,
            isDemo = isDemo,
            allowNegativeStock = false,
            ownerId = current.userId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val membership = BusinessMemberEntity(
            businessId = bizId,
            userId = current.userId,
            role = Role.OWNER,
            canSellNegativeStock = true
        )

        val defaultAccounts = DefaultAccounts.createDefaultAccounts(bizId)

        // Seed business, owner membership, and default system accounts in ONE atomic transaction
        database.withTransaction {
            businessDao.insertBusiness(business)
            businessMemberDao.insertMember(membership)
            database.accountDao().insertAccounts(defaultAccounts)
            // Opening balances: Dr cash / M-Pesa, Cr owner's capital. A new business is otherwise empty.
            postOpening(bizId, defaultAccounts, "1010", openingCash, "Opening cash balance", current.userId)
            postOpening(bizId, defaultAccounts, "1020", openingMpesa, "Opening M-Pesa balance", current.userId)
        }

        switchWorkspace(bizId)
        return Result.success(business)
    }

    private suspend fun postOpening(
        bizId: String,
        accounts: List<com.example.businessos.data.local.entity.AccountEntity>,
        code: String,
        amount: Money,
        description: String,
        userId: String
    ) {
        if (amount <= Money.ZERO) return
        val asset = accounts.first { it.code == code }
        val capital = accounts.first { it.code == "3010" }
        val entryId = UUID.randomUUID().toString()
        val now = clock()
        database.journalEntryDao().insertEntry(
            com.example.businessos.data.local.entity.JournalEntryEntity(
                id = entryId,
                businessId = bizId,
                entryNumber = database.documentCounterDao().nextNumber(bizId, com.example.businessos.data.model.DocumentType.JOURNAL, "JRN-"),
                date = now,
                description = description,
                sourceType = com.example.businessos.data.model.JournalSourceType.OPENING_BALANCE,
                totalDebitCents = amount.cents,
                totalCreditCents = amount.cents,
                createdByUserId = userId,
                createdAt = now
            )
        )
        database.journalLineDao().insertLines(
            listOf(
                com.example.businessos.data.local.entity.JournalLineEntity(UUID.randomUUID().toString(), entryId, asset.id, amount.cents, 0L, ""),
                com.example.businessos.data.local.entity.JournalLineEntity(UUID.randomUUID().toString(), entryId, capital.id, 0L, amount.cents, "")
            )
        )
        database.accountDao().adjustBalance(asset.id, bizId, amount.cents)
        database.accountDao().adjustBalance(capital.id, bizId, amount.cents)
    }

    /** Owner-only. Removes the business and everything under it (foreign keys cascade). */
    suspend fun deleteBusiness(businessId: String, typedName: String): Result<Unit> {
        val current = _session.value ?: return Result.failure(Exception("Not logged in"))
        val business = businessDao.getBusinessById(businessId)
            ?: return Result.failure(Exception("Business not found"))
        if (business.ownerId != current.userId) {
            return Result.failure(SecurityException("Only the owner can delete a business"))
        }
        if (typedName.trim() != business.name) {
            return Result.failure(IllegalArgumentException("Type the business name exactly to confirm"))
        }
        businessDao.deleteBusiness(businessId)
        if (current.activeBusinessId == businessId) {
            _session.value = current.copy(activeBusinessId = null, activeBusinessName = null, activeRole = null)
            persist()
        }
        return Result.success(Unit)
    }

    fun logout() {
        _session.value = null
        sessionStore.clear()
    }
}
