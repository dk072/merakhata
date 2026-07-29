package com.merakhata.app

import com.merakhata.app.data.model.CustomerEntity
import com.merakhata.app.data.model.TransactionEntity
import com.merakhata.app.data.model.TransactionType
import com.merakhata.app.domain.accounting.AccountingEngine
import com.merakhata.app.domain.accounting.LedgerStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountingEngineTest {

    @Test
    fun testParseToMinorUnits() {
        assertEquals(50000L, AccountingEngine.parseToMinorUnits("500"))
        assertEquals(50050L, AccountingEngine.parseToMinorUnits("500.5"))
        assertEquals(50050L, AccountingEngine.parseToMinorUnits("500.50"))
        assertEquals(125000L, AccountingEngine.parseToMinorUnits("1,250"))
        assertEquals(0L, AccountingEngine.parseToMinorUnits(""))
    }

    @Test
    fun testFormatCurrency() {
        assertEquals("₹500", AccountingEngine.formatCurrency(50000L))
        assertEquals("₹500.50", AccountingEngine.formatCurrency(50050L))
        assertEquals("₹1,250", AccountingEngine.formatCurrency(125000L))
        assertEquals("-₹200", AccountingEngine.formatCurrency(-20000L))
    }

    @Test
    fun testCustomerLedgerCalculations() {
        val customer = CustomerEntity(id = 1, name = "Ramesh")
        
        // Scenario 1: YOU GAVE ₹500 -> Balance = ₹500 receivable
        val tx1 = TransactionEntity(id = 1, customerId = 1, amountMinor = 50000L, type = TransactionType.YOU_GAVE)
        var summary = AccountingEngine.calculateCustomerSummary(customer, listOf(tx1))
        
        assertEquals(50000L, summary.netBalanceMinor)
        assertEquals(LedgerStatus.YOU_WILL_RECEIVE, summary.status)
        assertEquals(50000L, summary.totalGaveMinor)
        assertEquals(0L, summary.totalGotMinor)

        // Scenario 2: YOU GOT ₹200 -> Remaining Balance = ₹300 receivable
        val tx2 = TransactionEntity(id = 2, customerId = 1, amountMinor = 20000L, type = TransactionType.YOU_GOT)
        summary = AccountingEngine.calculateCustomerSummary(customer, listOf(tx1, tx2))
        
        assertEquals(30000L, summary.netBalanceMinor)
        assertEquals(LedgerStatus.YOU_WILL_RECEIVE, summary.status)
        assertEquals(50000L, summary.totalGaveMinor)
        assertEquals(20000L, summary.totalGotMinor)

        // Scenario 3: YOU GOT another ₹300 -> Remaining Balance = 0 / Settled
        val tx3 = TransactionEntity(id = 3, customerId = 1, amountMinor = 30000L, type = TransactionType.YOU_GOT)
        summary = AccountingEngine.calculateCustomerSummary(customer, listOf(tx1, tx2, tx3))
        
        assertEquals(0L, summary.netBalanceMinor)
        assertEquals(LedgerStatus.SETTLED, summary.status)

        // Scenario 4: YOU GOT another ₹200 -> Balance = -₹200 (YOU WILL PAY / User owes customer)
        val tx4 = TransactionEntity(id = 4, customerId = 1, amountMinor = 20000L, type = TransactionType.YOU_GOT)
        summary = AccountingEngine.calculateCustomerSummary(customer, listOf(tx1, tx2, tx3, tx4))

        assertEquals(-20000L, summary.netBalanceMinor)
        assertEquals(LedgerStatus.YOU_WILL_PAY, summary.status)
    }

    @Test
    fun testDashboardSummaryCalculations() {
        val c1 = CustomerEntity(id = 1, name = "Ramesh") // owes 1500
        val c2 = CustomerEntity(id = 2, name = "Amit")   // owes 850
        val c3 = CustomerEntity(id = 3, name = "Sharma") // user owes 2000

        val txs = listOf(
            TransactionEntity(id = 1, customerId = 1, amountMinor = 150000L, type = TransactionType.YOU_GAVE),
            TransactionEntity(id = 2, customerId = 2, amountMinor = 85000L, type = TransactionType.YOU_GAVE),
            TransactionEntity(id = 3, customerId = 3, amountMinor = 200000L, type = TransactionType.YOU_GOT)
        )

        val dash = AccountingEngine.calculateDashboardSummary(listOf(c1, c2, c3), txs)

        // Total Receivable = 1500 + 850 = 2350
        assertEquals(235000L, dash.totalReceivableMinor)
        // Total Payable = 2000
        assertEquals(200000L, dash.totalPayableMinor)
        // Net = 2350 - 2000 = 350
        assertEquals(35000L, dash.netBalanceMinor)
    }
}
