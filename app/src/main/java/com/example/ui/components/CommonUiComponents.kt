package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ComponentType
import com.example.ui.theme.*

@Composable
fun GlowingGradientButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "glowing_button"
) {
    val gradient = Brush.horizontalGradient(
        colors = if (enabled) listOf(SleekPrimary, SleekPrimaryLight) else listOf(SleekSurfaceContainerHigh, SleekCardBorder)
    )

    Surface(
        modifier = modifier
            .testTag(testTag)
            .height(48.dp)
            .shadow(if (enabled) 4.dp else 0.dp, RoundedCornerShape(12.dp), ambientColor = SleekPrimary.copy(alpha = 0.3f), spotColor = SleekPrimary.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) Color.White else SleekTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = if (enabled) Color.White else SleekTextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Composable
fun BuildTerminalConsole(
    logs: String,
    currentStep: Int,
    totalSteps: Int,
    statusMessage: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(logs) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SleekCardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(SleekSuccess.copy(alpha = pulseAlpha), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APK BUILD ENGINE v2.0",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = SleekPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SleekPrimaryContainer
                ) {
                    Text(
                        text = "STEP $currentStep / $totalSteps",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SleekOnPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress indicator
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SleekPrimary,
                trackColor = SleekSurfaceContainer
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "> $statusMessage",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SleekTextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Terminal Logs Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 220.dp)
                    .background(SleekCodeBackground, RoundedCornerShape(10.dp))
                    .border(1.dp, SleekCodeBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = if (logs.isBlank()) "Initializing build daemon...\n" else logs,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = SleekCodeText,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ComponentTypeBadge(
    type: ComponentType,
    modifier: Modifier = Modifier
) {
    val (label, color, icon) = when (type) {
        ComponentType.HEADER -> Triple("HEADER", SleekPrimary, Icons.Default.Title)
        ComponentType.TEXT -> Triple("TEXT", SleekSecondary, Icons.Default.TextFields)
        ComponentType.BUTTON -> Triple("BUTTON", SleekPrimaryLight, Icons.Default.SmartButton)
        ComponentType.INPUT_FIELD -> Triple("INPUT", SleekTertiary, Icons.Default.Edit)
        ComponentType.CARD -> Triple("CARD", SleekWarning, Icons.Default.ViewAgenda)
        ComponentType.IMAGE_BANNER -> Triple("IMAGE", SleekError, Icons.Default.Image)
        ComponentType.SWITCH -> Triple("SWITCH", SleekSuccess, Icons.Default.ToggleOn)
        ComponentType.SLIDER -> Triple("SLIDER", SleekCyan, Icons.Default.Tune)
        ComponentType.PROGRESS_BAR -> Triple("PROGRESS", SleekPrimary, Icons.Default.PendingActions)
        ComponentType.METRIC_STAT -> Triple("METRIC", SleekWarning, Icons.Default.Speed)
        ComponentType.LIST_VIEW -> Triple("LIST", SleekSecondary, Icons.Default.FormatListBulleted)
        ComponentType.ACTION_CHIP -> Triple("CHIP", SleekSuccess, Icons.Default.Label)
        ComponentType.BADGE -> Triple("BADGE", SleekTertiary, Icons.Default.Verified)
        ComponentType.DIVIDER -> Triple("DIVIDER", SleekTextMuted, Icons.Default.HorizontalRule)
        ComponentType.COUNTER_WIDGET -> Triple("COUNTER", SleekPrimary, Icons.Default.AddCircleOutline)
        ComponentType.RATING_BAR -> Triple("RATING", SleekWarning, Icons.Default.Star)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
