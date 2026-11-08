package com.example.businessos.data.repository

import com.example.businessos.data.local.dao.BusinessDao
import com.example.businessos.data.local.dao.BusinessMemberDao
import com.example.businessos.data.local.dao.UserDao
import com.example.businessos.data.local.entity.BusinessEntity
import com.example.businessos.data.local.entity.BusinessMemberEntity
import com.example.businessos.data.local.entity.UserEntity
import com.example.businessos.data.model.BusinessType
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.model.Role
import com.example.businessos.data.model.SubscriptionPlan
import com.example.businessos.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepository(
    private val userDao: UserDao,
    private val businessDao: BusinessDao,
    private val businessMemberDao: BusinessMemberDao
) {
    private val _session = MutableStateFlow<CurrentUserSession?>(null)
    val session: StateFlow<CurrentUserSession?> = _session.asStateFlow()

    suspend fun login(emailOrPhone: String): Result<UserEntity> {
        val user = userDao.getUserByAuthId(emailOrPhone)
            ?: userDao.getUserById(emailOrPhone)
            ?: return Result.failure(Exception("User not found with $emailOrPhone"))

        val businesses = businessDao.getBusinessesForUser(user.id)
        val firstBiz = businesses.firstOrNull()
        val role = firstBiz?.let { businessMemberDao.getMember(it.id, user.id)?.role }

        _session.value = CurrentUserSession(
            userId = user.id,
            name = user.name,
            email = user.email,
            phone = user.phone,
            activeBusinessId = firstBiz?.id,
            activeRole = role,
            activeBusinessName = firstBiz?.name,
            activeBusinessType = firstBiz?.type
        )
        return Result.success(user)
    }

    suspend fun signup(name: String, email: String, phone: String): Result<UserEntity> {
        val userId = UUID.randomUUID().toString()
        val user = UserEntity(
            id = userId,
            name = name,
            email = email,
            phone = phone,
            authId = email,
            syncStatus = SyncStatus.PENDING
        )
        userDao.insertUser(user)

        _session.value = CurrentUserSession(
            userId = user.id,
            name = user.name,
            email = user.email,
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
            activeBusinessType = business.type
        )
        return Result.success(Unit)
    }

    fun observeUserBusinesses(userId: String): Flow<List<BusinessEntity>> {
        return businessDao.observeBusinessesForUser(userId)
    }

    suspend fun createBusiness(
        name: String,
        type: BusinessType,
        plan: SubscriptionPlan = SubscriptionPlan.STARTER
    ): Result<BusinessEntity> {
        val current = _session.value ?: return Result.failure(Exception("Not logged in"))

        // Subscription Limit Check: count businesses owned
        val ownedCount = businessDao.getCountOwnedBy(current.userId)
        if (ownedCount >= plan.maxBusinesses) {
            return Result.failure(
                IllegalStateException(
                    "Plan limit reached! Your ${plan.name} plan allows up to ${plan.maxBusinesses} business(es). Upgrade to add more."
                )
            )
        }

        val bizId = UUID.randomUUID().toString()
        val business = BusinessEntity(
            id = bizId,
            name = name,
            type = type,
            ownerId = current.userId,
            subscriptionPlan = plan,
            syncStatus = SyncStatus.PENDING
        )
        businessDao.insertBusiness(business)

        val membership = BusinessMemberEntity(
            businessId = bizId,
            userId = current.userId,
            role = Role.OWNER,
            syncStatus = SyncStatus.PENDING
        )
        businessMemberDao.insertMember(membership)

        switchWorkspace(bizId)
        return Result.success(business)
    }

    fun logout() {
        _session.value = null
    }
}
