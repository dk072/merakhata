package com.merakhata.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merakhata.app.ui.theme.GreenPrimary
import com.merakhata.app.ui.theme.RedPayable
import com.merakhata.app.ui.viewmodels.SecurityViewModel

@Composable
fun PinLockScreen(
    viewModel: SecurityViewModel,
    onUnlocked: () -> Unit
) {
    val pinInput by viewModel.pinInput.collectAsState()
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val error by viewModel.error.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()

    LaunchedEffect(isUnlocked) {
        if (isUnlocked) {
            onUnlocked()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = GreenPrimary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mera Khata Locked",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )

            Text(
                text = "Enter 4-Digit PIN to unlock",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // PIN Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isFilled = index < pinInput.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) GreenPrimary else Color.LightGray)
                    )
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(error!!, color = RedPayable, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Numeric Keypad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIOMETRIC", "0", "BACKSPACE")
            )

            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.2f))
                                .clickable {
                                    if (key == "BACKSPACE") {
                                        viewModel.onBackspace()
                                    } else if (key == "BIOMETRIC") {
                                        if (isBiometricEnabled) {
                                            viewModel.onBiometricSuccess()
                                        }
                                    } else {
                                        viewModel.onPinChar(key)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "BACKSPACE") {
                                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace")
                            } else if (key == "BIOMETRIC") {
                                if (isBiometricEnabled) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = GreenPrimary)
                                }
                            } else {
                                Text(key, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
