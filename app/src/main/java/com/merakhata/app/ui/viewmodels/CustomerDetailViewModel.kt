package com.merakhata.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merakhata.app.data.model.CustomerEntity
import com.merakhata.app.data.model.TransactionEntity
import com.merakhata.app.data.repository.KhataRepository
import com.merakhata.app.domain.accounting.AccountingEngine
import com.merakhata.app.domain.accounting.CustomerLedgerSummary
import com.merakhata.app.domain.pdf.PdfGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomerDetailViewModel(
    private val repository: KhataRepository,
    private val customerId: Long
) : ViewModel() {

    val customer: StateFlow<CustomerEntity?> = repository.getCustomerById(customerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<TransactionEntity>> = repository.getTransactionsForCustomer(customerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<CustomerLedgerSummary?> = combine(customer, transactions) { cust, txs ->
        if (cust != null) {
            AccountingEngine.calculateCustomerSummary(cust, txs)
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val businessName: StateFlow<String> = repository.preferences.businessName
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            com.merakhata.app.domain.sync.CloudSyncManager.syncAll(repository)
        }
    }

    fun deleteCustomer(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCustomerById(customerId)
            com.merakhata.app.domain.sync.CloudSyncManager.syncAll(repository)
            onDeleted()
        }
    }

    fun generateAndSharePdf(context: Context) {
        val cust = customer.value ?: return
        val txs = transactions.value
        val bName = businessName.value

        viewModelScope.launch {
            val pdfFile = PdfGenerator.generateCustomerStatementPdf(context, bName, cust, txs)
            if (pdfFile != null) {
                PdfGenerator.sharePdfFile(context, pdfFile)
            }
        }
    }

    fun generateShareSummaryText(): String {
        val cust = customer.value ?: return ""
        val sum = summary.value ?: return ""

        val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        val gaveStr = AccountingEngine.formatCurrency(sum.totalGaveMinor)
        val gotStr = AccountingEngine.formatCurrency(sum.totalGotMinor)
        val pendStr = AccountingEngine.formatCurrency(kotlin.math.abs(sum.netBalanceMinor))

        val statusLabel = when (sum.status) {
            com.merakhata.app.domain.accounting.LedgerStatus.YOU_WILL_RECEIVE -> "Pending Receivable"
            com.merakhata.app.domain.accounting.LedgerStatus.YOU_WILL_PAY -> "Pending Payable"
            com.merakhata.app.domain.accounting.LedgerStatus.SETTLED -> "Settled"
        }

        return """
            *Mera Khata Statement*
            Customer: ${cust.name}
            Phone: ${cust.phone ?: "N/A"}
            
            Total Given: $gaveStr
            Total Received: $gotStr
            $statusLabel: $pendStr
            
            Last Updated: $dateStr
        """.trimIndent()
    }
}
