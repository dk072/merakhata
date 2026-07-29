package com.merakhata.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merakhata.app.data.model.TransactionEntity
import com.merakhata.app.data.model.TransactionType
import com.merakhata.app.data.repository.KhataRepository
import com.merakhata.app.domain.accounting.AccountingEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddEditTransactionViewModel(
    private val repository: KhataRepository,
    private val customerId: Long,
    initialTypeStr: String?,
    private val transactionId: Long?
) : ViewModel() {

    private val _amountInput = MutableStateFlow("")
    val amountInput: StateFlow<String> = _amountInput

    private val _type = MutableStateFlow(
        if (initialTypeStr == "YOU_GOT") TransactionType.YOU_GOT else TransactionType.YOU_GAVE
    )
    val type: StateFlow<TransactionType> = _type

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _attachmentUri = MutableStateFlow<String?>(null)
    val attachmentUri: StateFlow<String?> = _attachmentUri

    private val _transactionDate = MutableStateFlow(System.currentTimeMillis())
    val transactionDate: StateFlow<Long> = _transactionDate

    private val _amountError = MutableStateFlow<String?>(null)
    val amountError: StateFlow<String?> = _amountError

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private var existingTx: TransactionEntity? = null

    init {
        if (transactionId != null && transactionId > 0) {
            viewModelScope.launch {
                val tx = repository.getTransactionById(transactionId)
                if (tx != null) {
                    existingTx = tx
                    _amountInput.value = (tx.amountMinor / 100.0).let {
                        if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
                    }
                    _type.value = tx.type
                    _description.value = tx.description ?: ""
                    _attachmentUri.value = tx.attachmentUri
                    _transactionDate.value = tx.transactionDate
                }
            }
        }
    }

    fun onAmountInput(char: String) {
        if (char == "BACKSPACE") {
            if (_amountInput.value.isNotEmpty()) {
                _amountInput.value = _amountInput.value.dropLast(1)
            }
        } else if (char == ".") {
            if (!_amountInput.value.contains(".")) {
                _amountInput.value = if (_amountInput.value.isEmpty()) "0." else _amountInput.value + "."
            }
        } else {
            // Prevent multiple decimals or overflow length
            if (_amountInput.value.contains(".")) {
                val decimals = _amountInput.value.substringAfter(".")
                if (decimals.length >= 2) return
            }
            _amountInput.value += char
        }
        _amountError.value = null
    }

    fun onTypeChange(newType: TransactionType) {
        _type.value = newType
    }

    fun onDescriptionChange(value: String) {
        _description.value = value
    }

    fun onAttachmentSelected(uri: String?) {
        _attachmentUri.value = uri
    }

    fun onDateSelected(timestampMillis: Long) {
        _transactionDate.value = timestampMillis
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        if (_isSaving.value) return // Prevent double tap save

        val minorUnits = AccountingEngine.parseToMinorUnits(_amountInput.value)
        if (minorUnits <= 0L) {
            _amountError.value = "Amount must be greater than zero"
            return
        }

        _isSaving.value = true

        viewModelScope.launch {
            try {
                val entity = TransactionEntity(
                    id = existingTx?.id ?: 0,
                    customerId = customerId,
                    amountMinor = minorUnits,
                    type = _type.value,
                    description = _description.value.trim().takeIf { it.isNotEmpty() },
                    attachmentUri = _attachmentUri.value,
                    transactionDate = _transactionDate.value,
                    createdAt = existingTx?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.insertTransaction(entity)
                onSuccess()
            } finally {
                _isSaving.value = false
            }
        }
    }
}
