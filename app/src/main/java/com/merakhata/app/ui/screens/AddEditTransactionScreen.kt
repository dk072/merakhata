package com.merakhata.app.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merakhata.app.data.model.TransactionType
import com.merakhata.app.ui.theme.*
import com.merakhata.app.ui.viewmodels.AddEditTransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    viewModel: AddEditTransactionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val amountInput by viewModel.amountInput.collectAsState()
    val type by viewModel.type.collectAsState()
    val description by viewModel.description.collectAsState()
    val transactionDate by viewModel.transactionDate.collectAsState()
    val amountError by viewModel.amountError.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val isGave = type == TransactionType.YOU_GAVE
    val themeColor = if (isGave) RedPayable else GreenReceivable

    val calendar = remember { Calendar.getInstance() }

    fun showDatePicker() {
        calendar.timeInMillis = transactionDate
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewModel.onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showTimePicker() {
        calendar.timeInMillis = transactionDate
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                viewModel.onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isGave) "You Gave Money/Goods" else "You Got Payment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Type Selector Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(if (isGave) RedPayable else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { viewModel.onTypeChange(TransactionType.YOU_GAVE) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("YOU GAVE", fontWeight = FontWeight.Bold, color = if (isGave) Color.White else Color.Gray)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(if (!isGave) GreenReceivable else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { viewModel.onTypeChange(TransactionType.YOU_GOT) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("YOU GOT", fontWeight = FontWeight.Bold, color = if (!isGave) Color.White else Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Display Amount
                Text("Enter Amount", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = "₹${amountInput.ifEmpty { "0" }}",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )

                if (amountError != null) {
                    Text(amountError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Date & Time Selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(transactionDate))
                    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(transactionDate))

                    OutlinedButton(
                        onClick = { showDatePicker() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(dateStr, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showTimePicker() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(timeStr, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    label = { Text("Description / Items / Notes") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Custom Keypad Component
            NumericKeypad(
                onKeyClick = { char -> viewModel.onAmountInput(char) },
                onSave = { viewModel.saveTransaction(onSuccess = onNavigateBack) },
                isSaving = isSaving,
                themeColor = themeColor
            )
        }
    }
}

@Composable
fun NumericKeypad(
    onKeyClick: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    themeColor: Color
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf(".", "0", "BACKSPACE")
            )

            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .padding(2.dp)
                                .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .clickable { onKeyClick(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "BACKSPACE") {
                                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace", tint = Color.DarkGray)
                            } else {
                                Text(key, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSave,
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("SAVE TRANSACTION", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
