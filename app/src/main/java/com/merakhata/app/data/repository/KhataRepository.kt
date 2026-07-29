package com.merakhata.app.data.repository

import com.merakhata.app.data.local.CustomerDao
import com.merakhata.app.data.local.ReminderDao
import com.merakhata.app.data.local.TransactionDao
import com.merakhata.app.data.model.CustomerEntity
import com.merakhata.app.data.model.ReminderEntity
import com.merakhata.app.data.model.TransactionEntity
import com.merakhata.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class KhataRepository(
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao,
    private val reminderDao: ReminderDao,
    val preferences: UserPreferencesRepository
) {
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val activeReminders: Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()

    fun getCustomerById(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerById(id)
    suspend fun getCustomerByIdDirect(id: Long): CustomerEntity? = customerDao.getCustomerByIdDirect(id)

    fun getTransactionsForCustomer(customerId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForCustomer(customerId)

    suspend fun getTransactionsForCustomerList(customerId: Long): List<TransactionEntity> =
        transactionDao.getTransactionsForCustomerList(customerId)

    suspend fun getTransactionById(id: Long): TransactionEntity? = transactionDao.getTransactionById(id)

    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)
    suspend fun deleteCustomerById(id: Long) = customerDao.deleteCustomerById(id)

    suspend fun insertTransaction(transaction: TransactionEntity): Long = transactionDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: TransactionEntity) = transactionDao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: TransactionEntity) = transactionDao.deleteTransaction(transaction)
    suspend fun deleteTransactionById(id: Long) = transactionDao.deleteTransactionById(id)

    suspend fun insertReminder(reminder: ReminderEntity): Long = reminderDao.insertReminder(reminder)
    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: ReminderEntity) = reminderDao.deleteReminder(reminder)
    suspend fun deleteReminderById(id: Long) = reminderDao.deleteReminderById(id)

    suspend fun getAllCustomersList(): List<CustomerEntity> = customerDao.getAllCustomersList()
    suspend fun getAllTransactionsList(): List<TransactionEntity> = transactionDao.getAllTransactionsList()
    suspend fun getActiveRemindersList(): List<ReminderEntity> = reminderDao.getActiveRemindersList()

    suspend fun clearAllLocalData() {
        customerDao.deleteAllCustomers()
        transactionDao.deleteAllTransactions()
        reminderDao.deleteAllReminders()
    }
}
