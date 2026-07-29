package com.merakhata.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merakhata.app.domain.accounting.AccountingEngine
import com.merakhata.app.ui.theme.GreenPrimary
import com.merakhata.app.ui.theme.GreenReceivable
import com.merakhata.app.ui.theme.HeaderGradientStart
import com.merakhata.app.ui.theme.RedPayable
import com.merakhata.app.ui.viewmodels.ReportPeriod
import com.merakhata.app.ui.viewmodels.ReportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val reportSummary by viewModel.reportSummary.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Reports", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderGradientStart,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Period Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportPeriod.values().forEach { period ->
                    val isSelected = period == selectedPeriod
                    val label = when (period) {
                        ReportPeriod.TODAY -> "Today"
                        ReportPeriod.LAST_7_DAYS -> "Last 7 Days"
                        ReportPeriod.THIS_MONTH -> "This Month"
                        ReportPeriod.CUSTOM -> "Custom"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onPeriodSelect(period) },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(14.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Report Summary Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("Period Ledger Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)

                    Spacer(modifier = Modifier.height(16.dp))

                    ReportMetricRow(
                        label = "Total Credit Given (YOU GAVE)",
                        amountStr = AccountingEngine.formatCurrency(reportSummary.totalCreditGivenMinor),
                        color = RedPayable
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.5f))

                    ReportMetricRow(
                        label = "Total Payment Received (YOU GOT)",
                        amountStr = AccountingEngine.formatCurrency(reportSummary.totalPaymentReceivedMinor),
                        color = GreenReceivable
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.5f))

                    ReportMetricRow(
                        label = "Total Transactions Recorded",
                        amountStr = "${reportSummary.totalTransactionsCount}",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ReportMetricRow(
    label: String,
    amountStr: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(amountStr, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
