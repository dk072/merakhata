package com.merakhata.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merakhata.app.domain.accounting.AccountingEngine
import com.merakhata.app.ui.theme.*
import com.merakhata.app.ui.viewmodels.ReportPeriod
import com.merakhata.app.ui.viewmodels.ReportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel) {
    val haptic = LocalHapticFeedback.current
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val reportSummary by viewModel.reportSummary.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Analytics & Reports", fontWeight = FontWeight.ExtraBold, color = Color.White) },
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
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Period Filter Chips (Horizontal Scrollable for 100% Mobile Responsiveness)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportPeriod.values().forEach { period ->
                    val isSelected = period == selectedPeriod
                    val label = when (period) {
                        ReportPeriod.TODAY -> "Today"
                        ReportPeriod.LAST_7_DAYS -> "Last 7 Days"
                        ReportPeriod.THIS_MONTH -> "This Month"
                        ReportPeriod.CUSTOM -> "All Time"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.onPeriodSelect(period)
                        },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium) },
                        shape = RoundedCornerShape(14.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            labelColor = DeepCharcoal,
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = CardBorderLight,
                            selectedBorderColor = EmeraldPrimary,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Report Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CardBorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("Period Ledger Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)

                    Spacer(modifier = Modifier.height(18.dp))

                    ReportMetricRow(
                        label = "Total Credit Given (YOU GAVE)",
                        amountStr = AccountingEngine.formatCurrency(reportSummary.totalCreditGivenMinor),
                        color = PayableRed
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = CardBorderLight)

                    ReportMetricRow(
                        label = "Total Payment Received (YOU GOT)",
                        amountStr = AccountingEngine.formatCurrency(reportSummary.totalPaymentReceivedMinor),
                        color = ReceivableGreen
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = CardBorderLight)

                    ReportMetricRow(
                        label = "Total Transactions Recorded",
                        amountStr = "${reportSummary.totalTransactionsCount} entries",
                        color = DeepCharcoal
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
        Text(label, fontSize = 13.sp, color = MediumSlate, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(amountStr, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}
