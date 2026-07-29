package com.merakhata.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.merakhata.app.data.model.CustomerEntity
import com.merakhata.app.data.model.ReminderEntity
import com.merakhata.app.data.repository.KhataRepository
import com.merakhata.app.domain.notification.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReminderItem(
    val reminder: ReminderEntity,
    val customer: CustomerEntity?
)

class RemindersViewModel(private val repository: KhataRepository) : ViewModel() {

    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val reminderItems: StateFlow<List<ReminderItem>> = combine(
        repository.activeReminders,
        repository.allCustomers
    ) { reminders, custs ->
        val custMap = custs.associateBy { it.id }
        reminders.map { r ->
            ReminderItem(
                reminder = r,
                customer = custMap[r.customerId]
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createReminder(
        context: Context,
        customerId: Long,
        amountMinor: Long,
        dateTimeMillis: Long,
        notes: String?
    ) {
        viewModelScope.launch {
            val cust = repository.getCustomerByIdDirect(customerId) ?: return@launch
            val reminder = ReminderEntity(
                customerId = customerId,
                amountMinor = amountMinor,
                reminderDateTime = dateTimeMillis,
                notes = notes,
                enabled = true
            )
            val id = repository.insertReminder(reminder)
            val savedReminder = reminder.copy(id = id)

            ReminderScheduler.scheduleReminder(context, savedReminder, cust.name)
        }
    }

    fun deleteReminder(context: Context, reminderItem: ReminderItem) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(context, reminderItem.reminder.id)
            repository.deleteReminder(reminderItem.reminder)
        }
    }
}
