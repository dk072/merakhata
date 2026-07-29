package com.merakhata.app.data.local

import androidx.room.*
import com.merakhata.app.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC, id DESC")
    suspend fun getAllTransactionsList(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY transactionDate DESC, id DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY transactionDate DESC, id DESC")
    suspend fun getTransactionsForCustomerList(customerId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
