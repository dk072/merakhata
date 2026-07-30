package com.merakhata.app.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
    val haptic = LocalHapticFeedback.current
    val amountInput by viewModel.amountInput.collectAsState()
    val type by viewModel.type.collectAsState()
    val description by viewModel.description.collectAsState()
    val transactionDate by viewModel.transactionDate.collectAsState()
    val amountError by viewModel.amountError.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val isGave = type == TransactionType.YOU_GAVE
    val themeColor = if (isGave) ErrorRed else SecondaryTeal

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
                title = { Text("Add Entry", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurfaceDark) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurfaceDark)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = OnSurfaceDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceBg,
                    titleContentColor = OnSurfaceDark,
                    navigationIconContentColor = OnSurfaceDark,
                    actionIconContentColor = OnSurfaceDark
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SurfaceBg)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Transaction Type Segmented Toggle
                Surface(
                    color = SurfaceContainer,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isGave) ErrorContainerPink else Color.Transparent)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.onTypeChange(TransactionType.YOU_GAVE)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "You Gave",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isGave) OnErrorContainerRed else OnSurfaceVariantGray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (!isGave) SecondaryContainerMint else Color.Transparent)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.onTypeChange(TransactionType.YOU_GOT)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "You Got",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (!isGave) OnSecondaryContainerTeal else OnSurfaceVariantGray
                            )
                        }
                    }
                }

                // Amount Input Section Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Amount",
                            fontSize = 12.sp,
                            color = OnSurfaceVariantGray,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceDark,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = amountInput.ifEmpty { "0.00" },
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColor
                            )
                        }

                        if (amountError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(amountError!!, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Detail Inputs Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Description Field
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "What was this for?",
                                fontSize = 12.sp,
                                color = OnSurfaceVariantGray,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = description,
                                onValueChange = { viewModel.onDescriptionChange(it) },
                                placeholder = { Text("Enter description (e.g. Chai, Rent...)", fontSize = 14.sp, color = OutlineGray) },
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = OutlineGray) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceContainerLow.copy(alpha = 0.5f),
                                    unfocusedContainerColor = SurfaceContainerLow.copy(alpha = 0.5f),
                                    focusedBorderColor = PrimaryContainerNavy,
                                    unfocusedBorderColor = OutlineVariantLight.copy(alpha = 0.4f),
                                    focusedTextColor = OnSurfaceDark,
                                    unfocusedTextColor = OnSurfaceDark
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Date Picker Field
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Date",
                                fontSize = 12.sp,
                                color = OnSurfaceVariantGray,
                                fontWeight = FontWeight.Medium
                            )
                            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(transactionDate))
                            OutlinedButton(
                                onClick = { showDatePicker() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.4f)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainerLow.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = OutlineGray, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(dateStr, fontSize = 14.sp, color = OnSurfaceDark, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Attachments Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Attachments",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceDark
                            )
                            Text(
                                text = "Optional",
                                fontSize = 12.sp,
                                color = OutlineGray
                            )
                        }

                        Surface(
                            onClick = {},
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainerLow,
                            border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = OutlineGray, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Add Bill Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceVariantGray)
                            }
                        }
                    }
                }
            }

            // Numeric Keypad & Bottom Save Action Button
            NumericKeypad(
                onKeyClick = { char ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onAmountInput(char)
                },
                onSave = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.saveTransaction(onSuccess = onNavigateBack)
                },
                isSaving = isSaving,
                isGave = isGave
            )
        }
    }
}

@Composable
fun NumericKeypad(
    onKeyClick: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    isGave: Boolean
) {
    Surface(
        color = SurfaceBg,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .padding(vertical = 2.dp)
                                .background(SurfaceContainerHigh, RoundedCornerShape(12.dp))
                                .clickable { onKeyClick(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "BACKSPACE") {
                                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace", tint = OnSurfaceDark)
                            } else {
                                Text(key, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = OnSurfaceDark)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSave,
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Text(
                            text = if (isGave) "Save Giving" else "Save Getting",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
