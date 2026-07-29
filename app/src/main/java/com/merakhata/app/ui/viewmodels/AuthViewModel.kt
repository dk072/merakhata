package com.merakhata.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merakhata.app.data.repository.KhataRepository
import com.merakhata.app.domain.auth.AuthResult
import com.merakhata.app.domain.auth.FirebaseAuthService
import com.merakhata.app.domain.sync.CloudSyncManager
import com.merakhata.app.domain.sync.FirebaseRealtimeSyncEngine
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

        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            authState.value = AuthState.Error("Please enter a valid email address.")
            return
        }

        if (trimmedPassword.length < 6) {
            authState.value = AuthState.Error("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            authState.value = AuthState.Loading

            // Real-Time Online Authentication with Firebase Auth
            when (val result = FirebaseAuthService.signIn(trimmedEmail, trimmedPassword)) {
                is AuthResult.Success -> {
                    repository.preferences.setCloudUserLogin(result.userId, result.email)

                    // Fetch existing cloud data for this user account & restore to local database
                    FirebaseRealtimeSyncEngine.fetchCloudDataAndRestore(repository, result.userId)
                    // Push latest state to cloud
                    FirebaseRealtimeSyncEngine.pushLocalDataToCloud(repository, result.userId)
                    CloudSyncManager.syncAll(repository)

                    authState.value = AuthState.Success(result.email)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    fun register(emailInput: String, passwordInput: String, ownerName: String, businessName: String, onSuccess: () -> Unit) {
        val trimmedEmail = emailInput.trim()
        val trimmedPassword = passwordInput.trim()

        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            authState.value = AuthState.Error("Please enter a valid email address.")
            return
        }

        if (trimmedPassword.length < 6) {
            authState.value = AuthState.Error("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            authState.value = AuthState.Loading

            // Real-Time Online Registration with Firebase Auth
            when (val result = FirebaseAuthService.signUp(trimmedEmail, trimmedPassword)) {
                is AuthResult.Success -> {
                    repository.preferences.setCloudUserLogin(result.userId, result.email)
                    if (ownerName.isNotBlank()) {
                        repository.preferences.updateProfile(ownerName.trim(), businessName.trim())
                    }

                    FirebaseRealtimeSyncEngine.pushLocalDataToCloud(repository, result.userId)
                    CloudSyncManager.syncAll(repository)

                    authState.value = AuthState.Success(result.email)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    authState.value = AuthState.Error(result.message)
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            repository.preferences.logoutCloudUser()
            authState.value = AuthState.Idle
            onLoggedOut()
        }
    }

    fun clearState() {
        authState.value = AuthState.Idle
    }
}
