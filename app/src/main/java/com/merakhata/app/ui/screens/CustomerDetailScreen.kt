package com.merakhata.app.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
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
    val customer by viewModel.customer.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var showDeleteCustDialog by remember { mutableStateOf(false) }
    var selectedTxForDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GreenPrimary)
        }
        return
    }

    val cust = customer!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cust.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Generate PDF Statement") },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                viewModel.generateAndSharePdf(context)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Text Summary") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
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
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onNavigateToEditCustomer(cust.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Customer", color = RedPayable) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RedPayable) },
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
                    .padding(20.dp),
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
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = AccountingEngine.formatCurrency(kotlin.math.abs(summary?.netBalanceMinor ?: 0L)),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = statusStr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // Call / SMS Quick Action Buttons
                    if (!cust.phone.isNull_or_blank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cust.phone}"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call", color = Color.White, fontWeight = FontWeight.Bold)
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Message", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Big Action Buttons: YOU GAVE & YOU GOT with Rich Gradient
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ActionGaveGradient)
                        .clickable { onNavigateToAddTransaction(cust.id, "YOU_GAVE", null) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("YOU GAVE ₹", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ActionGotGradient)
                        .clickable { onNavigateToAddTransaction(cust.id, "YOU_GOT", null) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("YOU GOT ₹", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Transaction History Header
            Text(
                text = "Transaction History",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Timeline List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions recorded yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
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
            title = { Text("Delete Customer Account?") },
            text = { Text("Are you sure you want to delete ${cust.name}? All associated transaction history will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteCustDialog = false
                        viewModel.deleteCustomer(onDeleted = onNavigateBack)
                    }
                ) {
                    Text("Delete", color = RedPayable, fontWeight = FontWeight.Bold)
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
            title = { Text("Delete Transaction?") },
            text = { Text("Are you sure you want to delete this transaction record? Account balance will be recalculated.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(selectedTxForDelete!!)
                        selectedTxForDelete = null
                    }
                ) {
                    Text("Delete", color = RedPayable, fontWeight = FontWeight.Bold)
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
            containerColor = if (isGave) RedLight else GreenLight
        ),
        shape = RoundedCornerShape(14.dp)
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
                        color = if (isGave) RedPayable.copy(alpha = 0.15f) else GreenReceivable.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isGave) "YOU GAVE" else "YOU GOT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGave) RedPayable else GreenReceivable,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$dateStr, $timeStr",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AccountingEngine.formatCurrency(tx.amountMinor),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGave) RedPayable else GreenReceivable
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = RedPayable)
                    }
                }
            }

            if (!tx.description.isNull_or_blank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tx.description!!,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
