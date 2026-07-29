package com.merakhata.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merakhata.app.data.repository.KhataRepository
import com.merakhata.app.domain.sync.CloudSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

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

        if (trimmedPassword.length < 4) {
            authState.value = AuthState.Error("Password must be at least 4 characters.")
            return
        }

        viewModelScope.launch {
            authState.value = AuthState.Loading
            
            // Generate deterministic or cloud-based user ID linked to email
            val generatedId = "user_" + UUID.nameUUIDFromBytes(trimmedEmail.lowercase().toByteArray()).toString().take(12)
            
            repository.preferences.setCloudUserLogin(generatedId, trimmedEmail)
            
            // Trigger automatic Cloud Sync for logged-in user account
            CloudSyncManager.triggerSync(repository)

            authState.value = AuthState.Success(trimmedEmail)
            onSuccess()
        }
    }

    fun register(emailInput: String, passwordInput: String, ownerName: String, businessName: String, onSuccess: () -> Unit) {
        val trimmedEmail = emailInput.trim()
        val trimmedPassword = passwordInput.trim()

        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            authState.value = AuthState.Error("Please enter a valid email address.")
            return
        }

        if (trimmedPassword.length < 4) {
            authState.value = AuthState.Error("Password must be at least 4 characters.")
            return
        }

        viewModelScope.launch {
            authState.value = AuthState.Loading
            
            val generatedId = "user_" + UUID.nameUUIDFromBytes(trimmedEmail.lowercase().toByteArray()).toString().take(12)
            
            repository.preferences.setCloudUserLogin(generatedId, trimmedEmail)
            if (ownerName.isNotBlank()) {
                repository.preferences.updateProfile(ownerName.trim(), businessName.trim())
            }

            CloudSyncManager.triggerSync(repository)

            authState.value = AuthState.Success(trimmedEmail)
            onSuccess()
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
