package com.merakhata.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merakhata.app.data.repository.KhataRepository
import com.merakhata.app.domain.backup.BackupManager
import com.merakhata.app.domain.backup.BackupValidationSummary
import com.merakhata.app.domain.csv.CsvExporter
import com.merakhata.app.domain.security.SecurityManager
import com.merakhata.app.domain.updater.UpdateInfo
import com.merakhata.app.domain.updater.UpdateManager
import com.merakhata.app.domain.updater.UpdateStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

import com.merakhata.app.domain.sync.CloudSyncManager
import com.merakhata.app.domain.sync.SyncState

class SettingsViewModel(private val repository: KhataRepository) : ViewModel() {

    val ownerName: StateFlow<String> = repository.preferences.ownerName
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val businessName: StateFlow<String> = repository.preferences.businessName
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val isAppLockEnabled: StateFlow<Boolean> = repository.preferences.isAppLockEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isBiometricEnabled: StateFlow<Boolean> = repository.preferences.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val themeMode: StateFlow<String> = repository.preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Lazily, "SYSTEM")

    val updateUrl: StateFlow<String> = repository.preferences.updateUrl
        .stateIn(viewModelScope, SharingStarted.Lazily, "https://raw.githubusercontent.com/dk072/merakhata/main/update.json")

    val autoCheckUpdates: StateFlow<Boolean> = repository.preferences.autoCheckUpdates
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val syncState: StateFlow<SyncState> = CloudSyncManager.syncState

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus

    private val _backupSummary = MutableStateFlow<BackupValidationSummary?>(null)
    val backupSummary: StateFlow<BackupValidationSummary?> = _backupSummary

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    fun triggerCloudSync() {
        viewModelScope.launch {
            val success = CloudSyncManager.syncAll(repository)
            if (success) {
                _statusMessage.value = "Cloud Sync completed successfully!"
            } else {
                _statusMessage.value = "Cloud Sync failed. Please check internet connection."
            }
        }
    }

    fun updateProfile(owner: String, business: String) {
        viewModelScope.launch {
            repository.preferences.updateProfile(owner.trim(), business.trim())
            _statusMessage.value = "Profile updated successfully"
        }
    }

    fun setAppLock(enabled: Boolean, pin: String?) {
        viewModelScope.launch {
            val hash = if (pin != null && pin.isNotBlank()) SecurityManager.hashPin(pin) else null
            repository.preferences.setAppLock(enabled, hash)
            _statusMessage.value = if (enabled) "App Lock Enabled" else "App Lock Disabled"
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.preferences.setBiometricEnabled(enabled)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.preferences.setThemeMode(mode)
        }
    }

    fun setUpdateSettings(url: String, autoCheck: Boolean) {
        viewModelScope.launch {
            repository.preferences.setUpdateSettings(url.trim(), autoCheck)
            _statusMessage.value = "Update settings saved"
        }
    }

    fun checkForUpdates(context: Context, customJsonContent: String? = null) {
        viewModelScope.launch {
            _updateStatus.value = UpdateStatus.Checking
            val url = updateUrl.value
            val info = UpdateManager.checkForUpdates(context, url, customJsonContent)
            if (info != null) {
                _updateStatus.value = UpdateStatus.Available(info)
            } else {
                _updateStatus.value = UpdateStatus.NoUpdate
            }
        }
    }

    fun downloadAndInstallUpdate(context: Context, updateInfo: UpdateInfo) {
        viewModelScope.launch {
            _updateStatus.value = UpdateStatus.Downloading(0)
            val apkFile = UpdateManager.downloadApk(context, updateInfo.apkUrl) { progress ->
                _updateStatus.value = UpdateStatus.Downloading(progress)
            }
            if (apkFile != null && apkFile.exists()) {
                _updateStatus.value = UpdateStatus.ReadyToInstall(apkFile)
                UpdateManager.installApk(context, apkFile)
            } else {
                _updateStatus.value = UpdateStatus.Error("Failed to download APK update.")
            }
        }
    }

    fun installLocalApk(context: Context, apkFile: File) {
        if (apkFile.exists()) {
            UpdateManager.installApk(context, apkFile)
        } else {
            _statusMessage.value = "Selected APK file not found."
        }
    }

    fun resetUpdateStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }

    suspend fun generateBackupJson(): String {
        val customers = repository.getAllCustomersList()
        val transactions = repository.getAllTransactionsList()
        val reminders = repository.getActiveRemindersList()
        return BackupManager.createBackupJson(customers, transactions, reminders)
    }

    fun validateImportJson(json: String) {
        _backupSummary.value = BackupManager.validateBackupJson(json)
    }

    fun clearBackupSummary() {
        _backupSummary.value = null
    }

    fun restoreBackup(appDatabase: com.merakhata.app.data.local.AppDatabase, json: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val success = BackupManager.restoreDatabaseFromJson(appDatabase, json)
            if (success) {
                _backupSummary.value = null
                _statusMessage.value = "Data restored successfully!"
                onSuccess()
            } else {
                _statusMessage.value = "Failed to restore backup"
            }
        }
    }

    suspend fun generateCsvString(): String {
        val customers = repository.getAllCustomersList()
        val transactions = repository.getAllTransactionsList()
        return CsvExporter.exportTransactionsToCsv(customers, transactions)
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
