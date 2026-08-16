package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.apk.ApkBuildProgress
import com.example.data.apk.ApkFileManager
import com.example.ui.components.BuildTerminalConsole
import com.example.ui.components.GlowingGradientButton
import com.example.ui.theme.*

@Composable
fun ApkBuildModal(
    progress: ApkBuildProgress?,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            if (progress?.isComplete == true) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .border(1.dp, SleekCardBorder, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (progress?.isSuccess == true) SleekSuccessContainer else SleekPrimaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (progress?.isSuccess == true) Icons.Default.CheckCircle else Icons.Default.Build,
                                    contentDescription = null,
                                    tint = if (progress?.isSuccess == true) SleekSuccess else SleekPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (progress?.isComplete == true) "APK Build Ready!" else "Compiling APK Binary",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = if (progress?.isComplete == true) "Signed & ready for install" else "Packaging Android assets & Dex",
                                fontSize = 12.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }

                    if (progress?.isComplete == true) {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_build_modal")) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SleekTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Terminal Build Console
                BuildTerminalConsole(
                    logs = progress?.logOutput ?: "Initializing compiler...",
                    currentStep = progress?.step ?: 1,
                    totalSteps = progress?.totalSteps ?: 6,
                    statusMessage = progress?.statusMessage ?: "Preparing build workspace..."
                )

                if (progress?.isComplete == true && progress.isSuccess) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Success Card with stats
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SleekSuccessContainer.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekSuccess.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = progress.apkFile?.name ?: "app-release.apk",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )
                                Text(
                                    text = "Size: ${ApkFileManager.formatFileSize(progress.fileSizeBytes)} • Signed (v1/v2)",
                                    fontSize = 11.sp,
                                    color = SleekSuccess
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SleekSuccess
                            ) {
                                Text(
                                    text = "VERIFIED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlowingGradientButton(
                            text = "Download APK to Phone",
                            icon = Icons.Default.Download,
                            onClick = onDownload,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "modal_download_apk"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onInstall,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("modal_install_apk"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = SleekPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Install APK", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = onShare,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("modal_share_apk"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = SleekTextSecondary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

