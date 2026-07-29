package com.merakhata.app.domain.csv

import com.merakhata.app.data.model.CustomerEntity
import com.merakhata.app.data.model.TransactionEntity
import com.merakhata.app.data.model.TransactionType
import com.merakhata.app.domain.accounting.AccountingEngine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun exportTransactionsToCsv(
        customers: List<CustomerEntity>,
        transactions: List<TransactionEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("Customer,Phone,Date,Time,Transaction Type,Amount,Description,Running Balance\n")

        val customerMap = customers.associateBy { it.id }
        val groupedTxs = transactions.groupBy { it.customerId }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        for (customer in customers) {
            val customerTxs = (groupedTxs[customer.id] ?: emptyList()).sortedBy { it.transactionDate }
            var runningBal = 0L

            for (tx in customerTxs) {
                if (tx.type == TransactionType.YOU_GAVE) {
                    runningBal += tx.amountMinor
                } else {
                    runningBal -= tx.amountMinor
                }

                val dateStr = dateFormat.format(Date(tx.transactionDate))
                val timeStr = timeFormat.format(Date(tx.transactionDate))
                val typeStr = if (tx.type == TransactionType.YOU_GAVE) "YOU GAVE" else "YOU GOT"
                val amountStr = (tx.amountMinor / 100.0).toString()
                val descClean = (tx.description ?: "").replace(",", " ")
                val balStr = (runningBal / 100.0).toString()
                val custNameClean = customer.name.replace(",", " ")
                val phoneClean = customer.phone ?: ""

                sb.append("\"$custNameClean\",\"$phoneClean\",\"$dateStr\",\"$timeStr\",\"$typeStr\",$amountStr,\"$descClean\",$balStr\n")
            }
        }

        return sb.toString()
    }
}
