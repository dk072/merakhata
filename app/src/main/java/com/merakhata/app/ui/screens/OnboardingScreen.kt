package com.merakhata.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merakhata.app.ui.theme.GreenPrimary
import com.merakhata.app.ui.theme.HeaderGradientStart
import com.merakhata.app.ui.viewmodels.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val ownerName by viewModel.ownerName.collectAsState()
    val businessName by viewModel.businessName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome to Mera Khata", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = "Logo",
                tint = GreenPrimary,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Digital Udhar Khata",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )

            Text(
                text = "Fast, secure, offline credit ledger for shopkeepers and individuals",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = ownerName,
                onValueChange = { viewModel.onOwnerNameChange(it) },
                label = { Text("Your Name / Owner Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = businessName,
                onValueChange = { viewModel.onBusinessNameChange(it) },
                label = { Text("Business Name (Optional)") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.saveOnboarding(onComplete) },
                enabled = ownerName.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Create My Khata", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Version 1.0.0", fontSize = 12.sp, color = Color.Gray)
        }
    }
}
