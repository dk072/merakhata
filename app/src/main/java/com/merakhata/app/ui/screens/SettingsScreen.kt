package com.merakhata.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.merakhata.app.MeraKhataApp
import com.merakhata.app.domain.updater.UpdateManager
import com.merakhata.app.domain.updater.UpdateStatus
import com.merakhata.app.ui.theme.*
import com.merakhata.app.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

import com.merakhata.app.domain.sync.SyncState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val ownerName by viewModel.ownerName.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val updateUrl by viewModel.updateUrl.collectAsState()
    val autoCheckUpdates by viewModel.autoCheckUpdates.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val backupSummary by viewModel.backupSummary.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var ownerInput by remember(ownerName) { mutableStateOf(ownerName) }
    var businessInput by remember(businessName) { mutableStateOf(businessName) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showUpdateConfigDialog by remember { mutableStateOf(false) }

    val appDatabase = (context.applicationContext as MeraKhataApp).database
    val currentVerCode = UpdateManager.getCurrentVersionCode(context)
    val currentVerName = UpdateManager.getCurrentVersionName(context)

    // Backup Create Launcher
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val json = viewModel.generateBackupJson()
                try {
                    val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
                    outputStream?.write(json.toByteArray())
                    outputStream?.close()
                    Toast.makeText(context, "Backup exported successfully!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Backup Open Launcher
    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val json = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                viewModel.validateImportJson(json)
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading backup file", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Local APK Install Launcher
    val selectApkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val tempApk = File(context.cacheDir, "MeraKhata_LocalInstall.apk")
                if (inputStream != null) {
                    FileOutputStream(tempApk).use { out ->
                        inputStream.copyTo(out)
                    }
                    inputStream.close()
                    viewModel.installLocalApk(context, tempApk)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read APK file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // CSV Export Launcher
    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val csv = viewModel.generateCsvString()
                try {
                    val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
                    outputStream?.write(csv.toByteArray())
                    outputStream?.close()
                    Toast.makeText(context, "CSV exported successfully!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "CSV export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(PrimaryHeaderGradient)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "Settings & App Updates",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Profile / Business Details",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = ownerInput,
                        onValueChange = { ownerInput = it },
                        label = { Text("Owner Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = businessInput,
                        onValueChange = { businessInput = it },
                        label = { Text("Business Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.updateProfile(ownerInput, businessInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Save Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // In-App Updater Card (HIGH CONTRAST REDESIGN)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "In-App Auto Updater",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Text(
                                text = "Current Installed: v$currentVerName (Build $currentVerCode)",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = { showUpdateConfigDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configure Update Server", tint = primaryColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.checkForUpdates(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check for App Updates", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { selectApkLauncher.launch(arrayOf("application/vnd.android.package-archive", "*/*")) },
                        border = BorderStroke(1.5.dp, primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.InstallMobile, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Install Local APK File Directly", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            // Cloud Database Backup & Sync Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Cloud Database Sync",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Text(
                                text = when (val s = syncState) {
                                    is SyncState.Idle -> "Status: Ready to Sync"
                                    is SyncState.Syncing -> "Status: Syncing in progress..."
                                    is SyncState.Success -> "Last Synced: ${s.formattedTime}"
                                    is SyncState.Error -> "Status: Sync Error"
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Cloud Status",
                            tint = if (syncState is SyncState.Success) GreenReceived else primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.triggerCloudSync() },
                        enabled = syncState !is SyncState.Syncing,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (syncState is SyncState.Syncing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Syncing Data to Cloud...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync Ledger Data to Cloud Now", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // Security Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Security & App Lock", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable PIN Lock", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Protect your khata ledger with a 4-digit PIN", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showPinDialog = true
                                } else {
                                    viewModel.setAppLock(false, null)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Biometric Unlock", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Use fingerprint or face ID to unlock", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            enabled = isAppLockEnabled,
                            onCheckedChange = { viewModel.setBiometricEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                        )
                    }
                }
            }

            // Backup & Export Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Backup & Export Data", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { createBackupLauncher.launch("MeraKhata_Backup.json") },
                        border = BorderStroke(1.5.dp, primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Backup Data to Storage", color = primaryColor, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { openBackupLauncher.launch(arrayOf("application/json", "*/*")) },
                        border = BorderStroke(1.5.dp, primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore Data from Backup File", color = primaryColor, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { createCsvLauncher.launch("MeraKhata_Transactions.csv") },
                        border = BorderStroke(1.5.dp, primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export All Transactions to CSV", color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Privacy & Offline Guarantee
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("100% Offline & Private", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mera Khata stores all your customer data securely on your device. No customer data is ever sent to external servers.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // App Info & Version Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Mera Khata", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    Text("Digital Udhar Khata / Credit Ledger", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = primaryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Version $currentVerName (Build $currentVerCode)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    // Set PIN Dialog
    if (showPinDialog) {
        SetPinDialog(
            onDismiss = { showPinDialog = false },
            onPinSet = { pin ->
                viewModel.setAppLock(true, pin)
                showPinDialog = false
            }
        )
    }

    // Update Server Config Dialog
    if (showUpdateConfigDialog) {
        UpdateConfigDialog(
            currentUrl = updateUrl,
            currentAutoCheck = autoCheckUpdates,
            onDismiss = { showUpdateConfigDialog = false },
            onSave = { url, auto ->
                viewModel.setUpdateSettings(url, auto)
                showUpdateConfigDialog = false
            }
        )
    }

    // Update Status Modals
    when (val status = updateStatus) {
        is UpdateStatus.Checking -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetUpdateStatus() },
                title = { Text("Checking for Updates...") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Querying update server for latest version...")
                    }
                },
                confirmButton = {}
            )
        }
        is UpdateStatus.Available -> {
            val info = status.updateInfo
            AlertDialog(
                onDismissRequest = { if (!info.isMandatory) viewModel.resetUpdateStatus() },
                title = { Text("New Update Available! (v${info.versionName})", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("A new version of Mera Khata is available for installation.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Release Notes:", fontWeight = FontWeight.SemiBold)
                        Text(info.releaseNotes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.downloadAndInstallUpdate(context, info) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White)
                    ) {
                        Text("Update & Auto-Install", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    if (!info.isMandatory) {
                        TextButton(onClick = { viewModel.resetUpdateStatus() }) {
                            Text("Later")
                        }
                    }
                }
            )
        }
        is UpdateStatus.NoUpdate -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetUpdateStatus() },
                title = { Text("App is Up to Date") },
                text = { Text("You are using the latest version of Mera Khata (v$currentVerName).") },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetUpdateStatus() }) {
                        Text("OK", fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                }
            )
        }
        is UpdateStatus.Downloading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Downloading Update...") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(
                            progress = { status.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${status.progressPercent}% downloaded", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                },
                confirmButton = {}
            )
        }
        is UpdateStatus.ReadyToInstall -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetUpdateStatus() },
                title = { Text("Ready to Install") },
                text = { Text("Download complete! Launching Android Package Installer...") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.installLocalApk(context, status.apkFile) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White)
                    ) {
                        Text("Launch Installer Now")
                    }
                }
            )
        }
        is UpdateStatus.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetUpdateStatus() },
                title = { Text("Update Error") },
                text = { Text(status.message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetUpdateStatus() }) {
                        Text("OK")
                    }
                }
            )
        }
        UpdateStatus.Idle -> {}
    }

    // Backup Verification Modal before Restore
    if (backupSummary != null) {
        val sum = backupSummary!!
        AlertDialog(
            onDismissRequest = { viewModel.clearBackupSummary() },
            title = { Text(if (sum.isValid) "Confirm Backup Restoration" else "Invalid Backup File") },
            text = {
                if (sum.isValid) {
                    Column {
                        Text("Backup details verified:")
                        Text("• Date: ${sum.backupDateStr}")
                        Text("• Customers: ${sum.customerCount}")
                        Text("• Transactions: ${sum.transactionCount}")
                        Text("• Reminders: ${sum.reminderCount}")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("WARNING: Restoring will replace existing ledger data with this backup.", color = RedPayable, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(sum.errorMessage ?: "The selected file is not a valid Mera Khata backup.")
                }
            },
            confirmButton = {
                if (sum.isValid) {
                    TextButton(
                        onClick = {
                            viewModel.restoreBackup(appDatabase, sum.rawJson, onSuccess = {})
                        }
                    ) {
                        Text("Restore Now", color = RedPayable, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearBackupSummary() }) {
                    Text(if (sum.isValid) "Cancel" else "OK")
                }
            }
        )
    }
}

@Composable
fun SetPinDialog(
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set 4-Digit Security PIN") },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("Enter 4-Digit PIN") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (pin.length == 4) {
                        onPinSet(pin)
                    }
                }
            ) {
                Text("Set PIN", fontWeight = FontWeight.Bold, color = primaryColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UpdateConfigDialog(
    currentUrl: String,
    currentAutoCheck: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    var urlInput by remember { mutableStateOf(currentUrl) }
    var autoCheck by remember { mutableStateOf(currentAutoCheck) }
    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Server Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Update JSON Server URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto check updates on launch")
                    Switch(
                        checked = autoCheck,
                        onCheckedChange = { autoCheck = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(urlInput, autoCheck) }) {
                Text("Save", fontWeight = FontWeight.Bold, color = primaryColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
