package com.merakhata.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merakhata.app.ui.theme.DeepEmerald
import com.merakhata.app.ui.theme.PayableRed
import com.merakhata.app.ui.viewmodels.SecurityViewModel

@Composable
fun PinLockScreen(
    viewModel: SecurityViewModel,
    onUnlocked: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
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
            Surface(
                color = DeepEmerald.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = DeepEmerald,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Mera Khata Security",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DeepEmerald
            )

            Text(
                text = "Enter 4-Digit Security PIN to Unlock",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
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
                            .background(if (isFilled) DeepEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(error!!, color = PayableRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace", tint = MaterialTheme.colorScheme.onSurface)
                            } else if (key == "BIOMETRIC") {
                                if (isBiometricEnabled) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = DeepEmerald)
                                }
                            } else {
                                Text(key, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
