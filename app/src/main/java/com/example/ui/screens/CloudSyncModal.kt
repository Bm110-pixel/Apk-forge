package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppProject
import com.example.data.model.CloudProjectRecord
import com.example.data.model.CloudSyncState
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncModal(
    syncState: CloudSyncState,
    localProjects: List<AppProject>,
    onDismiss: () -> Unit,
    onUploadProject: (String) -> Unit,
    onSyncAllLocal: () -> Unit,
    onRefreshCloud: () -> Unit,
    onImportCloudProject: (CloudProjectRecord) -> Unit,
    onDeleteCloudProject: (String) -> Unit,
    onToggleAutoSync: (Boolean) -> Unit,
    onUpdateDeviceName: (String) -> Unit,
    onSetAccount: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onOpenProject: (String) -> Unit
) {
    val context = LocalContext.current
    var showDeviceRenameDialog by remember { mutableStateOf(false) }
    var deviceNameInput by remember { mutableStateOf(syncState.currentDeviceName) }

    var showAccountDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf(syncState.userEmail ?: "developer@aistudio.com") }
    var nameInput by remember { mutableStateOf(syncState.userDisplayName ?: "Android Dev") }
    var verificationCodeSent by remember { mutableStateOf(false) }
    var verificationCodeInput by remember { mutableStateOf("") }
    var expectedCode by remember { mutableStateOf("") }
    var verificationError by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Cloud Vault, 1 = Local Sync Status, 2 = Device & Settings

    if (showDeviceRenameDialog) {
        AlertDialog(
            onDismissRequest = { showDeviceRenameDialog = false },
            containerColor = SleekSurface,
            title = {
                Text(
                    text = "Edit Device Label",
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Set a friendly name to identify which phone/tablet modified each project in the cloud.",
                        fontSize = 12.sp,
                        color = SleekTextSecondary
                    )
                    OutlinedTextField(
                        value = deviceNameInput,
                        onValueChange = { deviceNameInput = it },
                        singleLine = true,
                        placeholder = { Text("e.g. Pixel 8 Pro (Personal)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekCardBorder,
                            focusedTextColor = SleekTextPrimary,
                            unfocusedTextColor = SleekTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateDeviceName(deviceNameInput)
                        showDeviceRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Text("Save Name", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeviceRenameDialog = false }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            }
        )
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            containerColor = SleekSurface,
            title = {
                Text(
                    text = "Sign In & Cloud Account",
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose your preferred authentication provider to sign in and sync your projects securely to the cloud:",
                        fontSize = 12.sp,
                        color = SleekTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Google Login Button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ai.studio/auth/google"))
                            try { context.startActivity(intent) } catch (e: Exception) {}
                            showAccountDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In with Google", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Microsoft Login Button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ai.studio/auth/microsoft"))
                            try { context.startActivity(intent) } catch (e: Exception) {}
                            showAccountDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A4EF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In with Microsoft", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Apple ID Login Button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ai.studio/auth/apple"))
                            try { context.startActivity(intent) } catch (e: Exception) {}
                            showAccountDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In with Apple", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = SleekCardBorder)
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Or link with Email / Custom Account:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextSecondary
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekCardBorder,
                            focusedTextColor = SleekTextPrimary,
                            unfocusedTextColor = SleekTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekCardBorder,
                            focusedTextColor = SleekTextPrimary,
                            unfocusedTextColor = SleekTextPrimary
                        )
                    )

                    if (!verificationCodeSent) {
                        Button(
                            onClick = {
                                if (emailInput.isNotBlank()) {
                                    val code = (100000..999999).random().toString()
                                    expectedCode = code
                                    verificationCodeSent = true
                                    verificationError = null
                                } else {
                                    verificationError = "Please enter a valid email address first"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Anti-Bot Verification Code", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = SleekPrimaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Verification code sent to $emailInput",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekPrimary
                                );
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Simulated Anti-Bot Code: [ $expectedCode ] (Copy & paste below)",
                                    fontSize = 11.sp,
                                    color = SleekTextPrimary
                                )
                            }
                        }

                        OutlinedTextField(
                            value = verificationCodeInput,
                            onValueChange = { verificationCodeInput = it },
                            label = { Text("Enter 6-Digit Verification Code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SleekPrimary,
                                unfocusedBorderColor = SleekCardBorder,
                                focusedTextColor = SleekTextPrimary,
                                unfocusedTextColor = SleekTextPrimary
                            )
                        )
                    }

                    if (verificationError != null) {
                        Text(
                            text = verificationError!!,
                            fontSize = 11.sp,
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                if (verificationCodeSent) {
                    Button(
                        onClick = {
                            if (verificationCodeInput.trim() == expectedCode.trim()) {
                                onSetAccount(emailInput, nameInput)
                                showAccountDialog = false
                                verificationCodeSent = false
                                verificationCodeInput = ""
                            } else {
                                verificationError = "Incorrect verification code. Please check and try again."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                    ) {
                        Text("Verify & Link Account", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAccountDialog = false
                    verificationCodeSent = false
                    verificationCodeInput = ""
                }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF090D16),
        scrimColor = Color.Black.copy(alpha = 0.75f),
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Firebase Cloud Sync",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (syncState.isFirebaseReady) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (syncState.isFirebaseReady) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFF59E0B).copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(if (syncState.isFirebaseReady) Color(0xFF10B981) else Color(0xFFF59E0B), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (syncState.isFirebaseReady) "Live Firestore" else "Cloud Cache",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (syncState.isFirebaseReady) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Multi-Device Continuity & Cloud Project Backup",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshCloud,
                    modifier = Modifier.testTag("refresh_cloud_btn")
                ) {
                    if (syncState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = SleekPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Cloud",
                            tint = SleekPrimary
                        )
                    }
                }
            }

            // Sync Banner info
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = SleekSurfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = syncState.currentDeviceName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit device name",
                                tint = SleekTextMuted,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { showDeviceRenameDialog = true }
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (syncState.lastSyncTime != null) {
                                "Last synced: ${formatRelativeTime(syncState.lastSyncTime)}"
                            } else {
                                "Not synced yet • Ready to push projects"
                            },
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }

                    Button(
                        onClick = onSyncAllLocal,
                        enabled = !syncState.isSyncing && localProjects.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = SleekPrimary,
                divider = { HorizontalDivider(color = SleekCardBorder) },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cloud Vault (${syncState.cloudProjects.size})", fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneIphone, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Local Projects (${localProjects.size})", fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Account & Auto-Sync", fontSize = 12.sp)
                        }
                    }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Cloud Vault Tab
                    if (syncState.cloudProjects.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = SleekTextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No Cloud Projects Found",
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )
                                Text(
                                    text = "Upload your local projects to access them across all your devices.",
                                    fontSize = 12.sp,
                                    color = SleekTextSecondary,
                                    modifier = Modifier.padding(horizontal = 32.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = onSyncAllLocal,
                                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Upload Local Projects Now", color = Color.White)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(syncState.cloudProjects, key = { it.id }) { cloudRecord ->
                                CloudProjectCard(
                                    record = cloudRecord,
                                    onImport = { onImportCloudProject(cloudRecord) },
                                    onDelete = { onDeleteCloudProject(cloudRecord.id) },
                                    onOpen = { onOpenProject(cloudRecord.id) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // Local Projects Upload Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(localProjects, key = { it.id }) { proj ->
                            val isAlreadyInCloud = syncState.cloudProjects.any { it.id == proj.id }
                            LocalProjectSyncRow(
                                project = proj,
                                isSynced = isAlreadyInCloud,
                                onUpload = { onUploadProject(proj.id) },
                                onOpen = { onOpenProject(proj.id) }
                            )
                        }
                    }
                }

                2 -> {
                    // Account & Settings Tab
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // User Account Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SleekSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(40.dp),
                                            shape = CircleShape,
                                            color = SleekPrimary.copy(alpha = 0.2f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = SleekPrimary
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = syncState.userDisplayName ?: "Mobile Developer",
                                                fontWeight = FontWeight.Bold,
                                                color = SleekTextPrimary,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = syncState.userEmail ?: "developer@aistudio.com",
                                                fontSize = 11.sp,
                                                color = SleekTextSecondary
                                            )
                                        }
                                    }

                                    TextButton(onClick = { showAccountDialog = true }) {
                                        Text("Switch", color = SleekPrimary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ai.studio/auth/login"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("web_login_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign In via Web Login Page", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Auto-Sync Toggle Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SleekSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto-Sync to Cloud",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = SleekTextPrimary
                                    )
                                    Text(
                                        text = "Automatically pushes changes to Firestore whenever you save or modify canvas components.",
                                        fontSize = 11.sp,
                                        color = SleekTextSecondary
                                    )
                                }
                                Switch(
                                    checked = syncState.autoSyncEnabled,
                                    onCheckedChange = onToggleAutoSync,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SleekPrimary,
                                        uncheckedThumbColor = SleekTextMuted,
                                        uncheckedTrackColor = SleekBackground
                                    )
                                )
                            }
                        }

                        // Device ID & Info Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SleekSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Device Identification",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = SleekTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Device ID:", fontSize = 11.sp, color = SleekTextSecondary)
                                    Text(
                                        text = syncState.currentDeviceId,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = SleekTextMuted
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Device Label:", fontSize = 11.sp, color = SleekTextSecondary)
                                    Text(
                                        text = syncState.currentDeviceName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CloudProjectCard(
    record: CloudProjectRecord,
    onImport: () -> Unit,
    onDelete: () -> Unit,
    onOpen: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SleekSurfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = try {
                                    Color(android.graphics.Color.parseColor(record.primaryColorHex))
                                } catch (_: Exception) {
                                    SleekPrimary
                                },
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = record.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = record.lastSyncDeviceName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            if (record.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.description,
                    fontSize = 11.sp,
                    color = SleekTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SleekBackground
                    ) {
                        Text(
                            text = "${record.componentCount} components",
                            fontSize = 10.sp,
                            color = SleekTextMuted,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "• ${formatRelativeTime(record.lastSyncedAt)}",
                        fontSize = 10.sp,
                        color = SleekTextMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete from cloud",
                            tint = SleekTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Button(
                        onClick = onImport,
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimaryContainer),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Restore & Edit",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocalProjectSyncRow(
    project: AppProject,
    isSynced: Boolean,
    onUpload: () -> Unit,
    onOpen: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SleekSurfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = project.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isSynced) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "☁️ Synced",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${project.category} • v${project.versionName} • ${project.packageName}",
                    fontSize = 10.sp,
                    color = SleekTextSecondary
                )
            }

            Button(
                onClick = onUpload,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSynced) SleekSurface else SleekPrimary
                ),
                border = if (isSynced) androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder) else null,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(
                    imageVector = if (isSynced) Icons.Default.Sync else Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = if (isSynced) SleekTextPrimary else Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSynced) "Re-Sync" else "Push Cloud",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSynced) SleekTextPrimary else Color.White
                )
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
