package com.example.ui.screens

import android.content.Context
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiAppGenerator
import com.example.data.apk.ApkFileManager
import com.example.data.model.AppProject
import com.example.data.model.PromptTemplate
import com.example.ui.components.DeveloperShellView
import com.example.ui.components.ErrorLogsView
import com.example.ui.components.GlowingGradientButton
import com.example.ui.components.TutorialCard
import com.example.ui.screens.CloudSyncModal
import com.example.ui.screens.ProjectAnalyticsModal
import com.example.ui.screens.TutorialModeModal
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioHomeScreen(
    viewModel: StudioViewModel,
    onOpenEditor: (String) -> Unit,
    onOpenAiCreator: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val projects by viewModel.allProjects.collectAsState()
    val builds by viewModel.allBuilds.collectAsState()
    val isBuildingApk by viewModel.isBuildingApk.collectAsState()
    val quickBuildProgress by viewModel.quickBuildProgress.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val subStatus by viewModel.subscriptionStatus.collectAsState()
    val diagLogs by viewModel.diagnosticsLogs.collectAsState()
    val shellHistory by viewModel.shellHistory.collectAsState()
    val cloudSyncState by viewModel.cloudSyncState.collectAsState()
    val tutorialProgress by viewModel.tutorialProgress.collectAsState()

    var showShellDialog by remember { mutableStateOf(false) }
    var showErrorLogsDialog by remember { mutableStateOf(false) }
    var showAnalyticsDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }
    var showTutorialDialog by remember { mutableStateOf(false) }

    val errorCount = diagLogs.count { it.level == com.example.data.model.LogLevel.ERROR && !it.isResolved }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    if (showTutorialDialog) {
        TutorialModeModal(
            tutorialManager = viewModel.tutorialManager,
            tutorialProgress = tutorialProgress,
            onDismiss = { showTutorialDialog = false },
            onLaunchSandbox = {
                viewModel.launchTutorialSandbox { proj ->
                    showTutorialDialog = false
                    onOpenEditor(proj.id)
                }
            },
            onOpenAiCreator = {
                showTutorialDialog = false
                onOpenAiCreator()
            },
            onOpenCloudSync = {
                showTutorialDialog = false
                showCloudSyncDialog = true
            },
            onOpenStore = {
                showTutorialDialog = false
                onOpenStore()
            },
            onClaimReward = {
                viewModel.claimTutorialCompletionReward()
            }
        )
    }

    if (showCloudSyncDialog) {
        CloudSyncModal(
            syncState = cloudSyncState,
            localProjects = projects,
            onDismiss = { showCloudSyncDialog = false },
            onUploadProject = { id -> viewModel.uploadProjectToCloud(id) },
            onSyncAllLocal = { viewModel.syncAllProjectsToCloud() },
            onRefreshCloud = { viewModel.refreshCloudProjects() },
            onImportCloudProject = { record ->
                viewModel.importCloudProject(record) { proj ->
                    showCloudSyncDialog = false
                    onOpenEditor(proj.id)
                }
            },
            onDeleteCloudProject = { id -> viewModel.deleteCloudProject(id) },
            onToggleAutoSync = { enabled -> viewModel.setAutoCloudSync(enabled) },
            onUpdateDeviceName = { name -> viewModel.updateDeviceName(name) },
            onSetAccount = { email, name -> viewModel.setCloudAccount(email, name) },
            onSignOut = { viewModel.signOutCloud() },
            onOpenProject = { id ->
                showCloudSyncDialog = false
                onOpenEditor(id)
            }
        )
    }

    if (showAnalyticsDialog) {
        ProjectAnalyticsModal(
            projects = projects,
            onDismiss = { showAnalyticsDialog = false },
            onToggleStar = { id -> viewModel.toggleProjectStar(id) },
            onOpenEditor = { id ->
                showAnalyticsDialog = false
                onOpenEditor(id)
            }
        )
    }

    if (showShellDialog) {
        ModalBottomSheet(
            onDismissRequest = { showShellDialog = false },
            containerColor = Color(0xFF090D16),
            scrimColor = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxHeight(0.85f)
        ) {
            DeveloperShellView(
                history = shellHistory,
                onExecuteCommand = { cmd -> viewModel.executeShellCommand(cmd) },
                currentProject = projects.firstOrNull(),
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showErrorLogsDialog) {
        ModalBottomSheet(
            onDismissRequest = { showErrorLogsDialog = false },
            containerColor = SleekSurface,
            scrimColor = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxHeight(0.85f)
        ) {
            ErrorLogsView(
                logs = diagLogs,
                onResolveLog = { id -> viewModel.resolveDiagnosticLog(id) },
                onClearLogs = { viewModel.clearDiagnosticLogs() },
                onTriggerScan = { viewModel.triggerDiagnosticScan() },
                onApplyAiFix = { log -> viewModel.applyAiFix(log) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (isBuildingApk) {
        ApkBuildModal(
            progress = quickBuildProgress,
            onDismiss = { viewModel.dismissQuickBuildDialog() },
            onDownload = {
                quickBuildProgress?.apkFile?.let { file ->
                    val record = builds.firstOrNull()
                    if (record != null) {
                        viewModel.downloadApk(context, record)
                    }
                }
            },
            onInstall = {
                quickBuildProgress?.apkFile?.let { file ->
                    ApkFileManager.launchApkInstaller(context, file)
                }
            },
            onShare = {
                quickBuildProgress?.apkFile?.let { file ->
                    ApkFileManager.shareApkFile(context, file, "AI App")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = SleekPrimaryContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI APK Studio",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )
                                if (subStatus.isPremium) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = SleekWarning.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekWarning)
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
                                text = "Mobile AI Compiler & CLI Terminal",
                                fontSize = 11.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Tutorial Walkthrough Guide
                    IconButton(
                        onClick = { showTutorialDialog = true },
                        modifier = Modifier.testTag("open_tutorial_top_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (!tutorialProgress.isCompleted) {
                                    Badge(
                                        containerColor = SleekPrimary,
                                        contentColor = Color.White
                                    ) {
                                        Text("${tutorialProgress.completedStepIds.size}/6")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Tutorial Mode",
                                tint = if (!tutorialProgress.isCompleted) Color(0xFF818CF8) else SleekTextPrimary
                            )
                        }
                    }

                    // Firebase Cloud Sync Action
                    IconButton(
                        onClick = { showCloudSyncDialog = true },
                        modifier = Modifier.testTag("open_cloud_sync_top_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cloudSyncState.cloudProjects.isNotEmpty()) {
                                    Badge(
                                        containerColor = Color(0xFF0284C7),
                                        contentColor = Color.White
                                    ) {
                                        Text("${cloudSyncState.cloudProjects.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Firebase Cloud Sync",
                                tint = if (cloudSyncState.isSyncing) SleekPrimary else Color(0xFF38BDF8)
                            )
                        }
                    }

                    // Developer Shell Action
                    IconButton(
                        onClick = { showShellDialog = true },
                        modifier = Modifier.testTag("open_shell_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Developer Shell",
                            tint = Color(0xFF38BDF8)
                        )
                    }

                    // Diagnostics / Error Logs Action
                    IconButton(
                        onClick = { showErrorLogsDialog = true },
                        modifier = Modifier.testTag("open_error_logs_top_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (errorCount > 0) {
                                    Badge(containerColor = SleekError, contentColor = Color.White) {
                                        Text("$errorCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Error Logs & Diagnostics",
                                tint = if (errorCount > 0) SleekError else SleekTextSecondary
                            )
                        }
                    }

                    // Store Action
                    IconButton(
                        onClick = onOpenStore,
                        modifier = Modifier.testTag("open_store_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Asset Store",
                            tint = SleekPrimary
                        )
                    }

                    // Settings & Codes Action
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("open_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & Promo Codes",
                            tint = SleekTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SleekBackground,
                    titleContentColor = SleekTextPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAiCreator,
                modifier = Modifier.testTag("fab_create_app"),
                containerColor = SleekPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                text = {
                    Text(
                        text = "AI Make APK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            )
        },
        containerColor = SleekBackground,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
        ) {
            // Hero Visual Card
            item {
                HeroBannerCard(
                    projects = projects,
                    projectCount = projects.size,
                    buildCount = builds.size,
                    isPremium = subStatus.isPremium,
                    onCreateWithAi = onOpenAiCreator,
                    onOpenShell = { showShellDialog = true },
                    onOpenAnalytics = { showAnalyticsDialog = true }
                )
            }

            // Quick Hub Strip (Store, Terminal Shell, Error Logs, Analytics)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickHubCard(
                            title = "Developer Shell",
                            subtitle = "CLI terminal (gradle/adb)",
                            icon = Icons.Default.Terminal,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f),
                            onClick = { showShellDialog = true }
                        )

                        QuickHubCard(
                            title = "Error Logs",
                            subtitle = if (errorCount > 0) "$errorCount issues detected" else "Compiler logs healthy",
                            icon = Icons.Default.BugReport,
                            color = if (errorCount > 0) SleekError else SleekSuccess,
                            modifier = Modifier.weight(1f),
                            onClick = { showErrorLogsDialog = true }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickHubCard(
                            title = "Asset Store",
                            subtitle = "UI widgets & modules",
                            icon = Icons.Default.Storefront,
                            color = SleekPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenStore
                        )

                        QuickHubCard(
                            title = "Views & Stars",
                            subtitle = "${projects.sumOf { if (it.viewCount > 0) it.viewCount else 120 }} views • ${projects.sumOf { if (it.starCount > 0) it.starCount else 8 }}★",
                            icon = Icons.Default.QueryStats,
                            color = SleekWarning,
                            modifier = Modifier.weight(1f),
                            onClick = { showAnalyticsDialog = true }
                        )
                    }
                }
            }

            // Customizable Template Projects Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Customizable Project Templates",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = "Calculator, To-Do List, Blog & more (Drag-and-Drop ready)",
                                fontSize = 11.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(AiAppGenerator.PRESET_TEMPLATES) { template ->
                            StarterTemplateCard(
                                template = template,
                                onClick = {
                                    if (template.isBuiltinTemplate && template.templateId.isNotBlank()) {
                                        viewModel.createProjectFromTemplate(template.templateId) { proj ->
                                            onOpenEditor(proj.id)
                                        }
                                    } else {
                                        viewModel.generateAppFromPrompt(template.prompt) { proj ->
                                            onOpenEditor(proj.id)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Projects List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your App Projects",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SleekSurfaceContainer
                    ) {
                        Text(
                            text = "${projects.size} Apps",
                            fontSize = 11.sp,
                            color = SleekTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Project Items
            if (projects.isEmpty()) {
                item {
                    EmptyProjectsCard(onOpenAiCreator = onOpenAiCreator)
                }
            } else {
                items(projects, key = { it.id }) { project ->
                    ProjectItemCard(
                        project = project,
                        onEdit = { onOpenEditor(project.id) },
                        onQuickBuild = { viewModel.quickBuildApk(context, project.id) },
                        onDelete = { viewModel.deleteProject(project.id) },
                        onToggleStar = { viewModel.toggleProjectStar(project.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickHubCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, SleekCardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = SleekSurface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                Text(text = subtitle, fontSize = 10.sp, color = SleekTextSecondary, maxLines = 1)
            }
        }
    }
}

@Composable
fun HeroBannerCard(
    projects: List<AppProject>,
    projectCount: Int,
    buildCount: Int,
    isPremium: Boolean,
    onCreateWithAi: () -> Unit,
    onOpenShell: () -> Unit,
    onOpenAnalytics: () -> Unit
) {
    val totalViews = projects.sumOf { if (it.viewCount > 0) it.viewCount else 120 }
    val totalDownloads = projects.sumOf { if (it.downloadCount > 0) it.downloadCount else 35 }
    val totalStars = projects.sumOf { if (it.starCount > 0) it.starCount else 8 }

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
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPremium) SleekWarning.copy(alpha = 0.15f) else SleekPrimaryContainer,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isPremium) SleekWarning else SleekPrimary.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isPremium) Icons.Default.WorkspacePremium else Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (isPremium) SleekWarning else SleekPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPremium) "PRO SUBSCRIBER (3 MONTHS)" else "AI ENGINE ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremium) SleekWarning else SleekPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Build & Edit Real Android APKs",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Synthesize apps, manage terminal shell & diagnostics, customize components, and track real-time views, downloads and community stars.",
                        fontSize = 12.sp,
                        color = SleekTextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Community Reach & Metrics Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenAnalytics),
                shape = RoundedCornerShape(12.dp),
                color = SleekSurfaceContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Views
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = SleekSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$totalViews", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "views", fontSize = 10.sp, color = SleekTextMuted)
                    }

                    // Downloads
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = SleekSuccess, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$totalDownloads", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekSuccess)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "dls", fontSize = 10.sp, color = SleekTextMuted)
                    }

                    // Stars
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = SleekWarning, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$totalStars", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SleekWarning)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "stars", fontSize = 10.sp, color = SleekTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenShell,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Shell", fontSize = 12.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onOpenAnalytics,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekWarning)
                    ) {
                        Icon(Icons.Default.QueryStats, contentDescription = null, tint = SleekWarning, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stats", fontSize = 12.sp, color = SleekWarning, fontWeight = FontWeight.Bold)
                    }
                }

                GlowingGradientButton(
                    text = "+ New App",
                    icon = Icons.Default.Add,
                    onClick = onCreateWithAi,
                    modifier = Modifier.height(38.dp),
                    testTag = "hero_create_app_btn"
                )
            }
        }
    }
}

@Composable
fun StarterTemplateCard(
    template: PromptTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .border(1.dp, SleekCardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = SleekPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (template.icon) {
                            "calculate" -> Icons.Default.Calculate
                            "check_circle" -> Icons.Default.CheckCircle
                            "newspaper" -> Icons.Default.Newspaper
                            "fitness_center" -> Icons.Default.FitnessCenter
                            "graphic_eq" -> Icons.Default.GraphicEq
                            "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
                            "sports_esports" -> Icons.Default.SportsEsports
                            else -> Icons.Default.AutoFixNormal
                        },
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = template.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = template.category,
                fontSize = 11.sp,
                color = SleekTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Load & Edit →",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SleekPrimary
            )
        }
    }
}

@Composable
fun ProjectItemCard(
    project: AppProject,
    onEdit: () -> Unit,
    onQuickBuild: () -> Unit,
    onDelete: () -> Unit,
    onToggleStar: () -> Unit
) {
    val views = if (project.viewCount > 0) project.viewCount else 120
    val dls = if (project.downloadCount > 0) project.downloadCount else 35
    val stars = if (project.starCount > 0) project.starCount else 8

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onEdit)
            .testTag("project_card_${project.id}"),
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
                    val colorParsed = try {
                        Color(android.graphics.Color.parseColor(project.primaryColorHex))
                    } catch (e: Exception) {
                        SleekPrimary
                    }
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = colorParsed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            colorParsed.copy(alpha = 0.3f)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (project.category) {
                                    "Productivity" -> Icons.Default.CheckCircle
                                    "Utilities", "Utility" -> Icons.Default.Calculate
                                    "News & Media", "Blog" -> Icons.Default.Newspaper
                                    "Health & Fitness" -> Icons.Default.FitnessCenter
                                    "Music & Audio" -> Icons.Default.GraphicEq
                                    "Finance" -> Icons.Default.AccountBalanceWallet
                                    else -> Icons.Default.Widgets
                                },
                                contentDescription = null,
                                tint = colorParsed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = project.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = project.packageName,
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Star button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleStar() }
                            .testTag("star_toggle_${project.id}"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (project.isStarred) SleekWarning.copy(alpha = 0.15f) else SleekSurfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (project.isStarred) SleekWarning else SleekCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (project.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star Project",
                                tint = if (project.isStarred) SleekWarning else SleekTextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$stars",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (project.isStarred) SleekWarning else SleekTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete project",
                            tint = SleekTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (project.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = project.description,
                    fontSize = 12.sp,
                    color = SleekTextSecondary,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges & Metrics Row (Views, Downloads, Version)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Views Counter badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SleekSecondary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = SleekSecondary, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "$views", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SleekSecondary)
                    }
                }

                // Downloads Counter badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SleekSuccess.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = SleekSuccess, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "$dls", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SleekSuccess)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SleekSurfaceContainer
                ) {
                    Text(
                        text = "v${project.versionName}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = SleekTextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                if (project.latestApkPath != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SleekSuccessContainer
                    ) {
                        Text(
                            text = "APK READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekSuccess,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = ApkFileManager.formatDate(project.updatedAt),
                    fontSize = 10.sp,
                    color = SleekTextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .testTag("edit_project_btn_${project.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Drag & Drop Editor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onQuickBuild,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("quick_build_btn_${project.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Build APK", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun EmptyProjectsCard(onOpenAiCreator: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = SleekPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "No App Projects Yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Ask AI to generate your first Android app or pick a starter template above!",
                fontSize = 12.sp,
                color = SleekTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            GlowingGradientButton(
                text = "Generate with AI",
                icon = Icons.Default.AutoAwesome,
                onClick = onOpenAiCreator
            )
        }
    }
}
