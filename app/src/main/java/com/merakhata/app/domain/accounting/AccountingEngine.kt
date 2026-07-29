package com.merakhata.app.domain.accounting

import com.merakhata.app.data.model.CustomerEntity
import com.merakhata.app.data.model.TransactionEntity
import com.merakhata.app.data.model.TransactionType
import java.text.NumberFormat
import java.util.Locale

enum class LedgerStatus {
    YOU_WILL_RECEIVE,
    YOU_WILL_PAY,
    SETTLED
}

data class CustomerLedgerSummary(
    val customer: CustomerEntity,
    val totalGaveMinor: Long,
    val totalGotMinor: Long,
    val netBalanceMinor: Long, // Positive = Receivable, Negative = Payable
    val status: LedgerStatus,
    val lastTransactionDate: Long?
)

data class DashboardSummary(
    val totalReceivableMinor: Long,
    val totalPayableMinor: Long,
    val netBalanceMinor: Long,
    val customerSummaries: List<CustomerLedgerSummary>
)

data class PeriodReportSummary(
    val totalCreditGivenMinor: Long,
    val totalPaymentReceivedMinor: Long,
    val totalCreditTakenMinor: Long, // When User received credit (Payable transactions)
    val totalPaymentMadeMinor: Long,  // When User made payment to clear payable
    val totalTransactionsCount: Int
)

object AccountingEngine {

    /**
     * Converts a display amount string (e.g. "500", "1250.50") into integer minor units (paise).
     * 1 Rupee = 100 Paise.
     */
    fun parseToMinorUnits(input: String): Long {
        val sanitized = input.replace(",", "").trim()
        if (sanitized.isEmpty()) return 0L
        return try {
            val parts = sanitized.split(".")
            val rupees = parts[0].toLongOrNull() ?: 0L
            val paise = if (parts.size > 1) {
                val pStr = parts[1].padEnd(2, '0').take(2)
                pStr.toLongOrNull() ?: 0L
            } else 0L
            (rupees * 100) + paise
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Formats integer minor units into currency string with symbol.
     */
    fun formatCurrency(amountMinor: Long, symbol: String = "₹"): String {
        val absMinor = kotlin.math.abs(amountMinor)
        val rupees = absMinor / 100
        val paise = absMinor % 100

        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        val formattedRupees = formatter.format(rupees)

        val amountStr = if (paise > 0) {
            "$symbol$formattedRupees.${paise.toString().padStart(2, '0')}"
        } else {
            "$symbol$formattedRupees"
        }

        return if (amountMinor < 0) "-$amountStr" else amountStr
    }

    /**
     * Calculates customer ledger summary for a single customer based on their transactions.
     */
    fun calculateCustomerSummary(
        customer: CustomerEntity,
        transactions: List<TransactionEntity>
    ): CustomerLedgerSummary {
        var totalGave = 0L
        var totalGot = 0L
        var lastDate: Long? = null

        for (tx in transactions) {
            if (lastDate == null || tx.transactionDate > lastDate) {
                lastDate = tx.transactionDate
            }
            when (tx.type) {
                TransactionType.YOU_GAVE -> totalGave += tx.amountMinor
                TransactionType.YOU_GOT -> totalGot += tx.amountMinor
            }
        }

        val netBalance = totalGave - totalGot
        val status = when {
            netBalance > 0 -> LedgerStatus.YOU_WILL_RECEIVE
            netBalance < 0 -> LedgerStatus.YOU_WILL_PAY
            else -> LedgerStatus.SETTLED
        }

        return CustomerLedgerSummary(
            customer = customer,
            totalGaveMinor = totalGave,
            totalGotMinor = totalGot,
            netBalanceMinor = netBalance,
            status = status,
            lastTransactionDate = lastDate
        )
    }

    /**
     * Calculates combined dashboard metrics for all customers.
     */
    fun calculateDashboardSummary(
        customers: List<CustomerEntity>,
        allTransactions: List<TransactionEntity>
    ): DashboardSummary {
        val txMap = allTransactions.groupBy { it.customerId }
        var totalReceivable = 0L
        var totalPayable = 0L

        val summaries = customers.map { customer ->
            val customerTxs = txMap[customer.id] ?: emptyList()
            val summary = calculateCustomerSummary(customer, customerTxs)
            if (summary.netBalanceMinor > 0) {
                totalReceivable += summary.netBalanceMinor
            } else if (summary.netBalanceMinor < 0) {
                totalPayable += kotlin.math.abs(summary.netBalanceMinor)
            }
            summary
        }

        val netBalance = totalReceivable - totalPayable

        return DashboardSummary(
            totalReceivableMinor = totalReceivable,
            totalPayableMinor = totalPayable,
            netBalanceMinor = netBalance,
            customerSummaries = summaries
        )
    }

    /**
     * Calculates report summaries for a given date range.
     */
    fun calculatePeriodReport(
        transactions: List<TransactionEntity>,
        startDate: Long,
        endDate: Long
    ): PeriodReportSummary {
        val filtered = transactions.filter { it.transactionDate in startDate..endDate }
        var creditGiven = 0L
        var paymentReceived = 0L

        for (tx in filtered) {
            when (tx.type) {
                TransactionType.YOU_GAVE -> creditGiven += tx.amountMinor
                TransactionType.YOU_GOT -> paymentReceived += tx.amountMinor
            }
        }

        return PeriodReportSummary(
            totalCreditGivenMinor = creditGiven,
            totalPaymentReceivedMinor = paymentReceived,
            totalCreditTakenMinor = 0L,
            totalPaymentMadeMinor = 0L,
            totalTransactionsCount = filtered.size
        )
    }
}
