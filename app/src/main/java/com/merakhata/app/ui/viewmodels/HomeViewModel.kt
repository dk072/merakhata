package com.merakhata.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merakhata.app.data.model.CustomerEntity
import com.merakhata.app.data.model.TransactionEntity
import com.merakhata.app.data.repository.KhataRepository
import com.merakhata.app.domain.accounting.AccountingEngine
import com.merakhata.app.domain.accounting.CustomerLedgerSummary
import com.merakhata.app.domain.accounting.DashboardSummary
import com.merakhata.app.domain.accounting.LedgerStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay

enum class CustomerFilter {
    ALL,
    YOU_WILL_RECEIVE,
    YOU_WILL_PAY,
    SETTLED
}

enum class CustomerSort {
    NAME_AZ,
    HIGHEST_BALANCE,
    LOWEST_BALANCE,
    RECENTLY_UPDATED
}

class HomeViewModel(private val repository: KhataRepository) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        startAutoRefreshLoop()
    }

    private fun startAutoRefreshLoop() {
        viewModelScope.launch {
            while (isActive) {
                refreshCloudData(showLoading = false)
                delay(10_000L) // 10 seconds auto-refresh loop
            }
        }
    }

    fun refreshCloudData(showLoading: Boolean = false) {
        viewModelScope.launch {
            if (showLoading) _isRefreshing.value = true
            try {
                val userId = repository.preferences.userId.firstOrNull()
                if (!userId.isNullOrEmpty()) {
                    com.merakhata.app.domain.sync.CloudSyncEngine.fetchCloudDataAndRestore(repository, userId)
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow(CustomerFilter.ALL)
    val selectedFilter: StateFlow<CustomerFilter> = _selectedFilter

    private val _selectedSort = MutableStateFlow(CustomerSort.RECENTLY_UPDATED)
    val selectedSort: StateFlow<CustomerSort> = _selectedSort

    val businessName: StateFlow<String> = repository.preferences.businessName
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val ownerName: StateFlow<String> = repository.preferences.ownerName
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        repository.allCustomers,
        repository.allTransactions
    ) { customers, transactions ->
        AccountingEngine.calculateDashboardSummary(customers, transactions)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardSummary(0, 0, 0, emptyList())
    )

    val filteredCustomers: StateFlow<List<CustomerLedgerSummary>> = combine(
        dashboardSummary,
        _searchQuery,
        _selectedFilter,
        _selectedSort
    ) { summary, query, filter, sort ->
        var list = summary.customerSummaries

        // Search Filter
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { s ->
                s.customer.name.lowercase().contains(q) ||
                        (s.customer.phone?.contains(q) == true)
            }
        }

        // Category Filter
        list = when (filter) {
            CustomerFilter.ALL -> list
            CustomerFilter.YOU_WILL_RECEIVE -> list.filter { it.status == LedgerStatus.YOU_WILL_RECEIVE }
            CustomerFilter.YOU_WILL_PAY -> list.filter { it.status == LedgerStatus.YOU_WILL_PAY }
            CustomerFilter.SETTLED -> list.filter { it.status == LedgerStatus.SETTLED }
        }

        // Sorting
        when (sort) {
            CustomerSort.NAME_AZ -> list.sortedBy { it.customer.name.lowercase() }
            CustomerSort.HIGHEST_BALANCE -> list.sortedByDescending { it.netBalanceMinor }
            CustomerSort.LOWEST_BALANCE -> list.sortedBy { it.netBalanceMinor }
            CustomerSort.RECENTLY_UPDATED -> list.sortedByDescending { it.lastTransactionDate ?: it.customer.createdAt }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelect(filter: CustomerFilter) {
        _selectedFilter.value = filter
    }

    fun onSortSelect(sort: CustomerSort) {
        _selectedSort.value = sort
    }
}
