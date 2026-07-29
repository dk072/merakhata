package com.merakhata.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merakhata.app.data.repository.KhataRepository
import com.merakhata.app.domain.auth.CloudAuthResult
import com.merakhata.app.domain.auth.CloudAuthService
import com.merakhata.app.domain.sync.CloudSyncEngine
import com.merakhata.app.domain.sync.CloudSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: KhataRepository) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = repository.preferences.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val userEmail: StateFlow<String?> = repository.preferences.userEmail
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val userId: StateFlow<String?> = repository.preferences.userId
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val authState = MutableStateFlow<AuthState>(AuthState.Idle)

    fun login(emailInput: String, passwordInput: String, onSuccess: () -> Unit) {
        val trimmedEmail = emailInput.trim()
        val trimmedPassword = passwordInput.trim()

        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            authState.value = AuthState.Error("Please enter a valid email address.")
            return
        }

        if (trimmedPassword.isEmpty()) {
            authState.value = AuthState.Error("Please enter your password.")
            return
        }

        viewModelScope.launch {
            authState.value = AuthState.Loading

            when (val result = CloudAuthService.signIn(trimmedEmail, trimmedPassword)) {
                is CloudAuthResult.Success -> {
                    // Wipe any local data leftover from previous user session for data isolation
                    repository.clearAllLocalData()

                    repository.preferences.setCloudUserLogin(result.userId, result.email, result.token)
                    if (!result.ownerName.isNullOrEmpty()) {
                        repository.preferences.updateProfile(result.ownerName, result.businessName ?: "")
                    }

                    // Fetch existing cloud data for this user account & restore to local database
                    CloudSyncEngine.fetchCloudDataAndRestore(repository, result.userId)
                    CloudSyncEngine.pushLocalDataToCloud(repository, result.userId)
                    CloudSyncManager.syncAll(repository)

                    authState.value = AuthState.Success(result.email)
                    onSuccess()
                }
                is CloudAuthResult.Error -> {
                    authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    fun register(emailInput: String, passwordInput: String, ownerName: String, businessName: String, onSuccess: () -> Unit) {
        val trimmedEmail = emailInput.trim()
        val trimmedPassword = passwordInput.trim()

        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            authState.value = AuthState.Error("Please enter a valid email address.")
            return
        }

        if (trimmedPassword.length < 4) {
            authState.value = AuthState.Error("Password must be at least 4 characters.")
            return
        }

        viewModelScope.launch {
            authState.value = AuthState.Loading

            when (val result = CloudAuthService.signUp(trimmedEmail, trimmedPassword, ownerName, businessName)) {
                is CloudAuthResult.Success -> {
                    // Wipe local data for clean isolate workspace
                    repository.clearAllLocalData()

                    repository.preferences.setCloudUserLogin(result.userId, result.email, result.token)
                    if (ownerName.isNotBlank()) {
                        repository.preferences.updateProfile(ownerName.trim(), businessName.trim())
                    }

                    CloudSyncEngine.pushLocalDataToCloud(repository, result.userId)
                    CloudSyncManager.syncAll(repository)

                    authState.value = AuthState.Success(result.email)
                    onSuccess()
                }
                is CloudAuthResult.Error -> {
                    authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            // Clear local Room database tables to preserve data isolation
            repository.clearAllLocalData()
            // Clear preferences & session token
            repository.preferences.logoutCloudUser()
            authState.value = AuthState.Idle
            onLoggedOut()
        }
    }

    fun clearState() {
        authState.value = AuthState.Idle
    }
}
