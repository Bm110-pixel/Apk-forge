package com.example.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.apk.ApkFileManager
import com.example.data.model.ApkBuildRecord
import com.example.ui.components.GlowingGradientButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkVaultScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val builds by viewModel.allBuilds.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = SleekPrimaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FolderZip,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("APK Package Vault", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            Text("${builds.size} compiled binaries ready", fontSize = 12.sp, color = SleekTextSecondary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SleekBackground,
                    titleContentColor = SleekTextPrimary
                )
            )
        },
        containerColor = SleekBackground,
        modifier = modifier
    ) { padding ->
        if (builds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = SleekSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                tint = SleekTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No APKs Compiled Yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Create an app with AI or open the visual editor, then tap 'Build APK' to package and download your first Android app!",
                        fontSize = 13.sp,
                        color = SleekTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(builds, key = { it.id }) { record ->
                    ApkPackageCard(
                        record = record,
                        onDownload = { viewModel.downloadApk(context, record) },
                        onInstall = { viewModel.installApk(context, record) },
                        onShare = { viewModel.shareApk(context, record) },
                        onDelete = { viewModel.deleteBuildRecord(record.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ApkPackageCard(
    record: ApkBuildRecord,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = SleekPrimaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.2f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = record.projectName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = record.packageName,
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete APK",
                        tint = SleekTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SleekSurfaceContainer
                ) {
                    Text(
                        text = "v${record.versionName} (${record.versionCode})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SleekTextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SleekSuccessContainer
                ) {
                    Text(
                        text = ApkFileManager.formatFileSize(record.fileSizeBytes),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekSuccess,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = ApkFileManager.formatDate(record.timestamp),
                    fontSize = 10.sp,
                    color = SleekTextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDownload,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(42.dp)
                        .testTag("download_apk_btn_${record.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onInstall,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("install_apk_btn_${record.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Install", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(42.dp)
                        .background(SleekSurfaceContainer, RoundedCornerShape(10.dp))
                        .border(1.dp, SleekCardBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share APK",
                        tint = SleekTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

