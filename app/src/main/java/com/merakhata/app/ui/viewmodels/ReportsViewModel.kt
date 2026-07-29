package com.merakhata.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merakhata.app.data.repository.KhataRepository
import com.merakhata.app.domain.accounting.AccountingEngine
import com.merakhata.app.domain.accounting.PeriodReportSummary
import kotlinx.coroutines.flow.*
import java.util.Calendar

enum class ReportPeriod {
    TODAY,
    LAST_7_DAYS,
    THIS_MONTH,
    CUSTOM
}

class ReportsViewModel(private val repository: KhataRepository) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod

    private val _customStartDate = MutableStateFlow(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L))
    val customStartDate: StateFlow<Long> = _customStartDate

    private val _customEndDate = MutableStateFlow(System.currentTimeMillis())
    val customEndDate: StateFlow<Long> = _customEndDate

    val reportSummary: StateFlow<PeriodReportSummary> = combine(
        repository.allTransactions,
        _selectedPeriod,
        _customStartDate,
        _customEndDate
    ) { txs, period, customStart, customEnd ->
        val (start, end) = when (period) {
            ReportPeriod.TODAY -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                Pair(cal.timeInMillis, System.currentTimeMillis())
            }
            ReportPeriod.LAST_7_DAYS -> {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                    set(Calendar.HOUR_OF_DAY, 0)
                }
                Pair(cal.timeInMillis, System.currentTimeMillis())
            }
            ReportPeriod.THIS_MONTH -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                Pair(cal.timeInMillis, System.currentTimeMillis())
            }
            ReportPeriod.CUSTOM -> Pair(customStart, customEnd)
        }

        AccountingEngine.calculatePeriodReport(txs, start, end)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PeriodReportSummary(0, 0, 0, 0, 0)
    )

    fun onPeriodSelect(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    fun onCustomDatesChange(start: Long, end: Long) {
        _customStartDate.value = start
        _customEndDate.value = end
    }
}
