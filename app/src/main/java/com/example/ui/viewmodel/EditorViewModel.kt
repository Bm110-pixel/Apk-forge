package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiAppGenerator
import com.example.data.apk.ApkBuildProgress
import com.example.data.apk.ApkFileManager
import com.example.data.db.AppDatabase
import com.example.data.model.AppProject
import com.example.data.model.ComponentAction
import com.example.data.model.ComponentType
import com.example.data.model.UiComponent
import com.example.data.repository.AppProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class EditorTab {
    VISUAL_CANVAS,
    LIVE_SIMULATOR,
    SOURCE_CODE,
    ERROR_LOGS,
    DEVELOPER_SHELL
}

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    val repository: AppProjectRepository
    val cloudSyncEngine: com.example.data.cloud.FirebaseCloudSyncEngine =
        com.example.data.cloud.FirebaseCloudSyncEngine.getInstance(application)
    val cloudSyncState: StateFlow<com.example.data.model.CloudSyncState> = cloudSyncEngine.syncState

    private val _currentProjectId = MutableStateFlow<String?>(null)

    val currentProject: StateFlow<AppProject?> = _currentProjectId
        .filterNotNull()
        .flatMapLatest { id -> repository.observeProject(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val components: StateFlow<List<UiComponent>> = _currentProjectId
        .filterNotNull()
        .flatMapLatest { id -> repository.observeComponents(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(EditorTab.VISUAL_CANVAS)
    val selectedTab: StateFlow<EditorTab> = _selectedTab.asStateFlow()

    private val _selectedComponentForEdit = MutableStateFlow<UiComponent?>(null)
    val selectedComponentForEdit: StateFlow<UiComponent?> = _selectedComponentForEdit.asStateFlow()

    // Canvas Selection & Clipboard
    private val _selectedComponentId = MutableStateFlow<String?>(null)
    val selectedComponentId: StateFlow<String?> = _selectedComponentId.asStateFlow()

    private val _clipboardComponent = MutableStateFlow<UiComponent?>(null)
    val clipboardComponent: StateFlow<UiComponent?> = _clipboardComponent.asStateFlow()

    // History: Undo / Redo
    private val undoStack = ArrayDeque<List<UiComponent>>()
    private val redoStack = ArrayDeque<List<UiComponent>>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Shortcut HUD Flash Feedback
    private val _shortcutHudMessage = MutableStateFlow<String?>(null)
    val shortcutHudMessage: StateFlow<String?> = _shortcutHudMessage.asStateFlow()

    private val _isBuildingApk = MutableStateFlow(false)
    val isBuildingApk: StateFlow<Boolean> = _isBuildingApk.asStateFlow()

    private val _buildProgress = MutableStateFlow<ApkBuildProgress?>(null)
    val buildProgress: StateFlow<ApkBuildProgress?> = _buildProgress.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Interactive simulator test state
    val simulatorCounter = MutableStateFlow(10)
    val simulatorSwitch = MutableStateFlow(true)
    val simulatorSlider = MutableStateFlow(50f)
    val simulatorTextInput = MutableStateFlow("")

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppProjectRepository(database)
    }

    fun loadProject(projectId: String) {
        if (_currentProjectId.value != projectId) {
            _currentProjectId.value = projectId
            undoStack.clear()
            redoStack.clear()
            _canUndo.value = false
            _canRedo.value = false
            _selectedComponentId.value = null
        }
    }

    fun setTab(tab: EditorTab) {
        _selectedTab.value = tab
    }

    fun selectComponent(id: String?) {
        _selectedComponentId.value = id
    }

    fun selectComponentForEdit(component: UiComponent?) {
        _selectedComponentForEdit.value = component
        if (component != null) {
            _selectedComponentId.value = component.id
        }
    }

    private fun pushUndoSnapshot() {
        val current = components.value
        if (current.isNotEmpty() || undoStack.isNotEmpty()) {
            undoStack.addLast(current)
            if (undoStack.size > 50) {
                undoStack.removeFirst()
            }
            redoStack.clear()
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = false
        }
    }

    fun undo() {
        val projId = _currentProjectId.value ?: return
        if (undoStack.isEmpty()) {
            triggerShortcutHud("⚠️ Nothing to undo")
            return
        }
        val previousState = undoStack.removeLast()
        val currentState = components.value
        redoStack.addLast(currentState)

        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()

        viewModelScope.launch {
            repository.replaceAllComponents(projId, previousState)
            triggerShortcutHud("↺ Undone (Ctrl+Z)")
        }
    }

    fun redo() {
        val projId = _currentProjectId.value ?: return
        if (redoStack.isEmpty()) {
            triggerShortcutHud("⚠️ Nothing to redo")
            return
        }
        val nextState = redoStack.removeLast()
        val currentState = components.value
        undoStack.addLast(currentState)

        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()

        viewModelScope.launch {
            repository.replaceAllComponents(projId, nextState)
            triggerShortcutHud("↻ Redone (Ctrl+Y)")
        }
    }

    fun copySelectedComponent() {
        val currentList = components.value
        val targetId = _selectedComponentId.value ?: _selectedComponentForEdit.value?.id
        val target = currentList.find { it.id == targetId } ?: currentList.firstOrNull()

        if (target != null) {
            _clipboardComponent.value = target
            triggerShortcutHud("📋 Copied: \"${target.title.take(20)}\" (Ctrl+C)")
        } else {
            triggerShortcutHud("⚠️ No component selected to copy")
        }
    }

    fun cutSelectedComponent() {
        val currentList = components.value
        val targetId = _selectedComponentId.value ?: _selectedComponentForEdit.value?.id
        val target = currentList.find { it.id == targetId } ?: currentList.firstOrNull()

        if (target != null) {
            _clipboardComponent.value = target
            pushUndoSnapshot()
            val projId = _currentProjectId.value ?: return
            viewModelScope.launch {
                repository.deleteComponent(target.id, projId)
                _selectedComponentId.value = null
                _selectedComponentForEdit.value = null
                triggerShortcutHud("✂️ Cut: \"${target.title.take(20)}\" (Ctrl+X)")
            }
        } else {
            triggerShortcutHud("⚠️ No component selected to cut")
        }
    }

    fun pasteComponent() {
        val projId = _currentProjectId.value ?: return
        val clip = _clipboardComponent.value
        val currentList = components.value

        val componentToPaste = clip ?: currentList.find { it.id == _selectedComponentId.value }
        if (componentToPaste == null) {
            triggerShortcutHud("⚠️ Clipboard is empty (Ctrl+C first)")
            return
        }

        pushUndoSnapshot()

        val selectedIdx = currentList.indexOfFirst { it.id == _selectedComponentId.value }
        val insertIndex = if (selectedIdx >= 0) selectedIdx + 1 else currentList.size

        val newComponent = componentToPaste.copy(
            id = UUID.randomUUID().toString(),
            projectId = projId,
            title = if (componentToPaste.id == clip?.id) "${componentToPaste.title} (Copy)" else componentToPaste.title,
            orderIndex = insertIndex
        )

        val newList = currentList.toMutableList()
        newList.add(insertIndex.coerceAtMost(newList.size), newComponent)

        viewModelScope.launch {
            repository.replaceAllComponents(projId, newList)
            _selectedComponentId.value = newComponent.id
            triggerShortcutHud("📄 Pasted: \"${newComponent.title.take(20)}\" (Ctrl+V)")
        }
    }

    fun duplicateSelectedComponent() {
        val currentList = components.value
        val targetId = _selectedComponentId.value ?: _selectedComponentForEdit.value?.id
        val target = currentList.find { it.id == targetId } ?: currentList.lastOrNull()
        if (target != null) {
            duplicateComponent(target)
        } else {
            triggerShortcutHud("⚠️ No component selected to duplicate")
        }
    }

    fun deleteSelectedComponent() {
        val currentList = components.value
        val targetId = _selectedComponentId.value ?: _selectedComponentForEdit.value?.id
        val target = currentList.find { it.id == targetId }
        if (target != null) {
            deleteComponent(target.id)
        } else {
            triggerShortcutHud("⚠️ No component selected to delete")
        }
    }

    fun moveSelectedUp() {
        val currentList = components.value
        val targetId = _selectedComponentId.value ?: return
        val index = currentList.indexOfFirst { it.id == targetId }
        if (index > 0) {
            moveComponentUp(index)
            triggerShortcutHud("▲ Moved Up (Alt+↑)")
        }
    }

    fun moveSelectedDown() {
        val currentList = components.value
        val targetId = _selectedComponentId.value ?: return
        val index = currentList.indexOfFirst { it.id == targetId }
        if (index >= 0 && index < currentList.size - 1) {
            moveComponentDown(index)
            triggerShortcutHud("▼ Moved Down (Alt+↓)")
        }
    }

    fun selectNextComponent() {
        val currentList = components.value
        if (currentList.isEmpty()) return
        val currentIdx = currentList.indexOfFirst { it.id == _selectedComponentId.value }
        val nextIdx = if (currentIdx < 0 || currentIdx >= currentList.size - 1) 0 else currentIdx + 1
        _selectedComponentId.value = currentList[nextIdx].id
        triggerShortcutHud("Selected #${nextIdx + 1}: ${currentList[nextIdx].title.take(18)}")
    }

    fun selectPreviousComponent() {
        val currentList = components.value
        if (currentList.isEmpty()) return
        val currentIdx = currentList.indexOfFirst { it.id == _selectedComponentId.value }
        val prevIdx = if (currentIdx <= 0) currentList.size - 1 else currentIdx - 1
        _selectedComponentId.value = currentList[prevIdx].id
        triggerShortcutHud("Selected #${prevIdx + 1}: ${currentList[prevIdx].title.take(18)}")
    }

    fun triggerShortcutHud(message: String) {
        _shortcutHudMessage.value = message
    }

    fun clearShortcutHud() {
        _shortcutHudMessage.value = null
    }

    fun addComponent(type: ComponentType) {
        val projId = _currentProjectId.value ?: return
        val currentList = components.value
        pushUndoSnapshot()
        val newOrder = currentList.size

        val (title, subtitle, stateValue, placeholder, action) = when (type) {
            ComponentType.HEADER -> Tuple5("New Section Title", "Description or section subtitle", "", "", ComponentAction.NONE)
            ComponentType.TEXT -> Tuple5("Regular text block", "Secondary note line", "", "", ComponentAction.NONE)
            ComponentType.BUTTON -> Tuple5("Tap Action Button", "", "", "", ComponentAction.SHOW_TOAST)
            ComponentType.INPUT_FIELD -> Tuple5("Text Field Input", "", "", "Type something here...", ComponentAction.NONE)
            ComponentType.CARD -> Tuple5("Featured Card Info", "Detailed card subtitle and content preview", "", "", ComponentAction.NONE)
            ComponentType.IMAGE_BANNER -> Tuple5("Hero Illustration", "Visual showcase header", "", "", ComponentAction.NONE)
            ComponentType.SWITCH -> Tuple5("Toggle Feature Switch", "", "true", "", ComponentAction.TOGGLE_STATE)
            ComponentType.SLIDER -> Tuple5("Volume / Intensity Slider", "Adjust intensity level", "65", "", ComponentAction.NONE)
            ComponentType.PROGRESS_BAR -> Tuple5("Task Completion", "Step 3 of 5 finished", "60", "", ComponentAction.NONE)
            ComponentType.METRIC_STAT -> Tuple5("Daily Metric", "+18% this week", "1,240", "", ComponentAction.NONE)
            ComponentType.COUNTER_WIDGET -> Tuple5("Quantity Counter", "", "5", "", ComponentAction.INCREMENT_COUNTER)
            ComponentType.ACTION_CHIP -> Tuple5("Smart Filter Tag", "", "Active", "", ComponentAction.SHOW_TOAST)
            ComponentType.BADGE -> Tuple5("Status: Online", "", "", "", ComponentAction.NONE)
            ComponentType.DIVIDER -> Tuple5("Divider", "", "", "", ComponentAction.NONE)
            ComponentType.RATING_BAR -> Tuple5("User Rating", "5 Star Feedback", "4.5", "", ComponentAction.NONE)
            ComponentType.LIST_VIEW -> Tuple5("Activity Item", "Timestamp: Just now", "", "", ComponentAction.NONE)
        }

        val newComponent = UiComponent(
            id = UUID.randomUUID().toString(),
            projectId = projId,
            type = type,
            title = title,
            subtitle = subtitle,
            stateValue = stateValue,
            placeholder = placeholder,
            actionType = action,
            actionPayload = if (action == ComponentAction.SHOW_TOAST) "Button tapped!" else "",
            orderIndex = newOrder
        )

        viewModelScope.launch {
            repository.addComponent(newComponent)
            _selectedComponentId.value = newComponent.id
            _toastMessage.value = "Added ${type.name.replace("_", " ")} to canvas"
            triggerShortcutHud("+ Added ${newComponent.title.take(18)}")
        }
    }

    fun updateComponent(updated: UiComponent) {
        pushUndoSnapshot()
        viewModelScope.launch {
            repository.updateComponent(updated)
            _selectedComponentForEdit.value = null
            _toastMessage.value = "Component updated"
        }
    }

    fun deleteComponent(componentId: String) {
        val projId = _currentProjectId.value ?: return
        val currentList = components.value
        val comp = currentList.find { it.id == componentId }
        pushUndoSnapshot()
        viewModelScope.launch {
            repository.deleteComponent(componentId, projId)
            if (_selectedComponentId.value == componentId) {
                _selectedComponentId.value = null
            }
            _selectedComponentForEdit.value = null
            _toastMessage.value = "Component removed"
            triggerShortcutHud("🗑️ Deleted: \"${comp?.title?.take(18) ?: "Component"}\" (Del)")
        }
    }

    fun moveComponentUp(index: Int) {
        if (index <= 0) return
        val currentList = components.value.toMutableList()
        if (index < currentList.size) {
            pushUndoSnapshot()
            val item = currentList.removeAt(index)
            currentList.add(index - 1, item)
            val projId = _currentProjectId.value ?: return
            viewModelScope.launch {
                repository.reorderComponents(projId, currentList)
            }
        }
    }

    fun moveComponentDown(index: Int) {
        val currentList = components.value.toMutableList()
        if (index >= 0 && index < currentList.size - 1) {
            pushUndoSnapshot()
            val item = currentList.removeAt(index)
            currentList.add(index + 1, item)
            val projId = _currentProjectId.value ?: return
            viewModelScope.launch {
                repository.reorderComponents(projId, currentList)
            }
        }
    }

    fun duplicateComponent(component: UiComponent) {
        val projId = _currentProjectId.value ?: return
        val currentList = components.value
        pushUndoSnapshot()
        val index = currentList.indexOfFirst { it.id == component.id }
        val insertIndex = if (index >= 0) index + 1 else currentList.size

        val duplicate = component.copy(
            id = UUID.randomUUID().toString(),
            title = "${component.title} (Copy)",
            orderIndex = insertIndex
        )
        val newList = currentList.toMutableList()
        newList.add(insertIndex.coerceAtMost(newList.size), duplicate)

        viewModelScope.launch {
            repository.replaceAllComponents(projId, newList)
            _selectedComponentId.value = duplicate.id
            _toastMessage.value = "Component duplicated"
            triggerShortcutHud("📑 Duplicated (Ctrl+D)")
        }
    }

    fun addStoreAsset(asset: com.example.data.model.StoreAsset) {
        val projId = _currentProjectId.value ?: return
        val currentList = components.value
        pushUndoSnapshot()
        val comp = asset.componentSnippet?.copy(
            id = UUID.randomUUID().toString(),
            projectId = projId,
            orderIndex = currentList.size
        ) ?: UiComponent(
            id = UUID.randomUUID().toString(),
            projectId = projId,
            type = ComponentType.CARD,
            title = asset.title,
            subtitle = asset.subtitle,
            orderIndex = currentList.size
        )
        viewModelScope.launch {
            repository.addComponent(comp)
            _selectedComponentId.value = comp.id
            _toastMessage.value = "Added '${asset.title}' to canvas"
            triggerShortcutHud("+ Added Add-On: \"${asset.title.take(18)}\"")
        }
    }

    fun updateProjectDetails(name: String, description: String, primaryColor: String, secondaryColor: String) {
        val proj = currentProject.value ?: return
        viewModelScope.launch {
            repository.saveProject(
                proj.copy(
                    name = name,
                    description = description,
                    primaryColorHex = primaryColor,
                    secondaryColorHex = secondaryColor
                )
            )
            _toastMessage.value = "Project settings saved"
        }
    }

    fun buildApk(context: Context) {
        val projId = _currentProjectId.value ?: return
        viewModelScope.launch {
            _isBuildingApk.value = true
            _buildProgress.value = null
            repository.buildApk(context, projId) { progress ->
                _buildProgress.value = progress
            }
        }
    }

    fun dismissBuildModal() {
        _isBuildingApk.value = false
    }

    fun downloadLatestApk(context: Context) {
        val progress = _buildProgress.value
        val apkFile = progress?.apkFile ?: currentProject.value?.latestApkPath?.let { File(it) }
        val proj = currentProject.value ?: return

        if (apkFile != null && apkFile.exists()) {
            viewModelScope.launch {
                val res = ApkFileManager.saveApkToDownloads(context, apkFile, "${proj.name}_v${proj.versionName}.apk")
                if (res.isSuccess) {
                    _toastMessage.value = "APK downloaded to device Downloads folder!"
                } else {
                    _toastMessage.value = "Download error: ${res.exceptionOrNull()?.message}"
                }
            }
        } else {
            _toastMessage.value = "APK file not compiled yet"
        }
    }

    fun installLatestApk(context: Context) {
        val progress = _buildProgress.value
        val apkFile = progress?.apkFile ?: currentProject.value?.latestApkPath?.let { File(it) }
        if (apkFile != null && apkFile.exists()) {
            ApkFileManager.launchApkInstaller(context, apkFile)
        } else {
            _toastMessage.value = "APK file not found"
        }
    }

    fun shareLatestApk(context: Context) {
        val progress = _buildProgress.value
        val apkFile = progress?.apkFile ?: currentProject.value?.latestApkPath?.let { File(it) }
        val proj = currentProject.value ?: return
        if (apkFile != null && apkFile.exists()) {
            ApkFileManager.shareApkFile(context, apkFile, proj.name)
        } else {
            _toastMessage.value = "APK file not found"
        }
    }

    fun getGeneratedKotlinCode(): String {
        val proj = currentProject.value ?: return "// No project selected"
        val comps = components.value
        return AiAppGenerator.generateJetpackComposeCode(proj, comps)
    }

    val diagnosticsLogs = com.example.data.diagnostics.DiagnosticsEngine.logs
    val shellHistory = com.example.data.shell.ShellEngine.history

    fun toggleStar() {
        val proj = currentProject.value ?: return
        viewModelScope.launch {
            repository.toggleStar(proj.id)
            _toastMessage.value = if (!proj.isStarred) "★ Starred project!" else "Unstarred project"
        }
    }

    fun executeShellCommand(cmd: String) {
        val proj = currentProject.value
        viewModelScope.launch {
            val all = repository.allProjects
            // Grab current list
            com.example.data.shell.ShellEngine.executeCommand(
                cmdLine = cmd,
                currentProject = proj,
                allProjects = if (proj != null) listOf(proj) else emptyList(),
                onStarToggle = { id ->
                    viewModelScope.launch { repository.toggleStar(id) }
                }
            )
        }
    }

    fun resolveDiagnosticLog(id: String) {
        com.example.data.diagnostics.DiagnosticsEngine.resolveLog(id)
        _toastMessage.value = "Issue resolved"
    }

    fun clearDiagnosticLogs() {
        com.example.data.diagnostics.DiagnosticsEngine.clearAllLogs()
        _toastMessage.value = "Logs cleared"
    }

    fun triggerDiagnosticScan() {
        viewModelScope.launch {
            val log = com.example.data.diagnostics.DiagnosticsEngine.triggerSimulatedDiagnosticCheck()
            _toastMessage.value = "Scan found: ${log.tag}"
        }
    }

    fun applyAiFix(log: com.example.data.model.DiagnosticLog) {
        viewModelScope.launch {
            com.example.data.diagnostics.DiagnosticsEngine.resolveLog(log.id)
            _toastMessage.value = "✓ AI Fix applied: ${log.suggestedAiFix ?: "Repaired"}"
        }
    }

    fun syncCurrentProjectToCloud() {
        viewModelScope.launch {
            val proj = currentProject.value ?: return@launch
            val comps = components.value
            val res = cloudSyncEngine.uploadProjectToCloud(proj, comps)
            if (res.isSuccess) {
                _toastMessage.value = "☁️ Synced to Cloud Vault (${comps.size} components)"
            } else {
                _toastMessage.value = "Sync Error: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun importCloudVersion(record: com.example.data.model.CloudProjectRecord) {
        viewModelScope.launch {
            val imported = repository.importProjectFromCloud(record, cloudSyncEngine)
            _toastMessage.value = "✓ Restored from Cloud: ${record.lastSyncDeviceName}"
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private data class Tuple5(
        val first: String,
        val second: String,
        val third: String,
        val fourth: String,
        val fifth: ComponentAction
    )
}
