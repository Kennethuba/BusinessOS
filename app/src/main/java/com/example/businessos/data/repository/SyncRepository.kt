package com.example.businessos.data.repository

import com.example.businessos.data.local.dao.BusinessDao
import com.example.businessos.data.local.dao.BusinessMemberDao
import com.example.businessos.data.local.dao.CustomerDao
import com.example.businessos.data.local.dao.ItemDao
import com.example.businessos.data.local.dao.JobCardDao
import com.example.businessos.data.local.dao.SaleDao
import com.example.businessos.data.local.dao.UserDao
import com.example.businessos.data.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncRepository(
    private val userDao: UserDao,
    private val businessDao: BusinessDao,
    private val businessMemberDao: BusinessMemberDao,
    private val itemDao: ItemDao,
    private val saleDao: SaleDao,
    private val customerDao: CustomerDao,
    private val jobCardDao: JobCardDao
) {
    suspend fun syncAllPending(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var syncedCount = 0

            // 1. Users
            for (user in userDao.getPendingUsers()) {
                userDao.updateUser(user.copy(syncStatus = SyncStatus.SYNCED))
                syncedCount++
            }

            // 2. Businesses
            for (biz in businessDao.getPendingBusinesses()) {
                businessDao.updateBusiness(biz.copy(syncStatus = SyncStatus.SYNCED))
                syncedCount++
            }

            // 3. Business Members
            for (member in businessMemberDao.getPendingMembers()) {
                businessMemberDao.updateMember(member.copy(syncStatus = SyncStatus.SYNCED))
                syncedCount++
            }

            // 4. Job Cards (Garage)
            for (card in jobCardDao.getPendingJobCards()) {
                jobCardDao.updateJobCard(card.copy(syncStatus = SyncStatus.SYNCED))
                syncedCount++
            }

            // 5. Items (Stall)
            for (item in itemDao.getPendingItems()) {
                itemDao.updateItem(item.copy(syncStatus = SyncStatus.SYNCED))
                syncedCount++
            }

            // 6. Sales (POS)
            for (sale in saleDao.getPendingSales()) {
                saleDao.updateSale(sale.copy(syncStatus = SyncStatus.SYNCED))
                syncedCount++
            }

            // 7. Customers (Deni)
            for (cust in customerDao.getPendingCustomers()) {
                customerDao.updateCustomer(cust.copy(syncStatus = SyncStatus.SYNCED))
                syncedCount++
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
