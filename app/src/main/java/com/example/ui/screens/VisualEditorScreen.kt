package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppProject
import com.example.data.model.ComponentAction
import com.example.data.model.ComponentType
import com.example.data.model.UiComponent
import com.example.ui.components.ComponentTypeBadge
import com.example.ui.components.DeveloperShellView
import com.example.ui.components.ErrorLogsView
import com.example.ui.components.GlowingGradientButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.EditorTab
import com.example.ui.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualEditorScreen(
    projectId: String,
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    onOpenVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    val project by viewModel.currentProject.collectAsState()
    val components by viewModel.components.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedComponentForEdit by viewModel.selectedComponentForEdit.collectAsState()
    val selectedComponentId by viewModel.selectedComponentId.collectAsState()
    val clipboardComponent by viewModel.clipboardComponent.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val shortcutHudMessage by viewModel.shortcutHudMessage.collectAsState()
    val isBuildingApk by viewModel.isBuildingApk.collectAsState()
    val buildProgress by viewModel.buildProgress.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()
    val diagnosticsLogs by viewModel.diagnosticsLogs.collectAsState()
    val shellHistory by viewModel.shellHistory.collectAsState()

    var showPaletteSheet by remember { mutableStateOf(false) }
    var showProjectSettingsDialog by remember { mutableStateOf(false) }
    var showShortcutsModal by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(shortcutHudMessage) {
        if (shortcutHudMessage != null) {
            kotlinx.coroutines.delay(1800)
            viewModel.clearShortcutHud()
        }
    }

    if (isBuildingApk) {
        ApkBuildModal(
            progress = buildProgress,
            onDismiss = { viewModel.dismissBuildModal() },
            onDownload = { viewModel.downloadLatestApk(context) },
            onInstall = { viewModel.installLatestApk(context) },
            onShare = { viewModel.shareLatestApk(context) }
        )
    }

    if (selectedComponentForEdit != null) {
        ComponentPropertyInspectorSheet(
            component = selectedComponentForEdit!!,
            onDismiss = { viewModel.selectComponentForEdit(null) },
            onSave = { updated -> viewModel.updateComponent(updated) },
            onDelete = { compId -> viewModel.deleteComponent(compId) }
        )
    }

    if (showPaletteSheet) {
        ComponentPaletteSheet(
            onDismiss = { showPaletteSheet = false },
            onSelectType = { type ->
                viewModel.addComponent(type)
                showPaletteSheet = false
            },
            onSelectStoreAsset = { asset ->
                viewModel.addStoreAsset(asset)
                showPaletteSheet = false
            }
        )
    }

    if (showProjectSettingsDialog && project != null) {
        ProjectSettingsDialog(
            project = project!!,
            onDismiss = { showProjectSettingsDialog = false },
            onSave = { name, desc, prim, sec ->
                viewModel.updateProjectDetails(name, desc, prim, sec)
                showProjectSettingsDialog = false
            }
        )
    }

    if (showShortcutsModal) {
        KeyboardShortcutsModal(
            onDismiss = { showShortcutsModal = false },
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onCopy = { viewModel.copySelectedComponent() },
            onPaste = { viewModel.pasteComponent() },
            onDuplicate = { viewModel.duplicateSelectedComponent() },
            onDelete = { viewModel.deleteSelectedComponent() },
            canUndo = canUndo,
            canRedo = canRedo,
            hasClipboard = clipboardComponent != null
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("editor_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = SleekTextPrimary
                        )
                    }
                },
                title = {
                    Column(
                        modifier = Modifier.clickable { showProjectSettingsDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = project?.name ?: "Loading...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit project info",
                                tint = SleekTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "${components.size} UI elements • Tap to configure",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                },
                actions = {
                    // Keyboard shortcuts guide button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showShortcutsModal = true }
                            .padding(end = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = SleekSurfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "Keyboard Shortcuts",
                                tint = SleekPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Shortcuts",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SleekTextPrimary
                            )
                        }
                    }

                    val p = project
                    if (p != null) {
                        // Star button
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleStar() }
                                .padding(end = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (p.isStarred) SleekWarning.copy(alpha = 0.15f) else SleekSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (p.isStarred) SleekWarning else SleekCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (p.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star",
                                    tint = if (p.isStarred) SleekWarning else SleekTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${if (p.starCount > 0) p.starCount else 8}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (p.isStarred) SleekWarning else SleekTextSecondary
                                )
                            }
                        }
                    }

                    GlowingGradientButton(
                        text = "Build APK",
                        icon = Icons.Default.Build,
                        onClick = { viewModel.buildApk(context) },
                        modifier = Modifier
                            .height(36.dp)
                            .padding(end = 8.dp),
                        testTag = "editor_build_apk_btn"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SleekBackground,
                    titleContentColor = SleekTextPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = SleekSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == EditorTab.VISUAL_CANVAS,
                        onClick = { viewModel.setTab(EditorTab.VISUAL_CANVAS) },
                        icon = { Icon(Icons.Default.DashboardCustomize, contentDescription = null) },
                        label = { Text("Canvas", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekPrimary,
                            selectedTextColor = SleekPrimary,
                            unselectedIconColor = SleekTextMuted,
                            unselectedTextColor = SleekTextMuted,
                            indicatorColor = SleekPrimaryContainer
                        ),
                        modifier = Modifier.testTag("tab_canvas")
                    )

                    NavigationBarItem(
                        selected = selectedTab == EditorTab.LIVE_SIMULATOR,
                        onClick = { viewModel.setTab(EditorTab.LIVE_SIMULATOR) },
                        icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                        label = { Text("Simulator", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekPrimary,
                            selectedTextColor = SleekPrimary,
                            unselectedIconColor = SleekTextMuted,
                            unselectedTextColor = SleekTextMuted,
                            indicatorColor = SleekPrimaryContainer
                        ),
                        modifier = Modifier.testTag("tab_simulator")
                    )

                    NavigationBarItem(
                        selected = selectedTab == EditorTab.SOURCE_CODE,
                        onClick = { viewModel.setTab(EditorTab.SOURCE_CODE) },
                        icon = { Icon(Icons.Default.Code, contentDescription = null) },
                        label = { Text("Source", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekPrimary,
                            selectedTextColor = SleekPrimary,
                            unselectedIconColor = SleekTextMuted,
                            unselectedTextColor = SleekTextMuted,
                            indicatorColor = SleekPrimaryContainer
                        ),
                        modifier = Modifier.testTag("tab_source_code")
                    )

                    NavigationBarItem(
                        selected = selectedTab == EditorTab.ERROR_LOGS,
                        onClick = { viewModel.setTab(EditorTab.ERROR_LOGS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    val errCount = diagnosticsLogs.count { it.level == com.example.data.model.LogLevel.ERROR && !it.isResolved }
                                    if (errCount > 0) {
                                        Badge(containerColor = SleekError) {
                                            Text("$errCount", fontSize = 9.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.BugReport, contentDescription = null)
                            }
                        },
                        label = { Text("Logs", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekError,
                            selectedTextColor = SleekError,
                            unselectedIconColor = SleekTextMuted,
                            unselectedTextColor = SleekTextMuted,
                            indicatorColor = SleekError.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_error_logs")
                    )

                    NavigationBarItem(
                        selected = selectedTab == EditorTab.DEVELOPER_SHELL,
                        onClick = { viewModel.setTab(EditorTab.DEVELOPER_SHELL) },
                        icon = { Icon(Icons.Default.Terminal, contentDescription = null) },
                        label = { Text("Shell", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            selectedTextColor = Color(0xFF38BDF8),
                            unselectedIconColor = SleekTextMuted,
                            unselectedTextColor = SleekTextMuted,
                            indicatorColor = Color(0xFF38BDF8).copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_dev_shell")
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == EditorTab.VISUAL_CANVAS) {
                ExtendedFloatingActionButton(
                    onClick = { showPaletteSheet = true },
                    modifier = Modifier.testTag("fab_add_component"),
                    containerColor = SleekPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("+ Add Component", fontWeight = FontWeight.Bold) }
                )
            }
        },
        containerColor = SleekBackground,
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    val isCtrlOrCmd = event.isCtrlPressed || event.isMetaPressed
                    when {
                        // Undo: Ctrl+Z (without Shift)
                        isCtrlOrCmd && !event.isShiftPressed && event.key == Key.Z -> {
                            viewModel.undo()
                            true
                        }
                        // Redo: Ctrl+Shift+Z or Ctrl+Y
                        (isCtrlOrCmd && event.isShiftPressed && event.key == Key.Z) || (isCtrlOrCmd && event.key == Key.Y) -> {
                            viewModel.redo()
                            true
                        }
                        // Copy: Ctrl+C
                        isCtrlOrCmd && event.key == Key.C -> {
                            viewModel.copySelectedComponent()
                            true
                        }
                        // Cut: Ctrl+X
                        isCtrlOrCmd && event.key == Key.X -> {
                            viewModel.cutSelectedComponent()
                            true
                        }
                        // Paste: Ctrl+V
                        isCtrlOrCmd && event.key == Key.V -> {
                            viewModel.pasteComponent()
                            true
                        }
                        // Duplicate: Ctrl+D
                        isCtrlOrCmd && event.key == Key.D -> {
                            viewModel.duplicateSelectedComponent()
                            true
                        }
                        // Delete: Delete or Backspace
                        event.key == Key.Delete || (isCtrlOrCmd && event.key == Key.Backspace) -> {
                            viewModel.deleteSelectedComponent()
                            true
                        }
                        // Move Up: Alt+Up or Ctrl+Up
                        (event.isAltPressed || isCtrlOrCmd) && event.key == Key.DirectionUp -> {
                            viewModel.moveSelectedUp()
                            true
                        }
                        // Move Down: Alt+Down or Ctrl+Down
                        (event.isAltPressed || isCtrlOrCmd) && event.key == Key.DirectionDown -> {
                            viewModel.moveSelectedDown()
                            true
                        }
                        // Navigate selection Up
                        !event.isAltPressed && !isCtrlOrCmd && event.key == Key.DirectionUp -> {
                            viewModel.selectPreviousComponent()
                            true
                        }
                        // Navigate selection Down
                        !event.isAltPressed && !isCtrlOrCmd && event.key == Key.DirectionDown -> {
                            viewModel.selectNextComponent()
                            true
                        }
                        // Add Component: Ctrl+K or Ctrl+P
                        isCtrlOrCmd && (event.key == Key.K || event.key == Key.P) -> {
                            showPaletteSheet = true
                            true
                        }
                        // Build APK: Ctrl+B
                        isCtrlOrCmd && event.key == Key.B -> {
                            viewModel.buildApk(context)
                            true
                        }
                        // Help / Shortcuts: Ctrl+/ or F1
                        (isCtrlOrCmd && event.key == Key.Slash) || event.key == Key.F1 -> {
                            showShortcutsModal = true
                            true
                        }
                        // Edit component: Enter
                        event.key == Key.Enter && selectedComponentForEdit == null -> {
                            val target = components.find { it.id == selectedComponentId }
                            if (target != null) {
                                viewModel.selectComponentForEdit(target)
                                true
                            } else false
                        }
                        // Deselect / Close: Escape
                        event.key == Key.Escape -> {
                            if (showPaletteSheet) {
                                showPaletteSheet = false
                                true
                            } else if (showShortcutsModal) {
                                showShortcutsModal = false
                                true
                            } else if (selectedComponentForEdit != null) {
                                viewModel.selectComponentForEdit(null)
                                true
                            } else {
                                viewModel.selectComponent(null)
                                true
                            }
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                EditorTab.VISUAL_CANVAS -> {
                    VisualCanvasTab(
                        components = components,
                        selectedComponentId = selectedComponentId,
                        clipboardComponent = clipboardComponent,
                        canUndo = canUndo,
                        canRedo = canRedo,
                        onSelect = { id -> viewModel.selectComponent(id) },
                        onMoveUp = { index -> viewModel.moveComponentUp(index) },
                        onMoveDown = { index -> viewModel.moveComponentDown(index) },
                        onSelectForEdit = { comp -> viewModel.selectComponentForEdit(comp) },
                        onDuplicate = { comp -> viewModel.duplicateComponent(comp) },
                        onDelete = { id -> viewModel.deleteComponent(id) },
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        onCopy = { viewModel.copySelectedComponent() },
                        onPaste = { viewModel.pasteComponent() },
                        onOpenShortcuts = { showShortcutsModal = true },
                        onOpenPalette = { showPaletteSheet = true }
                    )
                }
                EditorTab.LIVE_SIMULATOR -> {
                    if (project != null) {
                        LiveSimulatorTab(
                            project = project!!,
                            components = components,
                            viewModel = viewModel
                        )
                    }
                }
                EditorTab.SOURCE_CODE -> {
                    SourceCodeViewerTab(
                        sourceCode = viewModel.getGeneratedKotlinCode(),
                        context = context
                    )
                }
                EditorTab.ERROR_LOGS -> {
                    ErrorLogsView(
                        logs = diagnosticsLogs,
                        onResolveLog = { viewModel.resolveDiagnosticLog(it) },
                        onClearLogs = { viewModel.clearDiagnosticLogs() },
                        onTriggerScan = { viewModel.triggerDiagnosticScan() },
                        onApplyAiFix = { viewModel.applyAiFix(it) }
                    )
                }
                EditorTab.DEVELOPER_SHELL -> {
                    DeveloperShellView(
                        history = shellHistory,
                        onExecuteCommand = { viewModel.executeShellCommand(it) },
                        currentProject = project
                    )
                }
            }

            // Animated Shortcut HUD Flash Overlay
            AnimatedVisibility(
                visible = shortcutHudMessage != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -40 }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.94f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.6f)),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = shortcutHudMessage ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VisualCanvasTab(
    components: List<UiComponent>,
    selectedComponentId: String?,
    clipboardComponent: UiComponent?,
    canUndo: Boolean,
    canRedo: Boolean,
    onSelect: (String?) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onSelectForEdit: (UiComponent) -> Unit,
    onDuplicate: (UiComponent) -> Unit,
    onDelete: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onOpenShortcuts: () -> Unit,
    onOpenPalette: () -> Unit
) {
    val selectedComponent = components.find { it.id == selectedComponentId }

    if (components.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = SleekSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AddBox,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Canvas is Empty", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Tap below to add components or press Ctrl+K!", fontSize = 13.sp, color = SleekTextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                GlowingGradientButton(
                    text = "+ Add Component (Ctrl+K)",
                    icon = Icons.Default.Add,
                    onClick = onOpenPalette
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 84.dp)
        ) {
            // Quick Shortcuts & Actions Command Bar
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = SleekSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Undo Action Pill
                                QuickShortcutActionBtn(
                                    icon = Icons.Default.Undo,
                                    label = "Undo",
                                    shortcut = "Ctrl+Z",
                                    enabled = canUndo,
                                    onClick = onUndo
                                )

                                // Redo Action Pill
                                QuickShortcutActionBtn(
                                    icon = Icons.Default.Redo,
                                    label = "Redo",
                                    shortcut = "Ctrl+Y",
                                    enabled = canRedo,
                                    onClick = onRedo
                                )

                                // Copy Action Pill
                                QuickShortcutActionBtn(
                                    icon = Icons.Default.ContentCopy,
                                    label = "Copy",
                                    shortcut = "Ctrl+C",
                                    enabled = selectedComponentId != null,
                                    onClick = onCopy
                                )

                                // Paste Action Pill
                                QuickShortcutActionBtn(
                                    icon = Icons.Default.ContentPaste,
                                    label = if (clipboardComponent != null) "Paste (1)" else "Paste",
                                    shortcut = "Ctrl+V",
                                    enabled = clipboardComponent != null || selectedComponentId != null,
                                    onClick = onPaste
                                )
                            }

                            // Shortcuts Guide Trigger
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(onClick = onOpenShortcuts),
                                color = SleekSurfaceContainer,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Keyboard,
                                        contentDescription = null,
                                        tint = SleekPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Guide", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                                }
                            }
                        }

                        if (selectedComponent != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = SleekPrimary.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Selected: ", fontSize = 11.sp, color = SleekTextMuted)
                                        Text(
                                            selectedComponent.title.take(22),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekPrimary
                                        )
                                    }
                                    Text(
                                        "Ctrl+C (Copy) • Del (Remove) • Alt+↑↓ (Reorder)",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = SleekTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            itemsIndexed(components, key = { _, item -> item.id }) { index, component ->
                CanvasComponentItemCard(
                    component = component,
                    index = index,
                    totalCount = components.size,
                    isSelected = component.id == selectedComponentId,
                    onSelect = { onSelect(if (component.id == selectedComponentId) null else component.id) },
                    onMoveUp = { onMoveUp(index) },
                    onMoveDown = { onMoveDown(index) },
                    onEdit = { onSelectForEdit(component) },
                    onDuplicate = { onDuplicate(component) },
                    onDelete = { onDelete(component.id) }
                )
            }
        }
    }
}

@Composable
fun QuickShortcutActionBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    shortcut: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) SleekSurfaceContainer else SleekBackground.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (enabled) SleekCardBorder else SleekCardBorder.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) SleekPrimary else SleekTextMuted.copy(alpha = 0.4f),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) SleekTextPrimary else SleekTextMuted.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun CanvasComponentItemCard(
    component: UiComponent,
    index: Int,
    totalCount: Int,
    isSelected: Boolean = false,
    onSelect: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) SleekPrimary else SleekCardBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .testTag("canvas_item_${component.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SleekSurface.copy(alpha = 0.95f) else SleekSurface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with badge, selected tag, and order controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ComponentTypeBadge(type = component.type)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "#${index + 1}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SleekTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SleekPrimary.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Reorder & Action Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            tint = if (index > 0) SleekPrimary else SleekCardBorder,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = if (index < totalCount - 1) SleekPrimary else SleekCardBorder,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate",
                            tint = SleekTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit properties",
                            tint = SleekPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = SleekError,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Label and preview
            Text(
                text = component.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )

            if (component.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = component.subtitle,
                    fontSize = 12.sp,
                    color = SleekTextSecondary
                )
            }

            if (component.stateValue.isNotBlank() || component.actionType != ComponentAction.NONE) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (component.stateValue.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SleekSuccessContainer
                        ) {
                            Text(
                                text = "Value: ${component.stateValue}",
                                fontSize = 11.sp,
                                color = SleekSuccess,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (component.actionType != ComponentAction.NONE) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SleekPrimaryContainer
                        ) {
                            Text(
                                text = "Action: ${component.actionType.name}",
                                fontSize = 10.sp,
                                color = SleekPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentPropertyInspectorSheet(
    component: UiComponent,
    onDismiss: () -> Unit,
    onSave: (UiComponent) -> Unit,
    onDelete: (String) -> Unit
) {
    var title by remember { mutableStateOf(component.title) }
    var subtitle by remember { mutableStateOf(component.subtitle) }
    var stateValue by remember { mutableStateOf(component.stateValue) }
    var placeholder by remember { mutableStateOf(component.placeholder) }
    var actionType by remember { mutableStateOf(component.actionType) }
    var actionPayload by remember { mutableStateOf(component.actionPayload) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SleekSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = SleekCardBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ComponentTypeBadge(type = component.type)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Component Properties", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                }

                IconButton(onClick = { onDelete(component.id) }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = SleekError)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title / Label") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekCardBorder,
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary,
                    focusedContainerColor = SleekSurfaceLow,
                    unfocusedContainerColor = SleekSurfaceLow
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle Field
            OutlinedTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                label = { Text("Subtitle / Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekCardBorder,
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary,
                    focusedContainerColor = SleekSurfaceLow,
                    unfocusedContainerColor = SleekSurfaceLow
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Initial Value Field
            OutlinedTextField(
                value = stateValue,
                onValueChange = { stateValue = it },
                label = { Text("Default State / Initial Value") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekCardBorder,
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary,
                    focusedContainerColor = SleekSurfaceLow,
                    unfocusedContainerColor = SleekSurfaceLow
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Selection
            Text("Click / Tap Action Trigger:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SleekTextSecondary)
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ComponentAction.entries) { action ->
                    FilterChip(
                        selected = actionType == action,
                        onClick = { actionType = action },
                        label = { Text(action.name.replace("_", " "), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SleekPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = SleekSurfaceContainer,
                            labelColor = SleekTextSecondary
                        )
                    )
                }
            }

            if (actionType != ComponentAction.NONE) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = actionPayload,
                    onValueChange = { actionPayload = it },
                    label = { Text("Action Payload (e.g. Toast message, Alert text)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekCardBorder,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary,
                        focusedContainerColor = SleekSurfaceLow,
                        unfocusedContainerColor = SleekSurfaceLow
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            GlowingGradientButton(
                text = "Save Component Properties",
                icon = Icons.Default.Check,
                onClick = {
                    onSave(
                        component.copy(
                            title = title,
                            subtitle = subtitle,
                            stateValue = stateValue,
                            placeholder = placeholder,
                            actionType = actionType,
                            actionPayload = actionPayload
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "save_component_props_btn"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentPaletteSheet(
    onDismiss: () -> Unit,
    onSelectType: (ComponentType) -> Unit,
    onSelectStoreAsset: (com.example.data.model.StoreAsset) -> Unit
) {
    var selectedPaletteCategory by remember { mutableStateOf(0) } // 0: Basic Components, 1: Store Modules & Widgets

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SleekSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = SleekCardBorder) }
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
                    Icon(Icons.Default.Widgets, contentDescription = null, tint = SleekPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Component to Canvas",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Tab Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPaletteCategory == 0,
                    onClick = { selectedPaletteCategory = 0 },
                    label = { Text("Standard Elements", fontSize = 12.sp, fontWeight = if (selectedPaletteCategory == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SleekPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = SleekSurfaceContainer,
                        labelColor = SleekTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                FilterChip(
                    selected = selectedPaletteCategory == 1,
                    onClick = { selectedPaletteCategory = 1 },
                    label = { Text("Asset Store Widgets", fontSize = 12.sp, fontWeight = if (selectedPaletteCategory == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SleekPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = SleekSurfaceContainer,
                        labelColor = SleekTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedPaletteCategory == 0) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 420.dp)
                ) {
                    items(ComponentType.entries) { type ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectType(type) }
                                .testTag("palette_select_${type.name}"),
                            shape = RoundedCornerShape(12.dp),
                            color = SleekSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ComponentTypeBadge(type = type)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = when (type) {
                                            ComponentType.HEADER -> "Header & Title Section"
                                            ComponentType.BUTTON -> "Action Button"
                                            ComponentType.COUNTER_WIDGET -> "Interactive Counter (+/-)"
                                            ComponentType.SWITCH -> "Toggle Switch"
                                            ComponentType.SLIDER -> "Value Slider (0-100)"
                                            ComponentType.PROGRESS_BAR -> "Progress Bar"
                                            ComponentType.INPUT_FIELD -> "Text Input Field"
                                            ComponentType.METRIC_STAT -> "Metric Stat Card"
                                            ComponentType.CARD -> "Content Container Card"
                                            ComponentType.ACTION_CHIP -> "Filter Chip / Tag"
                                            ComponentType.IMAGE_BANNER -> "Image Banner"
                                            ComponentType.RATING_BAR -> "Rating Stars"
                                            ComponentType.BADGE -> "Status Badge"
                                            ComponentType.DIVIDER -> "Visual Divider"
                                            ComponentType.LIST_VIEW -> "List Item Record"
                                            ComponentType.TEXT -> "Text Paragraph"
                                        },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SleekTextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 420.dp)
                ) {
                    items(com.example.data.store.AssetStoreData.STORE_ASSETS) { asset ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectStoreAsset(asset) }
                                .testTag("palette_store_asset_${asset.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = SleekSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (asset.isPremium) SleekWarning.copy(alpha = 0.2f) else SleekPrimaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (asset.isPremium) Icons.Default.WorkspacePremium else Icons.Default.Widgets,
                                            contentDescription = null,
                                            tint = if (asset.isPremium) SleekWarning else SleekPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = asset.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (asset.isPremium) {
                                            Text("PRO", fontSize = 9.sp, color = SleekWarning, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = asset.subtitle,
                                        fontSize = 11.sp,
                                        color = SleekTextSecondary,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Add, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveSimulatorTab(
    project: AppProject,
    components: List<UiComponent>,
    viewModel: EditorViewModel
) {
    val context = LocalContext.current
    var counterVal by remember { mutableIntStateOf(10) }
    var switchVal by remember { mutableStateOf(true) }
    var sliderVal by remember { mutableFloatStateOf(65f) }
    var textVal by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SleekSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = SleekSuccess, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Interactive Simulator: Test buttons, switches & counters live!", fontSize = 12.sp, color = SleekTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Smartphone Frame
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(0.96f)
                .border(2.dp, SleekCardBorder, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            color = Color(android.graphics.Color.parseColor(project.backgroundColorHex))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Smartphone Top Notch Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("9:41", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Box(
                        modifier = Modifier
                            .size(width = 60.dp, height = 14.dp)
                            .background(Color.Black, RoundedCornerShape(7.dp))
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Icon(Icons.Default.BatteryFull, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }

                // Simulated App TopBar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = project.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }

                // Simulated Interactive Component Tree
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(components) { comp ->
                        when (comp.type) {
                            ComponentType.HEADER -> {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(comp.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    if (comp.subtitle.isNotBlank()) {
                                        Text(comp.subtitle, fontSize = 13.sp, color = Color.LightGray)
                                    }
                                }
                            }
                            ComponentType.METRIC_STAT -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(comp.title, fontSize = 12.sp, color = Color.LightGray)
                                        Text(
                                            text = comp.stateValue.ifBlank { "42" },
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(android.graphics.Color.parseColor(project.primaryColorHex))
                                        )
                                        if (comp.subtitle.isNotBlank()) {
                                            Text(comp.subtitle, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                            ComponentType.COUNTER_WIDGET -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(14.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(comp.title, fontWeight = FontWeight.SemiBold, color = Color.White)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            FilledTonalButton(
                                                onClick = { if (counterVal > 0) counterVal-- },
                                                contentPadding = PaddingValues(horizontal = 12.dp)
                                            ) { Text("-", fontSize = 16.sp) }
                                            Text(
                                                text = "$counterVal",
                                                modifier = Modifier.padding(horizontal = 14.dp),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            FilledTonalButton(
                                                onClick = { counterVal++ },
                                                contentPadding = PaddingValues(horizontal = 12.dp)
                                            ) { Text("+", fontSize = 16.sp) }
                                        }
                                    }
                                }
                            }
                            ComponentType.BUTTON -> {
                                Button(
                                    onClick = {
                                        val msg = comp.actionPayload.ifBlank { "Action triggered: ${comp.title}" }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (comp.actionType == ComponentAction.INCREMENT_COUNTER) {
                                            counterVal += 5
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(android.graphics.Color.parseColor(project.primaryColorHex))
                                    )
                                ) {
                                    Text(comp.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            ComponentType.SWITCH -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(14.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(comp.title, color = Color.White, fontSize = 14.sp)
                                        Switch(
                                            checked = switchVal,
                                            onCheckedChange = { switchVal = it }
                                        )
                                    }
                                }
                            }
                            ComponentType.SLIDER -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("${comp.title}: ${sliderVal.toInt()}%", color = Color.White, fontSize = 13.sp)
                                        Slider(
                                            value = sliderVal,
                                            onValueChange = { sliderVal = it },
                                            valueRange = 0f..100f
                                        )
                                    }
                                }
                            }
                            ComponentType.PROGRESS_BAR -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(comp.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = { 0.72f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = Color(android.graphics.Color.parseColor(project.secondaryColorHex))
                                        )
                                        if (comp.subtitle.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(comp.subtitle, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                            ComponentType.INPUT_FIELD -> {
                                OutlinedTextField(
                                    value = textVal,
                                    onValueChange = { textVal = it },
                                    label = { Text(comp.title) },
                                    placeholder = { Text(comp.placeholder.ifBlank { "Type input..." }) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(android.graphics.Color.parseColor(project.primaryColorHex)),
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                            }
                            else -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(comp.title, fontWeight = FontWeight.Bold, color = Color.White)
                                        if (comp.subtitle.isNotBlank()) {
                                            Text(comp.subtitle, fontSize = 12.sp, color = Color.LightGray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SourceCodeViewerTab(
    sourceCode: String,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Generated Android Kotlin Code", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                Text("Jetpack Compose 1.7.5 • Ready to build", fontSize = 12.sp, color = SleekTextSecondary)
            }

            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Generated Kotlin Code", sourceCode)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Kotlin code copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimaryContainer, contentColor = SleekPrimary)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, SleekCodeBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SleekCodeBackground)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                item {
                    Text(
                        text = sourceCode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = SleekCodeText,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectSettingsDialog(
    project: AppProject,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, primaryColor: String, secondaryColor: String) -> Unit
) {
    var name by remember { mutableStateOf(project.name) }
    var description by remember { mutableStateOf(project.description) }
    var primaryColor by remember { mutableStateOf(project.primaryColorHex) }
    var secondaryColor by remember { mutableStateOf(project.secondaryColorHex) }

    val colorPresets = listOf("#6366F1", "#06B6D4", "#10B981", "#F59E0B", "#EC4899", "#8B5CF6", "#EF4444")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SleekSurface,
        title = { Text("App Project Settings", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("App Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary,
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekCardBorder
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("App Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary,
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekCardBorder
                    )
                )

                Text("Theme Color Accent:", fontSize = 12.sp, color = SleekTextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colorPresets) { hex ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    if (primaryColor == hex) 3.dp else 0.dp,
                                    SleekPrimary,
                                    CircleShape
                                )
                                .clickable { primaryColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, description, primaryColor, secondaryColor) },
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
            ) {
                Text("Save Changes", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SleekTextMuted) }
        }
    )
}

@Composable
fun KeyboardShortcutsModal(
    onDismiss: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    hasClipboard: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SleekSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = SleekPrimaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Keyboard Shortcuts",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Visual canvas hotkeys & productivity triggers",
                        fontSize = 11.sp,
                        color = SleekTextSecondary
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: History
                item {
                    ShortcutCategoryHeader(title = "History & Undo", icon = Icons.Default.History)
                }
                item {
                    ShortcutRowItem(
                        title = "Undo",
                        subtitle = "Revert last canvas change",
                        keys = listOf("Ctrl", "Z"),
                        onAction = onUndo,
                        actionLabel = "Undo",
                        actionEnabled = canUndo
                    )
                }
                item {
                    ShortcutRowItem(
                        title = "Redo",
                        subtitle = "Reapply undone canvas change",
                        keys = listOf("Ctrl", "Y"),
                        onAction = onRedo,
                        actionLabel = "Redo",
                        actionEnabled = canRedo
                    )
                }

                // Section 2: Clipboard & Duplication
                item {
                    ShortcutCategoryHeader(title = "Clipboard & Duplication", icon = Icons.Default.ContentCopy)
                }
                item {
                    ShortcutRowItem(
                        title = "Copy Selected",
                        subtitle = "Copy active UI element to clipboard",
                        keys = listOf("Ctrl", "C"),
                        onAction = onCopy,
                        actionLabel = "Copy"
                    )
                }
                item {
                    ShortcutRowItem(
                        title = "Cut Selected",
                        subtitle = "Copy and remove active UI element",
                        keys = listOf("Ctrl", "X")
                    )
                }
                item {
                    ShortcutRowItem(
                        title = "Paste Element",
                        subtitle = if (hasClipboard) "Paste component from clipboard" else "Paste copied element",
                        keys = listOf("Ctrl", "V"),
                        onAction = onPaste,
                        actionLabel = "Paste",
                        actionEnabled = hasClipboard
                    )
                }
                item {
                    ShortcutRowItem(
                        title = "Duplicate Element",
                        subtitle = "Clone selected component directly below",
                        keys = listOf("Ctrl", "D"),
                        onAction = onDuplicate,
                        actionLabel = "Duplicate"
                    )
                }

                // Section 3: Canvas Reordering & Navigation
                item {
                    ShortcutCategoryHeader(title = "Canvas & Ordering", icon = Icons.Default.SwapVert)
                }
                item {
                    ShortcutRowItem(
                        title = "Move Up",
                        subtitle = "Shift element higher on canvas",
                        keys = listOf("Alt", "↑")
                    )
                }
                item {
                    ShortcutRowItem(
                        title = "Move Down",
                        subtitle = "Shift element lower on canvas",
                        keys = listOf("Alt", "↓")
                    )
                }
                item {
                    ShortcutRowItem(
                        title = "Delete Element",
                        subtitle = "Remove selected component",
                        keys = listOf("Del"),
                        onAction = onDelete,
                        actionLabel = "Delete"
                    )
                }
                item {
                    ShortcutRowItem(
                        title = "Select Next / Prev",
                        subtitle = "Navigate component selection",
                        keys = listOf("↑", "↓")
                    )
                }
                item {
                    ShortcutRowItem(
                        title = "Edit Properties",
                        subtitle = "Open inspector for active element",
                        keys = listOf("Enter")
                    )
                }
                item {
                    ShortcutRowItem(
                        title = "Deselect / Close",
                        subtitle = "Clear selection or close modals",
                        keys = listOf("Esc")
                    )
                }

                // Section 4: Fast Tools
                item {
                    ShortcutCategoryHeader(title = "Fast Tools & Build", icon = Icons.Default.Bolt)
                }
                item {
                    ShortcutRowItem(
                        title = "Add Component",
                        subtitle = "Open component palette",
                        keys = listOf("Ctrl", "K")
                    )
                }
                item {
                    ShortcutRowItem(
                        title = "Build & Compile APK",
                        subtitle = "Trigger background compiler",
                        keys = listOf("Ctrl", "B")
                    )
                }
                item {
                    ShortcutRowItem(
                        title = "Shortcuts Cheatsheet",
                        subtitle = "Open this shortcuts cheatsheet",
                        keys = listOf("Ctrl", "/")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Got It", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ShortcutCategoryHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SleekPrimary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SleekPrimary
        )
    }
}

@Composable
fun ShortcutRowItem(
    title: String,
    subtitle: String,
    keys: List<String>,
    onAction: (() -> Unit)? = null,
    actionLabel: String? = null,
    actionEnabled: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = SleekSurfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = SleekTextSecondary,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                keys.forEachIndexed { idx, key ->
                    ShortcutKeyCap(text = key)
                    if (idx < keys.size - 1) {
                        Text("+", fontSize = 10.sp, color = SleekTextMuted, fontWeight = FontWeight.Bold)
                    }
                }

                if (onAction != null && actionLabel != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = actionEnabled, onClick = onAction),
                        color = if (actionEnabled) SleekPrimaryContainer else SleekBackground,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (actionEnabled) SleekPrimary.copy(alpha = 0.3f) else SleekCardBorder
                        )
                    ) {
                        Text(
                            text = actionLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (actionEnabled) SleekPrimary else SleekTextMuted,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShortcutKeyCap(text: String) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
