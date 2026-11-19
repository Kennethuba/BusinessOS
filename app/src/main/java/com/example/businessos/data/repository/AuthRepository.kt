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
import com.example.businessos.data.model.Role
import com.example.businessos.data.security.PinSecurity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepository(
    private val database: AppDatabase,
    private val userDao: UserDao,
    private val businessDao: BusinessDao,
    private val businessMemberDao: BusinessMemberDao
) {
    private val _session = MutableStateFlow<CurrentUserSession?>(null)
    val session: StateFlow<CurrentUserSession?> = _session.asStateFlow()

    suspend fun login(phone: String, pin: String): Result<UserEntity> {
        val cleanPhone = phone.trim()
        val user = userDao.getUserByPhone(cleanPhone)
            ?: userDao.getUserById(cleanPhone)
            ?: return Result.failure(Exception("User not found with phone $cleanPhone"))

        val now = System.currentTimeMillis()
        if (user.lockoutUntil > now) {
            val remainingSec = (user.lockoutUntil - now) / 1000
            return Result.failure(Exception("Account temporarily locked due to failed attempts. Try again in ${remainingSec}s"))
        }

        // Verify PIN if set, or legacy transition
        if (user.pinHash.isNotEmpty()) {
            val valid = PinSecurity.verifyPin(pin, user.pinSalt, user.pinHash)
            if (!valid) {
                val attempts = user.failedAttempts + 1
                val lockoutTime = if (attempts >= 5) now + (5 * 60 * 1000L) else 0L
                userDao.updateLockout(user.id, attempts, if (lockoutTime > 0) lockoutTime else null)
                return Result.failure(Exception("Incorrect PIN. Attempt $attempts of 5."))
            }
        }

        // Reset lockout on successful login
        userDao.clearLockout(user.id)

        val businesses = businessDao.getBusinessesForUser(user.id)
        val firstBiz = businesses.firstOrNull()
        val role = firstBiz?.let { businessMemberDao.getMember(it.id, user.id)?.role }

        _session.value = CurrentUserSession(
            userId = user.id,
            name = user.name,
            phone = user.phone,
            activeBusinessId = firstBiz?.id,
            activeRole = role,
            activeBusinessName = firstBiz?.name,
            isShopEnabled = firstBiz?.isShopEnabled ?: true,
            isGarageEnabled = firstBiz?.isGarageEnabled ?: false,
            isDemo = firstBiz?.isDemo ?: false
        )
        return Result.success(user)
    }

    suspend fun signup(name: String, phone: String, pin: String): Result<UserEntity> {
        val cleanPhone = phone.trim()
        val existing = userDao.getUserByPhone(cleanPhone)
        if (existing != null) {
            return Result.failure(Exception("A user with phone $cleanPhone already exists"))
        }

        val salt = PinSecurity.generateSalt()
        val hash = PinSecurity.hashPin(pin, salt)
        val userId = UUID.randomUUID().toString()
        val user = UserEntity(
            id = userId,
            name = name.trim(),
            phone = cleanPhone,
            pinSalt = salt,
            pinHash = hash,
            failedAttempts = 0,
            lockoutUntil = 0L,
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(user)

        _session.value = CurrentUserSession(
            userId = user.id,
            name = user.name,
            phone = user.phone
        )
        return Result.success(user)
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
        isDemo: Boolean = false
    ): Result<BusinessEntity> {
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
            canSellNegativeStock = true,
            joinedAt = System.currentTimeMillis()
        )

        val defaultAccounts = DefaultAccounts.createDefaultAccounts(bizId)

        // Seed business, owner membership, and default system accounts in ONE atomic transaction
        database.withTransaction {
            businessDao.insertBusiness(business)
            businessMemberDao.insertMember(membership)
            database.accountDao().insertAccounts(defaultAccounts)
        }

        switchWorkspace(bizId)
        return Result.success(business)
    }

    fun logout() {
        _session.value = null
    }
}
