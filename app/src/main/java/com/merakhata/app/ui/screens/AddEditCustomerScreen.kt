package com.merakhata.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merakhata.app.ui.theme.GreenPrimary
import com.merakhata.app.ui.theme.HeaderGradientStart
import com.merakhata.app.ui.viewmodels.AddEditCustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    viewModel: AddEditCustomerViewModel,
    onNavigateBack: () -> Unit
) {
    val name by viewModel.name.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val address by viewModel.address.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val nameError by viewModel.nameError.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add / Edit Customer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderGradientStart,
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Customer Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GreenPrimary) },
                isError = nameError != null,
                supportingText = {
                    if (nameError != null) Text(nameError!!, color = MaterialTheme.colorScheme.error)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { viewModel.onPhoneChange(it) },
                label = { Text("Phone Number (Optional)") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GreenPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { viewModel.onAddressChange(it) },
                label = { Text("Address (Optional)") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = GreenPrimary) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.onNotesChange(it) },
                label = { Text("Notes / Description (Optional)") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = GreenPrimary) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveCustomer(onSuccess = onNavigateBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Save Customer Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
