package com.merakhata.app.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merakhata.app.domain.accounting.AccountingEngine
import com.merakhata.app.ui.theme.GreenPrimary
import com.merakhata.app.ui.theme.HeaderGradientStart
import com.merakhata.app.ui.theme.RedPayable
import com.merakhata.app.ui.viewmodels.ReminderItem
import com.merakhata.app.ui.viewmodels.RemindersViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: RemindersViewModel) {
    val context = LocalContext.current
    val reminderItems by viewModel.reminderItems.collectAsState()
    val customers by viewModel.customers.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Reminders", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderGradientStart,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GreenPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            if (reminderItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No payment reminders scheduled.\nTap '+' to add a reminder.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reminderItems, key = { it.reminder.id }) { item ->
                        ReminderCard(
                            item = item,
                            onDelete = { viewModel.deleteReminder(context, item) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            customers = customers,
            onDismiss = { showAddDialog = false },
            onConfirm = { custId, amtMinor, timeMillis, notes ->
                viewModel.createReminder(context, custId, amtMinor, timeMillis, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ReminderCard(
    item: ReminderItem,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(item.reminder.reminderDateTime))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Alarm, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(32.dp))

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.customer?.name ?: "Customer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Amount: ${AccountingEngine.formatCurrency(item.reminder.amountMinor)}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = dateStr,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedPayable)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    customers: List<com.merakhata.app.data.model.CustomerEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long, Long, String?) -> Unit
) {
    val context = LocalContext.current
    var selectedCustId by remember(customers) { mutableStateOf(customers.firstOrNull()?.id ?: 0L) }
    var amountInput by remember { mutableStateOf("") }
    var dateTimeMillis by remember { mutableStateOf(System.currentTimeMillis() + (24 * 60 * 60 * 1000L)) }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(dateTimeMillis))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Payment Reminder", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Customer Selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    val selectedName = customers.find { it.id == selectedCustId }?.name ?: "Select Customer"
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Customer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        customers.forEach { cust ->
                            DropdownMenuItem(
                                text = { Text(cust.name) },
                                onClick = {
                                    selectedCustId = cust.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        calendar.timeInMillis = dateTimeMillis
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                calendar.set(Calendar.YEAR, y)
                                calendar.set(Calendar.MONTH, m)
                                calendar.set(Calendar.DAY_OF_MONTH, d)
                                TimePickerDialog(
                                    context,
                                    { _, hr, min ->
                                        calendar.set(Calendar.HOUR_OF_DAY, hr)
                                        calendar.set(Calendar.MINUTE, min)
                                        dateTimeMillis = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reminder Time: $dateStr", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amtMinor = AccountingEngine.parseToMinorUnits(amountInput)
                    if (selectedCustId > 0 && amtMinor > 0) {
                        onConfirm(selectedCustId, amtMinor, dateTimeMillis, notes)
                    }
                }
            ) {
                Text("Schedule", fontWeight = FontWeight.Bold, color = GreenPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
