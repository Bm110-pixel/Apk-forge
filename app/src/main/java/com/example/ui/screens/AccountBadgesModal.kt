package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CloudSyncState
import com.example.ui.theme.*

data class UserBadge(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val unlocked: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountBadgesModal(
    syncState: CloudSyncState,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badges = listOf(
        UserBadge(
            title = "Cloud Sync Master",
            description = "Successfully synchronized local projects to the cloud vault.",
            icon = Icons.Default.CloudSync,
            color = Color(0xFF00F0FF),
            unlocked = true
        ),
        UserBadge(
            title = "Anti-Bot Verified",
            description = "Completed secure email anti-bot verification code challenge.",
            icon = Icons.Default.VerifiedUser,
            color = Color(0xFF00E676),
            unlocked = syncState.userEmail != null && !syncState.isGuestMode
        ),
        UserBadge(
            title = "Pro App Builder",
            description = "Created or generated mobile application projects.",
            icon = Icons.Default.Code,
            color = Color(0xFFFF9100),
            unlocked = true
        ),
        UserBadge(
            title = "Multi-Auth Sentinel",
            description = "Supports Google, Microsoft, Apple, and Email secure login.",
            icon = Icons.Default.Security,
            color = Color(0xFF7C4DFF),
            unlocked = true
        ),
        UserBadge(
            title = "Early AI Studio Adopter",
            description = "Built production-ready Android apps in AI Studio.",
            icon = Icons.Default.Star,
            color = Color(0xFFFFD700),
            unlocked = true
        ),
        UserBadge(
            title = "APK Release Ready",
            description = "App configured for direct installable APK distribution.",
            icon = Icons.Default.Android,
            color = Color(0xFF3DDC84),
            unlocked = true
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SleekSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SleekPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Account & Earned Badges",
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (syncState.isGuestMode) "Guest Account Mode" else "Verified Cloud Account",
                        fontSize = 12.sp,
                        color = SleekTextSecondary
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Profile Info Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SleekSurfaceLow,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SleekCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = syncState.userDisplayName ?: "Developer",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = SleekTextPrimary
                                )
                                Surface(
                                    color = if (syncState.isGuestMode) Color(0xFFFF9100).copy(alpha = 0.2f) else Color(0xFF00E676).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (syncState.isGuestMode) "Guest" else "Active",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (syncState.isGuestMode) Color(0xFFFF9100) else Color(0xFF00E676)
                                    )
                                }
                            }

                            Text(
                                text = syncState.userEmail ?: "No email linked",
                                fontSize = 13.sp,
                                color = SleekTextSecondary
                            )

                            HorizontalDivider(color = SleekCardBorder, modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Cloud Vault Sync", fontSize = 12.sp, color = SleekTextSecondary)
                                Text(
                                    text = if (syncState.autoSyncEnabled) "Enabled" else "Paused",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (syncState.autoSyncEnabled) Color(0xFF00E676) else SleekTextSecondary
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Unlocked Developer Badges (${badges.count { it.unlocked }}/${badges.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SleekTextPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(badges) { badge ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (badge.unlocked) badge.color.copy(alpha = 0.08f) else SleekSurfaceLow.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (badge.unlocked) badge.color.copy(alpha = 0.3f) else SleekCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(badge.color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = badge.icon,
                                    contentDescription = null,
                                    tint = badge.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = badge.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = SleekTextPrimary
                                )
                                Text(
                                    text = badge.description,
                                    fontSize = 11.sp,
                                    color = SleekTextSecondary
                                )
                            }
                            if (badge.unlocked) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = badge.color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onSignOut()
                    onDismiss()
                }
            ) {
                Text("Sign Out / Switch Account", color = Color(0xFFFF5252), fontSize = 12.sp)
            }
        }
    )
}
