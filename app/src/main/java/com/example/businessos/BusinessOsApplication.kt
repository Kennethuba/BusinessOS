package com.example.businessos

import android.app.Application
import com.example.businessos.data.local.AppDatabase
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import com.example.businessos.data.security.AppLock
import com.example.businessos.data.security.PrefsSessionStore
import com.example.businessos.ui.ViewModelFactory

class BusinessOsApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val appLock: AppLock by lazy { AppLock(this) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(
            database = database,
            sessionStore = PrefsSessionStore(this),
            userDao = database.userDao(),
            businessDao = database.businessDao(),
            businessMemberDao = database.businessMemberDao()
        )
    }

    val businessOsRepository: BusinessOsRepository by lazy {
        BusinessOsRepository(
            database = database,
            authRepository = authRepository,
            businessDao = database.businessDao(),
            businessMemberDao = database.businessMemberDao(),
            userDao = database.userDao(),
            itemDao = database.itemDao(),
            saleDao = database.saleDao(),
            saleItemDao = database.saleItemDao(),
            stockMovementDao = database.stockMovementDao(),
            documentCounterDao = database.documentCounterDao(),
            customerDao = database.customerDao(),
            jobCardDao = database.jobCardDao(),
            jobPartDao = database.jobPartDao(),
            jobStageTransitionDao = database.jobStageTransitionDao(),
            auditLogDao = database.auditLogDao(),
            expenseDao = database.expenseDao(),
            accountDao = database.accountDao(),
            journalEntryDao = database.journalEntryDao(),
            journalLineDao = database.journalLineDao(),
            invoiceDao = database.invoiceDao(),
            invoiceItemDao = database.invoiceItemDao(),
            supplierDao = database.supplierDao(),
            paymentDao = database.paymentDao(),
            wagePaymentDao = database.wagePaymentDao(),
            layawayDao = database.layawayDao()
        )
    }

    val viewModelFactory: ViewModelFactory by lazy {
        ViewModelFactory(
            authRepository = authRepository,
            businessRepository = businessOsRepository
        )
    }
}
