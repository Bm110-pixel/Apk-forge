package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.SubscriptionManager
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: StudioViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStore: () -> Unit
) {
    val context = LocalContext.current
    val subStatus by viewModel.subscriptionStatus.collectAsState()
    var promoCodeInput by remember { mutableStateOf("") }
    var showRedeemSuccessDialog by remember { mutableStateOf(false) }
    var promoErrorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings & Codes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Manage account, codes & AI engine defaults",
                            fontSize = 12.sp,
                            color = SleekTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SleekTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SleekSurface
                )
            )
        },
        containerColor = SleekBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ================= PRO MEMBERSHIP CARD =================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (subStatus.isPremium) Brush.horizontalGradient(listOf(SleekPrimary, SleekWarning, SleekSuccess))
                            else Brush.horizontalGradient(listOf(SleekCardBorder, SleekCardBorder)),
                            RoundedCornerShape(20.dp)
                        )
                        .testTag("membership_status_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (subStatus.isPremium) SleekSurfaceContainer else SleekSurface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    color = if (subStatus.isPremium) SleekWarning.copy(alpha = 0.2f) else SleekPrimaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (subStatus.isPremium) Icons.Default.WorkspacePremium else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (subStatus.isPremium) SleekWarning else SleekPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = subStatus.tierName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )
                                    Text(
                                        text = if (subStatus.isPremium) "Valid until: ${SubscriptionManager.getInstance(context).formatExpiryDate(subStatus.expiresAtTimestamp)}"
                                        else "Free Starter Tier",
                                        fontSize = 12.sp,
                                        color = if (subStatus.isPremium) SleekSuccess else SleekTextSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (subStatus.isPremium) SleekWarning.copy(alpha = 0.15f) else SleekSurfaceLow,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (subStatus.isPremium) SleekWarning else SleekCardBorder
                                )
                            ) {
                                Text(
                                    text = if (subStatus.isPremium) "PRO ACTIVE" else "STARTER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (subStatus.isPremium) SleekWarning else SleekTextMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Active Privileges:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SleekTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        subStatus.unlockedPerks.forEach { perk ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (subStatus.isPremium) SleekSuccess else SleekPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = perk,
                                    fontSize = 12.sp,
                                    color = SleekTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // ================= APPEARANCE & THEME SETTINGS =================
            item {
                val isDarkMode by viewModel.isDarkMode.collectAsState()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekCardBorder, RoundedCornerShape(20.dp))
                        .testTag("appearance_settings_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    color = SleekPrimaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                            contentDescription = null,
                                            tint = SleekPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Dark Theme",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )
                                    Text(
                                        text = "Reduce eye strain during long design sessions",
                                        fontSize = 12.sp,
                                        color = SleekTextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.setDarkMode(it) },
                                modifier = Modifier.testTag("dark_mode_switch")
                            )
                        }
                    }
                }
            }

            // ================= PROMO CODES / REDEEM SECTION =================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekCardBorder, RoundedCornerShape(20.dp))
                        .testTag("redeem_promo_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Redeem Promo Code",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Have a developer access code? Enter code 'dev15' to unlock 3 Months of Free Premium with all store assets & AI models unlocked.",
                            fontSize = 12.sp,
                            color = SleekTextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = promoCodeInput,
                                onValueChange = {
                                    promoCodeInput = it
                                    promoErrorMessage = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("promo_code_input"),
                                placeholder = { Text("Enter promo code (e.g. dev15)", fontSize = 13.sp, color = SleekTextMuted) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SleekPrimary,
                                    unfocusedBorderColor = SleekCardBorder,
                                    focusedContainerColor = SleekSurfaceLow,
                                    unfocusedContainerColor = SleekSurfaceLow,
                                    focusedTextColor = SleekTextPrimary,
                                    unfocusedTextColor = SleekTextPrimary
                                )
                            )

                            Button(
                                onClick = {
                                    val res = viewModel.redeemPromoCode(promoCodeInput)
                                    if (res.isSuccess) {
                                        showRedeemSuccessDialog = true
                                        promoCodeInput = ""
                                        promoErrorMessage = null
                                    } else {
                                        promoErrorMessage = res.exceptionOrNull()?.message ?: "Invalid code"
                                    }
                                },
                                enabled = promoCodeInput.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SleekPrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("redeem_code_button")
                            ) {
                                Text("Redeem", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (promoErrorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = promoErrorMessage!!,
                                color = SleekError,
                                fontSize = 12.sp
                            )
                        }

                        // Quick hint chip for dev15
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { promoCodeInput = "dev15" },
                            shape = RoundedCornerShape(8.dp),
                            color = SleekSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tap to paste test code: dev15",
                                    fontSize = 11.sp,
                                    color = SleekPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // ================= ASSET STORE SHORTCUT =================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigateToStore() }
                        .border(1.dp, SleekCardBorder, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = SleekPrimaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = SleekPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "In-App Asset Store",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )
                                Text(
                                    text = "Browse UI widgets, vector icon packs & code modules",
                                    fontSize = 12.sp,
                                    color = SleekTextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SleekTextMuted
                        )
                    }
                }
            }

            // ================= AI ENGINE & BUILD PREFERENCES =================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekCardBorder, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "AI Studio & Compiler Preferences",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SettingToggleRow(
                            title = "High-Speed APK Compilation",
                            subtitle = "Multi-threaded bytecode synthesis & resource optimization",
                            initialChecked = true
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = SleekCardBorder
                        )

                        SettingToggleRow(
                            title = "Live Simulator Sound & Haptics",
                            subtitle = "Simulate device vibration & soundpad audio playback",
                            initialChecked = true
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = SleekCardBorder
                        )

                        SettingToggleRow(
                            title = "Custom Dataset Schema Validation",
                            subtitle = "Verify JSON / CSV structure prior to AI neural synthesis",
                            initialChecked = true
                        )
                    }
                }
            }

            // Version info
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI APK Builder v1.0.0 • Powered by Google AI Studio",
                        fontSize = 11.sp,
                        color = SleekTextMuted
                    )
                }
            }
        }
    }

    // Success dialog for code redemption
    if (showRedeemSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showRedeemSuccessDialog = false },
            icon = {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = SleekSuccess.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = SleekSuccess,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "🎉 Promo Code 'dev15' Applied!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Congratulations! You have unlocked 3 Months of Free Pro Membership.",
                        fontSize = 13.sp,
                        color = SleekTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• All In-App Store Assets Unlocked (UI, Icons, Code Modules)\n• Advanced AI Models (Claude, DeepSeek, Gemini Pro)\n• Custom Datasets & Parameter Tuning\n• Unlimited Instant APK Compilations",
                        fontSize = 12.sp,
                        color = SleekTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRedeemSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Start Building")
                }
            },
            containerColor = SleekSurface
        )
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    initialChecked: Boolean
) {
    var checked by remember { mutableStateOf(initialChecked) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SleekTextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = SleekTextSecondary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SleekPrimary
            )
        )
    }
}
