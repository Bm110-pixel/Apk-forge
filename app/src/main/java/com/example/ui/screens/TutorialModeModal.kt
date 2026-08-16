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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TutorialProgress
import com.example.data.model.TutorialStep
import com.example.data.tutorial.TutorialManager
import com.example.ui.theme.*

@Composable
fun TutorialModeModal(
    tutorialManager: TutorialManager,
    tutorialProgress: TutorialProgress,
    onDismiss: () -> Unit,
    onLaunchSandbox: () -> Unit,
    onOpenAiCreator: () -> Unit,
    onOpenCloudSync: () -> Unit,
    onOpenStore: () -> Unit,
    onClaimReward: () -> Unit
) {
    val steps = tutorialManager.tutorialSteps
    var currentIdx by remember { mutableIntStateOf(tutorialProgress.currentStepIndex.coerceIn(0, steps.size - 1)) }
    val currentStep = steps[currentIdx]

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1322),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SleekPrimaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "TUTORIAL MODE • STEP ${currentStep.stepNumber} OF ${steps.size}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close tutorial",
                            tint = SleekTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Step Progress Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    steps.forEachIndexed { idx, step ->
                        val isCompleted = tutorialProgress.completedStepIds.contains(step.id)
                        val isCurrent = idx == currentIdx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        isCurrent -> SleekPrimary
                                        isCompleted -> Color(0xFF10B981)
                                        else -> SleekCardBorder
                                    }
                                )
                                .clickable {
                                    currentIdx = idx
                                    tutorialManager.setStepIndex(idx)
                                }
                        )
                    }
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero Step Banner
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SleekSurfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(54.dp),
                                shape = CircleShape,
                                color = getCategoryColor(currentStep.category).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    getCategoryColor(currentStep.category).copy(alpha = 0.4f)
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = getStepIcon(currentStep.iconName),
                                        contentDescription = null,
                                        tint = getCategoryColor(currentStep.category),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = currentStep.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = currentStep.subtitle,
                                fontSize = 12.sp,
                                color = getCategoryColor(currentStep.category),
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = currentStep.description,
                                fontSize = 12.sp,
                                color = SleekTextSecondary,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Highlights checklist
                item {
                    Text(
                        text = "KEY CONCEPTS & WORKFLOW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextMuted,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                items(currentStep.highlights) { highlight ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = SleekSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = highlight,
                                fontSize = 11.sp,
                                color = SleekTextPrimary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // Contextual Interactive Trigger
                item {
                    when (currentStep.id) {
                        "step_welcome" -> {
                            Button(
                                onClick = {
                                    tutorialManager.markStepCompleted("step_welcome")
                                    onLaunchSandbox()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Launch Tutorial Sandbox Project", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        "step_ai_generator" -> {
                            Button(
                                onClick = {
                                    tutorialManager.markStepCompleted("step_ai_generator")
                                    onOpenAiCreator()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Try AI App Generator", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        "step_visual_canvas", "step_keyboard_shortcuts" -> {
                            Button(
                                onClick = {
                                    tutorialManager.markStepCompleted(currentStep.id)
                                    onLaunchSandbox()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Sandbox Canvas & Test Hotkeys", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        "step_cloud_sync" -> {
                            Button(
                                onClick = {
                                    tutorialManager.markStepCompleted("step_cloud_sync")
                                    onOpenCloudSync()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Firebase Cloud Vault", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        "step_store_addons" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        tutorialManager.markStepCompleted("step_store_addons")
                                        onOpenStore()
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Explore Add-Ons & SDKs", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                if (!tutorialProgress.rewardClaimed) {
                                    Button(
                                        onClick = {
                                            tutorialManager.markStepCompleted("step_store_addons")
                                            onClaimReward()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("🎉 Complete Tutorial (+500 Dev Credits)", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "✓ Reward Claimed: +500 Dev Credits granted to your wallet!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentIdx > 0) {
                    OutlinedButton(
                        onClick = {
                            currentIdx--
                            tutorialManager.setStepIndex(currentIdx)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                    ) {
                        Text("Previous", color = SleekTextSecondary, fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        tutorialManager.markStepCompleted(currentStep.id)
                        if (currentIdx < steps.size - 1) {
                            currentIdx++
                            tutorialManager.setStepIndex(currentIdx)
                        } else {
                            if (!tutorialProgress.rewardClaimed) {
                                onClaimReward()
                            }
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (currentIdx < steps.size - 1) "Next Step →" else "Finish Walkthrough",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    )
}

private fun getCategoryColor(category: String): Color {
    return when (category) {
        "Overview" -> Color(0xFF6366F1)
        "AI Synthesis" -> Color(0xFF0284C7)
        "Editor" -> Color(0xFF8B5CF6)
        "Productivity" -> Color(0xFFEC4899)
        "Cloud Continuity" -> Color(0xFF059669)
        "Marketplace" -> Color(0xFFF59E0B)
        else -> Color(0xFF38BDF8)
    }
}

private fun getStepIcon(iconName: String): ImageVector {
    return when (iconName) {
        "rocket_launch" -> Icons.Default.RocketLaunch
        "auto_awesome" -> Icons.Default.AutoAwesome
        "dashboard_customize" -> Icons.Default.DashboardCustomize
        "keyboard" -> Icons.Default.Keyboard
        "cloud_sync" -> Icons.Default.CloudSync
        "storefront" -> Icons.Default.Storefront
        else -> Icons.Default.HelpOutline
    }
}
