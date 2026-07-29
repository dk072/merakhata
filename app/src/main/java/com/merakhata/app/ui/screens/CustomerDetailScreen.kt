package com.merakhata.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merakhata.app.data.model.TransactionEntity
import com.merakhata.app.data.model.TransactionType
import com.merakhata.app.domain.accounting.AccountingEngine
import com.merakhata.app.domain.accounting.LedgerStatus
import com.merakhata.app.ui.theme.*
import com.merakhata.app.ui.viewmodels.CustomerDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    viewModel: CustomerDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditCustomer: (Long) -> Unit,
    onNavigateToAddTransaction: (Long, String, Long?) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val customer by viewModel.customer.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var showDeleteCustDialog by remember { mutableStateOf(false) }
    var selectedTxForDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DeepEmerald)
        }
        return
    }

    val cust = customer!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = cust.name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Generate PDF Statement") },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = DeepEmerald) },
                            onClick = {
                                showMenu = false
                                viewModel.generateAndSharePdf(context)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Text Summary") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = DeepEmerald) },
                            onClick = {
                                showMenu = false
                                val text = viewModel.generateShareSummaryText()
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Ledger Summary"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Customer Profile") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = DeepEmerald) },
                            onClick = {
                                showMenu = false
                                onNavigateToEditCustomer(cust.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Customer", color = PayableRed, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = PayableRed) },
                            onClick = {
                                showMenu = false
                                showDeleteCustDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderGradientStart,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Sticky Bottom Action Buttons: YOU GAVE & YOU GOT
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 10.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ActionGaveGradient)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToAddTransaction(cust.id, "YOU_GAVE", null)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("YOU GAVE ₹", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 0.5.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ActionGotGradient)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToAddTransaction(cust.id, "YOU_GOT", null)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("YOU GOT ₹", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Balance Card with Rich Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryHeaderGradient)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val statusStr = when (summary?.status) {
                        LedgerStatus.YOU_WILL_RECEIVE -> "YOU WILL RECEIVE"
                        LedgerStatus.YOU_WILL_PAY -> "YOU WILL PAY"
                        else -> "SETTLED"
                    }
                    Text(
                        text = "Current Account Balance",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = AccountingEngine.formatCurrency(kotlin.math.abs(summary?.netBalanceMinor ?: 0L)),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = statusStr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }

                    // Call / SMS / WhatsApp Quick Action Buttons
                    if (!cust.phone.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cust.phone}"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.22f)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    val amtStr = AccountingEngine.formatCurrency(kotlin.math.abs(summary?.netBalanceMinor ?: 0L))
                                    val msg = "Namaste ${cust.name}, aapke khate me $amtStr pending hai. Payment kar dena."
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${cust.phone}")).apply {
                                        putExtra("sms_body", msg)
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.22f)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SMS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transaction History Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction History",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${transactions.size} records",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Timeline List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = DeepEmerald.copy(alpha = 0.4f),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No transactions recorded yet.\nTap 'YOU GAVE ₹' or 'YOU GOT ₹' below to add.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionItemCard(
                            tx = tx,
                            onEdit = { onNavigateToAddTransaction(cust.id, tx.type.name, tx.id) },
                            onDelete = { selectedTxForDelete = tx }
                        )
                    }
                }
            }
        }
    }

    // Delete Modals
    if (showDeleteCustDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCustDialog = false },
            title = { Text("Delete Customer Account?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${cust.name}? All associated transaction history will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteCustDialog = false
                        viewModel.deleteCustomer(onDeleted = onNavigateBack)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PayableRed)
                ) {
                    Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCustDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (selectedTxForDelete != null) {
        AlertDialog(
            onDismissRequest = { selectedTxForDelete = null },
            title = { Text("Delete Transaction?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this transaction record? Account balance will be recalculated.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(selectedTxForDelete!!)
                        selectedTxForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PayableRed)
                ) {
                    Text("Delete Record", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTxForDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionItemCard(
    tx: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isGave = tx.type == TransactionType.YOU_GAVE
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(tx.transactionDate))
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(tx.transactionDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGave) LightPayableBg else LightReceivableBg
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isGave) PayableRed.copy(alpha = 0.2f) else ReceivableGreen.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Surface(
                        color = if (isGave) PayableRed.copy(alpha = 0.15f) else ReceivableGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isGave) "YOU GAVE (DEBIT)" else "YOU GOT (CREDIT)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isGave) PayableRed else ReceivableGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$dateStr, $timeStr",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AccountingEngine.formatCurrency(tx.amountMinor),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isGave) PayableRed else ReceivableGreen
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = PayableRed)
                    }
                }
            }

            if (!tx.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = (if (isGave) PayableRed else ReceivableGreen).copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tx.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
