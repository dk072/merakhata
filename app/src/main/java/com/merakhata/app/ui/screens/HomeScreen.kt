package com.merakhata.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merakhata.app.domain.accounting.AccountingEngine
import com.merakhata.app.domain.accounting.CustomerLedgerSummary
import com.merakhata.app.domain.accounting.LedgerStatus
import com.merakhata.app.ui.theme.*
import com.merakhata.app.ui.viewmodels.CustomerFilter
import com.merakhata.app.ui.viewmodels.CustomerSort
import com.merakhata.app.ui.viewmodels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCustomer: (Long) -> Unit,
    onNavigateToAddCustomer: () -> Unit
) {
    val dashboardSummary by viewModel.dashboardSummary.collectAsState()
    val customers by viewModel.filteredCustomers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val ownerName by viewModel.ownerName.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddCustomer,
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                text = { Text("+ ADD CUSTOMER", fontWeight = FontWeight.Bold, color = Color.White) },
                containerColor = GreenReceivable,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Bar with Rich Multi-Color Mesh Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(PrimaryHeaderGradient)
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (businessName.isNotBlank()) businessName else "MERA KHATA",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (ownerName.isNotBlank()) {
                                Text(
                                    text = "Owner: $ownerName",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "v1.0.0",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Dashboard Premium Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "You Will Receive",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = AccountingEngine.formatCurrency(dashboardSummary.totalReceivableMinor),
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenReceivable
                                    )
                                }

                                VerticalDivider(
                                    modifier = Modifier
                                        .height(44.dp)
                                        .padding(horizontal = 12.dp),
                                    color = Color.LightGray.copy(alpha = 0.6f)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "You Will Pay",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = AccountingEngine.formatCurrency(dashboardSummary.totalPayableMinor),
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RedPayable
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 14.dp),
                                color = Color.LightGray.copy(alpha = 0.4f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Net Balance",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val netColor = when {
                                    dashboardSummary.netBalanceMinor > 0 -> GreenReceivable
                                    dashboardSummary.netBalanceMinor < 0 -> RedPayable
                                    else -> SettledGray
                                }
                                Surface(
                                    color = netColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = AccountingEngine.formatCurrency(dashboardSummary.netBalanceMinor),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = netColor,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar & Sort Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search customer by name or phone...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GreenPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = CardBorderLight
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = GreenPrimary)
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    CustomerSort.values().forEach { sort ->
                        DropdownMenuItem(
                            text = {
                                val label = when (sort) {
                                    CustomerSort.NAME_AZ -> "Name A-Z"
                                    CustomerSort.HIGHEST_BALANCE -> "Highest Balance"
                                    CustomerSort.LOWEST_BALANCE -> "Lowest Balance"
                                    CustomerSort.RECENTLY_UPDATED -> "Recently Updated"
                                }
                                Text(label, fontWeight = if (sort == selectedSort) FontWeight.Bold else FontWeight.Normal)
                            },
                            onClick = {
                                viewModel.onSortSelect(sort)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CustomerFilter.values().forEach { filter ->
                    val isSelected = filter == selectedFilter
                    val label = when (filter) {
                        CustomerFilter.ALL -> "All Accounts"
                        CustomerFilter.YOU_WILL_RECEIVE -> "You Receive"
                        CustomerFilter.YOU_WILL_PAY -> "You Pay"
                        CustomerFilter.SETTLED -> "Settled"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onFilterSelect(filter) },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(14.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Customer Accounts List
            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No customer matches search." else "No customer accounts yet.\nTap '+ ADD CUSTOMER' to get started.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customers, key = { it.customer.id }) { item ->
                        CustomerCard(
                            summary = item,
                            onClick = { onNavigateToCustomer(item.customer.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCard(
    summary: CustomerLedgerSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Customer Avatar with Gradient Accent
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryHeaderGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = summary.customer.name.take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.customer.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!summary.customer.phone.isNull_or_blank()) {
                    Text(
                        text = summary.customer.phone!!,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                if (summary.lastTransactionDate != null) {
                    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(summary.lastTransactionDate))
                    Text(
                        text = "Last: $dateStr",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            // Balance Column with Pill Badge
            Column(horizontalAlignment = Alignment.End) {
                val (amtColor, labelText) = when (summary.status) {
                    LedgerStatus.YOU_WILL_RECEIVE -> Pair(GreenReceivable, "You Receive")
                    LedgerStatus.YOU_WILL_PAY -> Pair(RedPayable, "You Pay")
                    LedgerStatus.SETTLED -> Pair(SettledGray, "Settled")
                }

                Surface(
                    color = amtColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = AccountingEngine.formatCurrency(kotlin.math.abs(summary.netBalanceMinor)),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = amtColor
                        )

                        Text(
                            text = labelText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = amtColor
                        )
                    }
                }
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
