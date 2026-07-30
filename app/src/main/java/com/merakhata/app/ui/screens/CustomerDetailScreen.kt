package com.merakhata.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
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

    var selectedFilter by remember { mutableStateOf("ALL") }
    var showDeleteCustDialog by remember { mutableStateOf(false) }
    var selectedTxForDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SecondaryTeal)
        }
        return
    }

    val cust = customer!!

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "GAVE" -> transactions.filter { it.type == TransactionType.YOU_GAVE }
            "GOT" -> transactions.filter { it.type == TransactionType.YOU_GOT }
            else -> transactions
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = cust.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = OnSurfaceDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurfaceDark)
                    }
                },
                actions = {
                    if (!cust.phone.isNullOrEmpty()) {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cust.phone}"))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = OnSurfaceDark)
                        }
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = OnSurfaceDark)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Generate PDF Statement") },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = SecondaryTeal) },
                            onClick = {
                                showMenu = false
                                viewModel.generateAndSharePdf(context)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Text Summary") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = SecondaryTeal) },
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
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = SecondaryTeal) },
                            onClick = {
                                showMenu = false
                                onNavigateToEditCustomer(cust.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Customer", color = ErrorRed, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
                            onClick = {
                                showMenu = false
                                showDeleteCustDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceBg,
                    titleContentColor = OnSurfaceDark,
                    navigationIconContentColor = OnSurfaceDark,
                    actionIconContentColor = OnSurfaceDark
                )
            )
        },
        bottomBar = {
            // Sticky Bottom Action Buttons: YOU GAVE & YOU GOT
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = SurfaceBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToAddTransaction(cust.id, "YOU_GAVE", null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.RemoveCircle, contentDescription = null, tint = Color.White)
                            Text("You Gave", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToAddTransaction(cust.id, "YOU_GOT", null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
                            Text("You Got", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SurfaceBg)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer Contact Header Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar Badge
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryContainerNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cust.name.take(2).uppercase(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = cust.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceDark
                            )

                            if (!cust.phone.isNullOrEmpty()) {
                                Text(
                                    text = cust.phone,
                                    fontSize = 13.sp,
                                    color = OutlineGray
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Net Balance Breakdown Card
                            val (badgeBg, badgeText, amtColor, badgeLabel) = when (summary?.status) {
                                LedgerStatus.YOU_WILL_RECEIVE -> Tuple4(SecondaryContainerMint, OnSecondaryContainerTeal, SecondaryTeal, "You'll Get")
                                LedgerStatus.YOU_WILL_PAY -> Tuple4(ErrorContainerPink, OnErrorContainerRed, ErrorRed, "You'll Give")
                                else -> Tuple4(SurfaceContainerHigh, OnSurfaceDark, OutlineGray, "Settled")
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "NET BALANCE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OutlineGray,
                                            letterSpacing = 0.8.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = AccountingEngine.formatCurrency(kotlin.math.abs(summary?.netBalanceMinor ?: 0L)),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = amtColor
                                        )
                                    }

                                    Surface(
                                        color = badgeBg,
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = badgeLabel,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeText,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Filter Chips (All, Gave, Got)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALL" to "All", "GAVE" to "Gave", "GOT" to "Got").forEach { (filterKey, label) ->
                            val isSelected = selectedFilter == filterKey
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedFilter = filterKey
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) PrimaryContainerNavy else SurfaceContainerHigh,
                                    contentColor = if (isSelected) Color.White else OnSurfaceVariantGray
                                ),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Transaction List
                if (filteredTransactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = OutlineGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No transactions recorded yet.\nTap 'You Gave' or 'You Got' below to add.",
                                    color = OnSurfaceVariantGray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionRowItem(
                            tx = tx,
                            onEdit = { onNavigateToAddTransaction(cust.id, tx.type.name, tx.id) },
                            onDelete = { selectedTxForDelete = tx }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Delete Confirmation Modals
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
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
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
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
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

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun TransactionRowItem(
    tx: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isGave = tx.type == TransactionType.YOU_GAVE
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.transactionDate))
    val accentColor = if (isGave) ErrorRed else SecondaryTeal
    val iconBg = if (isGave) ErrorContainerPink else SecondaryContainerMint
    val iconColor = if (isGave) ErrorRed else SecondaryTeal

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left Accent Bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isGave) Icons.AutoMirrored.Filled.CallMade else Icons.AutoMirrored.Filled.CallReceived,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = tx.description?.takeIf { it.isNotBlank() } ?: if (isGave) "Material / Cash Given" else "Payment Received",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dateStr,
                            fontSize = 12.sp,
                            color = OutlineGray
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = AccountingEngine.formatCurrency(tx.amountMinor),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                    Text(
                        text = if (isGave) "You Gave" else "You Got",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
        }
    }
}
