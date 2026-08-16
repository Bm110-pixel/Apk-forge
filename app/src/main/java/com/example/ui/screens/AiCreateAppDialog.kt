package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ai.AiAppGenerator
import com.example.data.model.*
import com.example.ui.components.GlowingGradientButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioUiState
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCreateAppDialog(
    viewModel: StudioViewModel,
    onDismiss: () -> Unit,
    onAppCreated: (AppProject) -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf(AiModelOption.GEMINI_FLASH) }
    var temperature by remember { mutableFloatStateOf(0.7f) }
    var selectedThemeMood by remember { mutableStateOf(DesignThemeMood.SLEEK_MINIMAL) }
    var selectedArch by remember { mutableStateOf(ArchitecturePattern.MVVM_COMPOSE) }
    var targetSdk by remember { mutableIntStateOf(35) }

    // Dataset tuning
    var showAdvancedTuning by remember { mutableStateOf(false) }
    var datasetType by remember { mutableStateOf(DatasetInputType.JSON) }
    var datasetPayload by remember { mutableStateOf("") }

    val genState by viewModel.generationState.collectAsState()
    val subStatus by viewModel.subscriptionStatus.collectAsState()

    val isGenerating = genState is StudioUiState.Generating

    val sampleDatasets = listOf(
        "E-Commerce Catalog JSON" to """[{"id": 1, "name": "Wireless Noise Cancelling Headset", "price": "$199.99", "category": "Audio"}, {"id": 2, "name": "Mechanical Gaming Keyboard RGB", "price": "$129.50", "category": "Accessories"}]""",
        "Gym Exercises CSV" to """exercise,sets,target_reps,weight\nBench Press,4,10,80kg\nIncline Dumbbell,3,12,28kg\nCable Flyes,4,15,18kg""",
        "Crypto Tickers JSON" to """[{"symbol": "BTC", "price": 96400, "change": "+4.2%"}, {"symbol": "ETH", "price": 3450, "change": "+2.8%"}, {"symbol": "SOL", "price": 185, "change": "+8.1%"}]""",
        "To-Do Tasks Matrix JSON" to """[{"task": "Build APK Engine", "priority": "High", "done": true}, {"task": "Review Compose UI", "priority": "Medium", "done": false}]"""
    )

    Dialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
                .border(1.dp, SleekCardBorder, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = SleekPrimaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI APK Synthesizer",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )
                                if (subStatus.isPremium) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = SleekWarning.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "PRO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekWarning,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Custom datasets & multi-model parameters",
                                fontSize = 11.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }

                    if (!isGenerating) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_ai_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SleekTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isGenerating) {
                    // Animated AI Generation Pipeline
                    AiGeneratingVisualizer(
                        statusMessage = (genState as? StudioUiState.Generating)?.stepMessage
                            ?: "Synthesizing UI nodes & compiling APK metadata..."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Prompt Input
                        item {
                            Text(
                                text = "1. Describe the App to Build:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 90.dp)
                                    .testTag("ai_prompt_input"),
                                placeholder = {
                                    Text(
                                        "e.g. Build a simple calculator, to-do list, blog reader, or crypto tracker...",
                                        color = SleekTextMuted,
                                        fontSize = 13.sp
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SleekPrimary,
                                    unfocusedBorderColor = SleekCardBorder,
                                    focusedContainerColor = SleekSurfaceLow,
                                    unfocusedContainerColor = SleekSurfaceLow,
                                    focusedTextColor = SleekTextPrimary,
                                    unfocusedTextColor = SleekTextPrimary
                                )
                            )
                        }

                        // One-tap Builtin Templates
                        item {
                            Text(
                                text = "Or Pick a Ready-to-Run Starter Template:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SleekTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 4.dp)
                            ) {
                                items(AiAppGenerator.PRESET_TEMPLATES) { template ->
                                    PresetTemplateChip(
                                        template = template,
                                        onClick = {
                                            if (template.isBuiltinTemplate && template.templateId.isNotBlank()) {
                                                viewModel.createProjectFromTemplate(template.templateId) { project ->
                                                    onAppCreated(project)
                                                }
                                            } else {
                                                promptInput = template.prompt
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // AI Model Selection
                        item {
                            Text(
                                text = "2. Select AI Neural Model:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(AiModelOption.entries.toTypedArray()) { model ->
                                    val isSelected = selectedModel == model
                                    val isAvailable = !model.isPremium || subStatus.isPremium
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (isAvailable) {
                                                    selectedModel = model
                                                }
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) SleekPrimary.copy(alpha = 0.15f) else SleekSurfaceContainer,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) SleekPrimary else SleekCardBorder
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (model.isPremium && !subStatus.isPremium) Icons.Default.Lock else Icons.Default.Psychology,
                                                contentDescription = null,
                                                tint = if (isSelected) SleekPrimary else if (model.isPremium) SleekWarning else SleekTextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = model.displayName,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) SleekPrimary else SleekTextPrimary
                                                    )
                                                    if (model.isPremium) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("PRO", fontSize = 9.sp, color = SleekWarning, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Text(model.provider, fontSize = 10.sp, color = SleekTextMuted)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Advanced AI & Dataset Ingestion Toggle
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { showAdvancedTuning = !showAdvancedTuning },
                                shape = RoundedCornerShape(14.dp),
                                color = SleekSurfaceContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = SleekPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "AI Parameters & Dataset Upload",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekTextPrimary
                                        )
                                    }
                                    Icon(
                                        imageVector = if (showAdvancedTuning) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = SleekTextMuted
                                    )
                                }
                            }
                        }

                        // Advanced Panel (Theme mood, temperature, dataset upload)
                        if (showAdvancedTuning) {
                            // Theme Styling Mood
                            item {
                                Column {
                                    Text(
                                        text = "Visual Design Mood:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SleekTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(DesignThemeMood.entries.toTypedArray()) { mood ->
                                            val isSelected = selectedThemeMood == mood
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { selectedThemeMood = mood },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) SleekPrimary else SleekSurfaceLow,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekCardBorder)
                                            ) {
                                                Text(
                                                    text = mood.displayName,
                                                    fontSize = 11.sp,
                                                    color = if (isSelected) Color.White else SleekTextPrimary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Temperature slider
                            item {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Creativity / Temperature:", fontSize = 12.sp, color = SleekTextSecondary)
                                        Text(String.format("%.1f", temperature), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                                    }
                                    Slider(
                                        value = temperature,
                                        onValueChange = { temperature = it },
                                        valueRange = 0.1f..1.0f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = SleekPrimary,
                                            activeTrackColor = SleekPrimary
                                        )
                                    )
                                }
                            }

                            // Custom Dataset Upload / Ingestion
                            item {
                                Column {
                                    Text(
                                        text = "Upload / Paste Custom Dataset:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SleekTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Dataset format chips
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        DatasetInputType.entries.forEach { type ->
                                            val isSelected = datasetType == type
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { datasetType = type },
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSelected) SleekPrimaryContainer else SleekSurfaceLow,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SleekPrimary else SleekCardBorder)
                                            ) {
                                                Text(
                                                    text = type.displayName,
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) SleekPrimary else SleekTextMuted,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Quick sample datasets loader
                                    Text("Load sample dataset:", fontSize = 11.sp, color = SleekTextMuted)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(sampleDatasets) { (name, payload) ->
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        datasetPayload = payload
                                                        promptInput = "Build a data-driven app showcasing $name"
                                                    },
                                                shape = RoundedCornerShape(6.dp),
                                                color = SleekSurfaceContainer,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                                            ) {
                                                Text(
                                                    text = name,
                                                    fontSize = 10.sp,
                                                    color = SleekPrimary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = datasetPayload,
                                        onValueChange = { datasetPayload = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 80.dp),
                                        placeholder = {
                                            Text(
                                                "Paste JSON, CSV table, or data schema here...",
                                                color = SleekTextMuted,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SleekPrimary,
                                            unfocusedBorderColor = SleekCardBorder,
                                            focusedContainerColor = SleekSurfaceLow,
                                            unfocusedContainerColor = SleekSurfaceLow,
                                            focusedTextColor = SleekTextPrimary,
                                            unfocusedTextColor = SleekTextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (genState is StudioUiState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (genState as StudioUiState.Error).message,
                            color = SleekError,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    GlowingGradientButton(
                        text = "Generate APK Project with AI",
                        icon = Icons.Default.Bolt,
                        enabled = promptInput.isNotBlank(),
                        onClick = {
                            val config = AiConfiguration(
                                selectedModel = selectedModel,
                                temperature = temperature,
                                themeMood = selectedThemeMood,
                                architecture = selectedArch,
                                targetSdk = targetSdk,
                                customDatasetType = datasetType,
                                customDatasetPayload = datasetPayload
                            )
                            viewModel.generateAppFromPrompt(promptInput, config) { project ->
                                onAppCreated(project)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "generate_app_button"
                    )
                }
            }
        }
    }
}

@Composable
fun PresetTemplateChip(
    template: PromptTemplate,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = SleekSurfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (template.icon) {
                    "calculate" -> Icons.Default.Calculate
                    "check_circle" -> Icons.Default.CheckCircle
                    "newspaper" -> Icons.Default.Newspaper
                    "fitness_center" -> Icons.Default.FitnessCenter
                    "graphic_eq" -> Icons.Default.GraphicEq
                    "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
                    "sports_esports" -> Icons.Default.SportsEsports
                    else -> Icons.Default.AutoFixHigh
                },
                contentDescription = null,
                tint = SleekPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = template.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextPrimary
                )
                Text(
                    text = template.category,
                    fontSize = 10.sp,
                    color = SleekTextSecondary
                )
            }
        }
    }
}

@Composable
fun AiGeneratingVisualizer(statusMessage: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_rot")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ai_rot"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(90.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .rotate(rotation)
                    .border(
                        3.dp,
                        Brush.sweepGradient(listOf(SleekPrimary, SleekPrimaryLight, SleekSuccess, SleekPrimary)),
                        CircleShape
                    )
            )

            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = SleekPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "AI Neural Engine at Work",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SleekTextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = statusMessage,
            fontSize = 13.sp,
            color = SleekTextSecondary,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = SleekPrimary,
            trackColor = SleekSurfaceContainer
        )
    }
}
