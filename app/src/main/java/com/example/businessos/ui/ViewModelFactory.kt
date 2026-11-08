package com.example.businessos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import com.example.businessos.sync.SyncManager
import com.example.businessos.ui.audit.AuditLogViewModel
import com.example.businessos.ui.auth.AuthViewModel
import com.example.businessos.ui.deni.DeniViewModel
import com.example.businessos.ui.garage.GarageViewModel
import com.example.businessos.ui.home.HomeViewModel
import com.example.businessos.ui.pos.PosViewModel
import com.example.businessos.ui.staff.StaffViewModel
import com.example.businessos.ui.subscription.SubscriptionViewModel
import com.example.businessos.ui.workspace.WorkspaceViewModel

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessOsRepository,
    private val syncManager: SyncManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository, syncManager) as T
            }
            modelClass.isAssignableFrom(WorkspaceViewModel::class.java) -> {
                WorkspaceViewModel(authRepository, syncManager) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(authRepository, businessRepository, syncManager) as T
            }
            modelClass.isAssignableFrom(PosViewModel::class.java) -> {
                PosViewModel(authRepository, businessRepository, syncManager) as T
            }
            modelClass.isAssignableFrom(DeniViewModel::class.java) -> {
                DeniViewModel(authRepository, businessRepository, syncManager) as T
            }
            modelClass.isAssignableFrom(GarageViewModel::class.java) -> {
                GarageViewModel(authRepository, businessRepository, syncManager) as T
            }
            modelClass.isAssignableFrom(StaffViewModel::class.java) -> {
                StaffViewModel(authRepository, businessRepository, syncManager) as T
            }
            modelClass.isAssignableFrom(AuditLogViewModel::class.java) -> {
                AuditLogViewModel(authRepository, businessRepository, syncManager) as T
            }
            modelClass.isAssignableFrom(SubscriptionViewModel::class.java) -> {
                SubscriptionViewModel(authRepository, businessRepository) as T
            }
            modelClass.isAssignableFrom(com.example.businessos.ui.accounting.AccountingViewModel::class.java) -> {
                com.example.businessos.ui.accounting.AccountingViewModel(authRepository, businessRepository, syncManager) as T
            }
            modelClass.isAssignableFrom(com.example.businessos.ui.assistant.AssistantViewModel::class.java) -> {
                com.example.businessos.ui.assistant.AssistantViewModel(authRepository, businessRepository, syncManager) as T
            }
            modelClass.isAssignableFrom(com.example.businessos.ui.pos.LayawayViewModel::class.java) -> {
                com.example.businessos.ui.pos.LayawayViewModel(authRepository, businessRepository, syncManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
