package com.example.businessos

import android.app.Application
import com.example.businessos.data.local.AppDatabase
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import com.example.businessos.data.repository.SyncRepository
import com.example.businessos.sync.SyncManager
import com.example.businessos.ui.ViewModelFactory

class BusinessOsApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            userDao = database.userDao(),
            businessDao = database.businessDao(),
            businessMemberDao = database.businessMemberDao()
        )
    }

    val businessOsRepository: BusinessOsRepository by lazy {
        BusinessOsRepository(
            authRepository = authRepository,
            businessDao = database.businessDao(),
            businessMemberDao = database.businessMemberDao(),
            userDao = database.userDao(),
            itemDao = database.itemDao(),
            saleDao = database.saleDao(),
            customerDao = database.customerDao(),
            jobCardDao = database.jobCardDao(),
            auditLogDao = database.auditLogDao(),
            expenseDao = database.expenseDao(),
            accountDao = database.accountDao(),
            journalEntryDao = database.journalEntryDao(),
            invoiceDao = database.invoiceDao(),
            supplierDao = database.supplierDao(),
            billDao = database.billDao(),
            paymentDao = database.paymentDao(),
            assetDao = database.assetDao(),
            liabilityDao = database.liabilityDao(),
            wagePaymentDao = database.wagePaymentDao(),
            layawayDao = database.layawayDao()
        )
    }

    val syncRepository: SyncRepository by lazy {
        SyncRepository(
            userDao = database.userDao(),
            businessDao = database.businessDao(),
            businessMemberDao = database.businessMemberDao(),
            itemDao = database.itemDao(),
            saleDao = database.saleDao(),
            customerDao = database.customerDao(),
            jobCardDao = database.jobCardDao()
        )
    }

    val syncManager: SyncManager by lazy {
        SyncManager(
            context = this,
            syncRepository = syncRepository
        )
    }

    val viewModelFactory: ViewModelFactory by lazy {
        ViewModelFactory(
            authRepository = authRepository,
            businessRepository = businessOsRepository,
            syncManager = syncManager
        )
    }
}
