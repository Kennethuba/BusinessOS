package com.example.businessos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import com.example.businessos.ui.accounting.AccountingViewModel
import com.example.businessos.ui.audit.AuditLogViewModel
import com.example.businessos.ui.auth.AuthViewModel
import com.example.businessos.ui.deni.DeniViewModel
import com.example.businessos.ui.garage.GarageViewModel
import com.example.businessos.ui.home.HomeViewModel
import com.example.businessos.ui.pos.LayawayViewModel
import com.example.businessos.ui.pos.PosViewModel
import com.example.businessos.ui.staff.StaffViewModel
import com.example.businessos.ui.workspace.WorkspaceViewModel

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessOsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(WorkspaceViewModel::class.java) -> {
                WorkspaceViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(authRepository, businessRepository) as T
            }
            modelClass.isAssignableFrom(PosViewModel::class.java) -> {
                PosViewModel(authRepository, businessRepository) as T
            }
            modelClass.isAssignableFrom(DeniViewModel::class.java) -> {
                DeniViewModel(authRepository, businessRepository) as T
            }
            modelClass.isAssignableFrom(GarageViewModel::class.java) -> {
                GarageViewModel(authRepository, businessRepository) as T
            }
            modelClass.isAssignableFrom(StaffViewModel::class.java) -> {
                StaffViewModel(authRepository, businessRepository) as T
            }
            modelClass.isAssignableFrom(AuditLogViewModel::class.java) -> {
                AuditLogViewModel(authRepository, businessRepository) as T
            }
            modelClass.isAssignableFrom(AccountingViewModel::class.java) -> {
                AccountingViewModel(authRepository, businessRepository) as T
            }
            modelClass.isAssignableFrom(LayawayViewModel::class.java) -> {
                LayawayViewModel(authRepository, businessRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
