package com.merakhata.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merakhata.app.data.repository.KhataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(private val repository: KhataRepository) : ViewModel() {

    private val _ownerName = MutableStateFlow("")
    val ownerName: StateFlow<String> = _ownerName

    private val _businessName = MutableStateFlow("")
    val businessName: StateFlow<String> = _businessName

    fun onOwnerNameChange(value: String) {
        _ownerName.value = value
    }

    fun onBusinessNameChange(value: String) {
        _businessName.value = value
    }

    fun saveOnboarding(onSuccess: () -> Unit) {
        if (_ownerName.value.trim().isEmpty()) return
        viewModelScope.launch {
            repository.preferences.setOnboardingCompleted(
                completed = true,
                owner = _ownerName.value.trim(),
                business = _businessName.value.trim()
            )
            onSuccess()
        }
    }
}
