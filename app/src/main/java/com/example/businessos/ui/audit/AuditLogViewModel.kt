package com.example.businessos.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.businessos.data.local.entity.AuditLogEntity
import com.example.businessos.data.model.ActionType
import com.example.businessos.data.model.CurrentUserSession
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.data.repository.BusinessOsRepository
import kotlinx.coroutines.flow.*

data class AuditLogUiState(
    val logs: List<AuditLogEntity> = emptyList(),
    val selectedFilter: ActionType? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuditLogViewModel(
    private val authRepository: AuthRepository,
    private val repository: BusinessOsRepository
) : ViewModel() {

    val session: StateFlow<CurrentUserSession?> = authRepository.session

    private val _selectedFilter = MutableStateFlow<ActionType?>(null)

    private val rawLogs: StateFlow<List<AuditLogEntity>> = repository.observeAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<AuditLogUiState> = combine(
        rawLogs,
        _selectedFilter
    ) { logs, filter ->
        val filteredLogs = if (filter == null) {
            logs
        } else {
            logs.filter { it.actionType == filter }
        }
        AuditLogUiState(
            logs = filteredLogs,
            selectedFilter = filter,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AuditLogUiState(isLoading = true)
    )

    fun setFilter(filter: ActionType?) {
        _selectedFilter.value = filter
    }
}
