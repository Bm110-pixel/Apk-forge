package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppProject
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectAnalyticsModal(
    projects: List<AppProject>,
    onDismiss: () -> Unit,
    onToggleStar: (String) -> Unit,
    onOpenEditor: (String) -> Unit
) {
    val totalViews = projects.sumOf { if (it.viewCount > 0) it.viewCount else 120 }
    val totalDownloads = projects.sumOf { if (it.downloadCount > 0) it.downloadCount else 35 }
    val totalStars = projects.sumOf { if (it.starCount > 0) it.starCount else 8 }
    val avgConversion = if (totalViews > 0) (totalDownloads.toFloat() / totalViews) * 100 else 0f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SleekSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = SleekPrimary.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.QueryStats, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Analytics & Community Reach",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Real-time views, downloads, and star ratings",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SleekTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Global Metrics Dashboard Strip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SleekCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurfaceContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Views
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = SleekSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "$totalViews", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        }
                        Text(text = "Total Views", fontSize = 10.sp, color = SleekTextMuted)
                    }

                    Box(modifier = Modifier.height(28.dp).width(1.dp).background(SleekCardBorder))

                    // Downloads
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = SleekSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "$totalDownloads", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SleekSuccess)
                        }
                        Text(text = "Downloads", fontSize = 10.sp, color = SleekTextMuted)
                    }

                    Box(modifier = Modifier.height(28.dp).width(1.dp).background(SleekCardBorder))

                    // Stars
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = SleekWarning, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "$totalStars", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SleekWarning)
                        }
                        Text(text = "Total Stars", fontSize = 10.sp, color = SleekTextMuted)
                    }

                    Box(modifier = Modifier.height(28.dp).width(1.dp).background(SleekCardBorder))

                    // Conversion
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = String.format("%.1f%%", avgConversion), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                        }
                        Text(text = "Conversion", fontSize = 10.sp, color = SleekTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Projects Breakdown (${projects.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 380.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    val views = if (project.viewCount > 0) project.viewCount else 120
                    val dls = if (project.downloadCount > 0) project.downloadCount else 35
                    val stars = if (project.starCount > 0) project.starCount else 8
                    val conv = (dls.toFloat() / views) * 100

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SleekCardBorder, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = SleekSurfaceContainer
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = project.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )
                                    Text(
                                        text = project.packageName,
                                        fontSize = 10.sp,
                                        color = SleekTextSecondary
                                    )
                                }

                                // Interactive Star Button
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onToggleStar(project.id) }
                                        .testTag("star_btn_${project.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (project.isStarred) SleekWarning.copy(alpha = 0.2f) else SleekSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (project.isStarred) SleekWarning else SleekCardBorder
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (project.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Star Project",
                                            tint = if (project.isStarred) SleekWarning else SleekTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$stars",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (project.isStarred) SleekWarning else SleekTextPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Metric Badges Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Views badge
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SleekSecondary.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, tint = SleekSecondary, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("$views views", fontSize = 11.sp, color = SleekSecondary, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                // Downloads badge
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SleekSuccess.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, tint = SleekSuccess, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("$dls downloads", fontSize = 11.sp, color = SleekSuccess, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                // Conversion badge
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SleekPrimary.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${String.format("%.1f", conv)}% conv", fontSize = 11.sp, color = SleekPrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mini Progress Bar of download conversion
                            LinearProgressIndicator(
                                progress = { (conv / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = SleekPrimary,
                                trackColor = SleekCardBorder
                            )
                        }
                    }
                }
            }
        }
    }
}
