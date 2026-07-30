package com.merakhata.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merakhata.app.data.model.CustomerEntity
import com.merakhata.app.data.repository.KhataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddEditCustomerViewModel(
    private val repository: KhataRepository,
    private val customerId: Long?
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes

    private val _photoUri = MutableStateFlow<String?>(null)
    val photoUri: StateFlow<String?> = _photoUri

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError

    private var existingCustomer: CustomerEntity? = null

    init {
        if (customerId != null && customerId > 0) {
            viewModelScope.launch {
                val cust = repository.getCustomerByIdDirect(customerId)
                if (cust != null) {
                    existingCustomer = cust
                    _name.value = cust.name
                    _phone.value = cust.phone ?: ""
                    _address.value = cust.address ?: ""
                    _notes.value = cust.notes ?: ""
                    _photoUri.value = cust.photoUri
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _name.value = value
        if (value.trim().isNotEmpty()) {
            _nameError.value = null
        }
    }

    fun onPhoneChange(value: String) {
        _phone.value = value
    }

    fun onAddressChange(value: String) {
        _address.value = value
    }

    fun onNotesChange(value: String) {
        _notes.value = value
    }

    fun onPhotoSelected(uri: String?) {
        _photoUri.value = uri
    }

    fun saveCustomer(onSuccess: () -> Unit) {
        if (_name.value.trim().isEmpty()) {
            _nameError.value = "Customer name is required"
            return
        }

        viewModelScope.launch {
            val entity = CustomerEntity(
                id = existingCustomer?.id ?: 0,
                name = _name.value.trim(),
                phone = _phone.value.trim().takeIf { it.isNotEmpty() },
                address = _address.value.trim().takeIf { it.isNotEmpty() },
                notes = _notes.value.trim().takeIf { it.isNotEmpty() },
                photoUri = _photoUri.value,
                createdAt = existingCustomer?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertCustomer(entity)
            com.merakhata.app.domain.sync.CloudSyncManager.syncAll(repository)
            onSuccess()
        }
    }
}
