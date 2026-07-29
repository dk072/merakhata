package com.merakhata.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merakhata.app.data.repository.KhataRepository
import com.merakhata.app.domain.security.SecurityManager
import kotlinx.coroutines.flow.*

class SecurityViewModel(private val repository: KhataRepository) : ViewModel() {

    val isAppLockEnabled: StateFlow<Boolean> = repository.preferences.isAppLockEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val storedPinHash: StateFlow<String?> = repository.preferences.pinHash
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isBiometricEnabled: StateFlow<Boolean> = repository.preferences.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked

    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun onPinChar(char: String) {
        if (_pinInput.value.length < 4) {
            _pinInput.value += char
            _error.value = null
            if (_pinInput.value.length == 4) {
                verifyPin()
            }
        }
    }

    fun onBackspace() {
        if (_pinInput.value.isNotEmpty()) {
            _pinInput.value = _pinInput.value.dropLast(1)
            _error.value = null
        }
    }

    fun verifyPin() {
        val hash = storedPinHash.value
        if (hash != null && SecurityManager.verifyPin(_pinInput.value, hash)) {
            _isUnlocked.value = true
        } else {
            _error.value = "Incorrect PIN"
            _pinInput.value = ""
        }
    }

    fun onBiometricSuccess() {
        _isUnlocked.value = true
    }

    fun lockApp() {
        if (isAppLockEnabled.value) {
            _isUnlocked.value = false
            _pinInput.value = ""
        }
    }
}
