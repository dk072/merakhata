package com.merakhata.app.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    val haptic = LocalHapticFeedback.current
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
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToAddCustomer()
                },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White) },
                text = { Text("Add Customer", fontWeight = FontWeight.Bold, color = Color.White) },
                containerColor = PrimaryContainerNavy,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SurfaceBg)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = OnSurfaceDark)
                    }
                    Text(
                        text = if (businessName.isNotBlank()) businessName else "Mera Khata",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurfaceDark
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = OnSurfaceDark)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (ownerName.isNotBlank()) ownerName.take(2).uppercase() else "MK",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dashboard Summary Card (Dark Navy Container)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PrimaryContainerNavy),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "TOTAL BALANCE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = AccountingEngine.formatCurrency(dashboardSummary.netBalanceMinor),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Breakdown Tiles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // You'll Get
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = SecondaryFixedDimMint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "You'll Get",
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = AccountingEngine.formatCurrency(dashboardSummary.totalReceivableMinor),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SecondaryFixedMint
                                        )
                                    }
                                }

                                // You'll Give
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                tint = ErrorContainerPink,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "You'll Give",
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = AccountingEngine.formatCurrency(dashboardSummary.totalPayableMinor),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ErrorContainerPink
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Search & Filter Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Search customers...", color = OutlineGray, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = null, tint = OutlineGray) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = OutlineGray)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerLow,
                                unfocusedContainerColor = SurfaceContainerLow,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = OnSurfaceDark,
                                unfocusedTextColor = OnSurfaceDark
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showSortMenu = true
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(SurfaceContainer, RoundedCornerShape(14.dp))
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = OnSurfaceDark)
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
                                        Text(label, fontWeight = if (sort == selectedSort) FontWeight.Bold else FontWeight.Normal, color = OnSurfaceDark)
                                    },
                                    onClick = {
                                        viewModel.onSortSelect(sort)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CustomerFilter.values().forEach { filter ->
                            val isSelected = filter == selectedFilter
                            val label = when (filter) {
                                CustomerFilter.ALL -> "All"
                                CustomerFilter.YOU_WILL_RECEIVE -> "Got"
                                CustomerFilter.YOU_WILL_PAY -> "Gave"
                                CustomerFilter.SETTLED -> "Settled"
                            }
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.onFilterSelect(filter)
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) PrimaryContainerNavy else SurfaceContainerHigh,
                                    contentColor = if (isSelected) Color.White else OnSurfaceVariantGray
                                ),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Section Title: Recent Customers
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Customers",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                        TextButton(onClick = { viewModel.onFilterSelect(CustomerFilter.ALL) }) {
                            Text("View All", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryDark)
                        }
                    }
                }

                // Customer Accounts List
                if (customers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
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
                                    Icons.Default.PeopleOutline,
                                    contentDescription = null,
                                    tint = OutlineGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No customer matches your search." else "No customer accounts yet.\nTap 'Add Customer' to get started.",
                                    color = OnSurfaceVariantGray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    items(customers, key = { it.customer.id }) { item ->
                        CustomerCard(
                            summary = item,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNavigateToCustomer(item.customer.id)
                            }
                        )
                    }
                }

                // Monthly Collection Target Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Monthly Collection Target",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceDark
                                )
                                Text(
                                    text = "65% Achieved",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDark
                                )
                            }

                            LinearProgressIndicator(
                                progress = { 0.65f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = SecondaryTeal,
                                trackColor = SurfaceContainer
                            )

                            Text(
                                text = "₹ 2.5L collected out of ₹ 4L target.",
                                fontSize = 12.sp,
                                color = OutlineGray
                            )
                        }
                    }
                }

                // Bottom padding for FAB and Navigation bar
                item {
                    Spacer(modifier = Modifier.height(80.dp))
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Customer Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = summary.customer.name.take(1).uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.customer.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark
                )
                if (summary.lastTransactionDate != null) {
                    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(summary.lastTransactionDate))
                    Text(
                        text = "Updated $dateStr",
                        fontSize = 12.sp,
                        color = OutlineGray
                    )
                } else if (!summary.customer.phone.isNullOrEmpty()) {
                    Text(
                        text = summary.customer.phone,
                        fontSize = 12.sp,
                        color = OutlineGray
                    )
                }
            }

            // Balance Column with Pill Badge
            Column(horizontalAlignment = Alignment.End) {
                val (amtColor, labelText) = when (summary.status) {
                    LedgerStatus.YOU_WILL_RECEIVE -> Pair(SecondaryTeal, "YOU'LL GET")
                    LedgerStatus.YOU_WILL_PAY -> Pair(ErrorRed, "YOU'LL GIVE")
                    LedgerStatus.SETTLED -> Pair(OutlineGray, "SETTLED")
                }

                Text(
                    text = labelText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = amtColor,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = AccountingEngine.formatCurrency(kotlin.math.abs(summary.netBalanceMinor)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = amtColor
                )
            }
        }
    }
}
