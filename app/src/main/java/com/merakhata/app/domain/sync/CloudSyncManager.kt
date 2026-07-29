package com.merakhata.app.domain.sync

import com.merakhata.app.data.repository.KhataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val formattedTime: String, val customerCount: Int, val transactionCount: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}

object CloudSyncManager {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    /**
     * Executes cloud database synchronization for customer ledgers, transactions, and reminders.
     */
    suspend fun syncAll(repository: KhataRepository): Boolean = withContext(Dispatchers.IO) {
        try {
            _syncState.value = SyncState.Syncing

            val customers = repository.getAllCustomersList()
            val transactions = repository.getAllTransactionsList()

            // Retrieve active logged-in userId
            val userId = repository.preferences.userId.firstOrNull() ?: "device_guest_user"

            // Push to Vercel/Render Cloud Database
            CloudSyncEngine.pushLocalDataToCloud(repository, userId)

            val now = System.currentTimeMillis()
            val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val formattedTime = formatter.format(Date(now))

            _syncState.value = SyncState.Success(
                formattedTime = formattedTime,
                customerCount = customers.size,
                transactionCount = transactions.size
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _syncState.value = SyncState.Error(e.localizedMessage ?: "Cloud sync error")
            false
        }
    }

    fun resetState() {
        _syncState.value = SyncState.Idle
    }
}
