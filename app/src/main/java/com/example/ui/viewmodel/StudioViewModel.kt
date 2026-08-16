package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.apk.ApkBuildProgress
import com.example.data.apk.ApkFileManager
import com.example.data.db.AppDatabase
import com.example.data.model.AiConfiguration
import com.example.data.model.ApkBuildRecord
import com.example.data.model.AppProject
import com.example.data.model.StoreAsset
import com.example.data.model.SubscriptionStatus
import com.example.data.preferences.SubscriptionManager
import com.example.data.repository.AppProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface StudioUiState {
    data object Idle : StudioUiState
    data class Generating(val prompt: String, val stepMessage: String) : StudioUiState
    data class Success(val project: AppProject) : StudioUiState
    data class Error(val message: String) : StudioUiState
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    val repository: AppProjectRepository
    val subscriptionManager: SubscriptionManager = SubscriptionManager.getInstance(application)
    val cloudSyncEngine: com.example.data.cloud.FirebaseCloudSyncEngine =
        com.example.data.cloud.FirebaseCloudSyncEngine.getInstance(application)
    val tutorialManager: com.example.data.tutorial.TutorialManager =
        com.example.data.tutorial.TutorialManager.getInstance(application)

    val allProjects: StateFlow<List<AppProject>>
    val allBuilds: StateFlow<List<ApkBuildRecord>>
    val subscriptionStatus: StateFlow<SubscriptionStatus> = subscriptionManager.subscriptionStatus
    val cloudSyncState: StateFlow<com.example.data.model.CloudSyncState> = cloudSyncEngine.syncState
    val tutorialProgress: StateFlow<com.example.data.model.TutorialProgress> = tutorialManager.tutorialProgress

    private val _generationState = MutableStateFlow<StudioUiState>(StudioUiState.Idle)
    val generationState: StateFlow<StudioUiState> = _generationState.asStateFlow()

    private val _quickBuildProgress = MutableStateFlow<ApkBuildProgress?>(null)
    val quickBuildProgress: StateFlow<ApkBuildProgress?> = _quickBuildProgress.asStateFlow()

    private val _isBuildingApk = MutableStateFlow(false)
    val isBuildingApk: StateFlow<Boolean> = _isBuildingApk.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppProjectRepository(database)

        allProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allBuilds = repository.allBuilds.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.seedDefaultDataIfEmpty()
            trySyncOfflineChanges()
        }
    }

    fun trySyncOfflineChanges() {
        viewModelScope.launch {
            if (isNetworkAvailable(getApplication())) {
                val result = repository.syncOfflineChanges(cloudSyncEngine)
                result.onSuccess { count ->
                    if (count > 0) {
                        _userMessage.value = "Synced $count offline project edit(s) to cloud automatically!"
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun generateAppFromPrompt(
        prompt: String,
        config: AiConfiguration = AiConfiguration(),
        onComplete: (AppProject) -> Unit
    ) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _generationState.value = StudioUiState.Generating(prompt, "Synthesizing UI with ${config.selectedModel.displayName} & ingesting parameters...")
            try {
                val project = repository.createProjectWithAi(prompt, config)
                _generationState.value = StudioUiState.Success(project)
                onComplete(project)
            } catch (e: Exception) {
                _generationState.value = StudioUiState.Error(e.message ?: "Failed to generate app")
            }
        }
    }

    fun createProjectFromTemplate(templateId: String, onComplete: (AppProject) -> Unit) {
        viewModelScope.launch {
            _generationState.value = StudioUiState.Generating("Template: $templateId", "Instantiating template components and theme...")
            try {
                val project = repository.createProjectFromTemplate(templateId)
                _generationState.value = StudioUiState.Success(project)
                onComplete(project)
            } catch (e: Exception) {
                _generationState.value = StudioUiState.Error(e.message ?: "Failed to load template")
            }
        }
    }

    fun redeemPromoCode(code: String): Result<SubscriptionStatus> {
        val result = subscriptionManager.redeemCode(code)
        if (result.isSuccess) {
            _userMessage.value = "🎉 Code '$code' applied! Unlocked 3 Months of Free Pro Membership!"
        }
        return result
    }

    fun installStoreAssetToProject(asset: StoreAsset, targetProjectId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val comp = asset.componentSnippet
            if (comp != null) {
                repository.addComponent(comp.copy(projectId = targetProjectId))
                _userMessage.value = "Added '${asset.title}' to your project!"
                onComplete()
            } else {
                _userMessage.value = "Installed '${asset.title}' to workspace!"
                onComplete()
            }
        }
    }

    fun resetGenerationState() {
        _generationState.value = StudioUiState.Idle
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            _userMessage.value = "Project deleted"
        }
    }

    fun quickBuildApk(context: Context, projectId: String) {
        viewModelScope.launch {
            _isBuildingApk.value = true
            _quickBuildProgress.value = null
            repository.buildApk(context, projectId) { progress ->
                _quickBuildProgress.value = progress
            }
        }
    }

    fun dismissQuickBuildDialog() {
        _isBuildingApk.value = false
        _quickBuildProgress.value = null
    }

    fun downloadApk(context: Context, record: ApkBuildRecord) {
        viewModelScope.launch {
            val file = File(record.apkFilePath)
            if (file.exists()) {
                val res = ApkFileManager.saveApkToDownloads(context, file, record.apkFileName)
                if (res.isSuccess) {
                    _userMessage.value = "APK downloaded to device Downloads folder!"
                } else {
                    _userMessage.value = "Download failed: ${res.exceptionOrNull()?.message}"
                }
            } else {
                _userMessage.value = "APK file not found on device storage"
            }
        }
    }

    fun installApk(context: Context, record: ApkBuildRecord) {
        val file = File(record.apkFilePath)
        ApkFileManager.launchApkInstaller(context, file)
    }

    fun shareApk(context: Context, record: ApkBuildRecord) {
        val file = File(record.apkFilePath)
        ApkFileManager.shareApkFile(context, file, record.projectName)
    }

    fun deleteBuildRecord(buildId: String) {
        viewModelScope.launch {
            repository.deleteBuild(buildId)
            _userMessage.value = "Build record removed"
        }
    }

    fun toggleProjectStar(projectId: String) {
        viewModelScope.launch {
            repository.toggleStar(projectId)
        }
    }

    fun incrementProjectView(projectId: String) {
        viewModelScope.launch {
            repository.incrementViewCount(projectId)
        }
    }

    fun executeShellCommand(cmd: String, currentProject: AppProject? = null) {
        viewModelScope.launch {
            com.example.data.shell.ShellEngine.executeCommand(
                cmdLine = cmd,
                currentProject = currentProject,
                allProjects = allProjects.value,
                onStarToggle = { id -> toggleProjectStar(id) }
            )
        }
    }

    val diagnosticsLogs = com.example.data.diagnostics.DiagnosticsEngine.logs
    val shellHistory = com.example.data.shell.ShellEngine.history

    fun resolveDiagnosticLog(id: String) {
        com.example.data.diagnostics.DiagnosticsEngine.resolveLog(id)
        _userMessage.value = "Issue marked as resolved"
    }

    fun clearDiagnosticLogs() {
        com.example.data.diagnostics.DiagnosticsEngine.clearAllLogs()
        _userMessage.value = "Diagnostic log console cleared"
    }

    fun triggerDiagnosticScan() {
        viewModelScope.launch {
            val log = com.example.data.diagnostics.DiagnosticsEngine.triggerSimulatedDiagnosticCheck()
            _userMessage.value = "Diagnostics scanned: Found 1 item (${log.tag})"
        }
    }

    fun applyAiFix(log: com.example.data.model.DiagnosticLog) {
        viewModelScope.launch {
            com.example.data.diagnostics.DiagnosticsEngine.resolveLog(log.id)
            _userMessage.value = "✓ AI auto-fix applied: ${log.suggestedAiFix ?: "Repaired AST"}"
        }
    }

    fun buyStoreAddOn(asset: StoreAsset) {
        val res = subscriptionManager.buyAddOn(asset)
        if (res.isSuccess) {
            _userMessage.value = res.getOrNull() ?: "Add-On purchased successfully!"
        } else {
            _userMessage.value = res.exceptionOrNull()?.message ?: "Failed to complete purchase"
        }
    }

    fun claimDevCreditsGrant(amount: Int = 500) {
        val total = subscriptionManager.topUpCredits(amount)
        _userMessage.value = "🎁 +$amount Dev Credits claimed! Current balance: $total Credits"
    }

    fun toggleAddOnInstallation(assetId: String) {
        val installed = subscriptionManager.toggleInstallAddOn(assetId)
        _userMessage.value = if (installed) "Add-On enabled for projects" else "Add-On disabled"
    }

    // ==========================================
    // Firebase Cloud Sync Operations
    // ==========================================

    fun uploadProjectToCloud(projectId: String) {
        viewModelScope.launch {
            val project = repository.getProject(projectId)
            if (project == null) {
                _userMessage.value = "Project not found"
                return@launch
            }
            val components = repository.getComponentsList(projectId)
            val res = cloudSyncEngine.uploadProjectToCloud(project, components)
            if (res.isSuccess) {
                _userMessage.value = "☁️ Synced '${project.name}' to Firebase Cloud Vault!"
            } else {
                _userMessage.value = "Cloud Sync Error: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun syncAllProjectsToCloud() {
        viewModelScope.launch {
            val projects = allProjects.value
            if (projects.isEmpty()) {
                _userMessage.value = "No local projects to sync"
                return@launch
            }
            var count = 0
            for (p in projects) {
                val comps = repository.getComponentsList(p.id)
                val res = cloudSyncEngine.uploadProjectToCloud(p, comps)
                if (res.isSuccess) count++
            }
            _userMessage.value = "☁️ Synced $count project(s) to Firebase Cloud!"
        }
    }

    fun refreshCloudProjects() {
        viewModelScope.launch {
            val res = cloudSyncEngine.fetchCloudProjects()
            if (res.isSuccess) {
                _userMessage.value = "Cloud Vault updated (${res.getOrNull()?.size ?: 0} projects found)"
            } else {
                _userMessage.value = "Cloud fetch issue: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun importCloudProject(record: com.example.data.model.CloudProjectRecord, onComplete: ((AppProject) -> Unit)? = null) {
        viewModelScope.launch {
            val imported = repository.importProjectFromCloud(record, cloudSyncEngine)
            _userMessage.value = "📲 Restored '${imported.name}' from Cloud to local device!"
            onComplete?.invoke(imported)
        }
    }

    fun deleteCloudProject(projectId: String) {
        viewModelScope.launch {
            val res = cloudSyncEngine.deleteCloudProject(projectId)
            if (res.isSuccess) {
                _userMessage.value = "Project removed from Cloud Vault"
            } else {
                _userMessage.value = "Failed to remove cloud project"
            }
        }
    }

    fun setAutoCloudSync(enabled: Boolean) {
        cloudSyncEngine.setAutoSync(enabled)
        _userMessage.value = if (enabled) "Auto-Cloud Sync enabled" else "Auto-Cloud Sync disabled"
    }

    fun updateDeviceName(name: String) {
        cloudSyncEngine.updateDeviceName(name)
        _userMessage.value = "Device label updated to '$name'"
    }

    fun setCloudAccount(email: String, name: String) {
        cloudSyncEngine.setUserAccount(email, name)
        _userMessage.value = "Signed in to Cloud as $name ($email)"
    }

    fun signOutCloud() {
        cloudSyncEngine.signOut()
        _userMessage.value = "Signed out of Cloud Sync"
    }

    // ==========================================
    // Interactive Tutorial Mode Operations
    // ==========================================

    fun launchTutorialSandbox(onComplete: (AppProject) -> Unit) {
        viewModelScope.launch {
            val proj = repository.createOrLoadTutorialSandbox(tutorialManager)
            _userMessage.value = "🚀 Launched Interactive Tutorial Sandbox!"
            onComplete(proj)
        }
    }

    fun markTutorialStepCompleted(stepId: String) {
        tutorialManager.markStepCompleted(stepId)
    }

    fun setTutorialStepIndex(index: Int) {
        tutorialManager.setStepIndex(index)
    }

    fun dismissTutorialIntro() {
        tutorialManager.setDismissedIntro(true)
    }

    fun claimTutorialCompletionReward() {
        val claimed = tutorialManager.claimReward()
        if (claimed) {
            val total = subscriptionManager.topUpCredits(500)
            _userMessage.value = "🎓 Tutorial Complete! Claimed +500 Dev Credits (Wallet: $total credits)"
        }
    }

    fun resetTutorialProgress() {
        tutorialManager.resetTutorial()
        _userMessage.value = "Tutorial walkthrough reset to Step 1"
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
