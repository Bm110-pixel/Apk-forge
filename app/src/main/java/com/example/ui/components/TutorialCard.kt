package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TutorialProgress
import com.example.data.tutorial.TutorialManager
import com.example.ui.theme.*

@Composable
fun TutorialCard(
    tutorialManager: TutorialManager,
    tutorialProgress: TutorialProgress,
    onOpenTutorial: () -> Unit,
    onLaunchSandbox: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSteps = tutorialManager.tutorialSteps.size
    val completedCount = tutorialProgress.completedStepIds.size
    val progressFraction = (completedCount.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenTutorial() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF6366F1).copy(alpha = 0.6f),
                    Color(0xFF06B6D4).copy(alpha = 0.6f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = Color(0xFF6366F1).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (tutorialProgress.isCompleted) Icons.Default.CheckCircle else Icons.Default.School,
                                contentDescription = null,
                                tint = if (tutorialProgress.isCompleted) Color(0xFF10B981) else Color(0xFF818CF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Interactive Tutorial Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (tutorialProgress.isCompleted) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF6366F1).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (tutorialProgress.isCompleted) "✓ COMPLETED" else "$completedCount OF $totalSteps STEPS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tutorialProgress.isCompleted) Color(0xFF10B981) else Color(0xFF818CF8),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (tutorialProgress.isCompleted) "You've mastered AI APK Studio! Tap to re-review" else "Learn AI prompting, hotkeys, canvas & cloud sync",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SleekTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(SleekSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF06B6D4), Color(0xFF10B981))
                                )
                            )
                    )
                }

                Text(
                    text = "${(progressFraction * 100).toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLaunchSandbox,
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Launch Sandbox", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF818CF8))
                }

                Button(
                    onClick = onOpenTutorial,
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (tutorialProgress.isCompleted) "Re-run Tour" else "Continue Tour",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
