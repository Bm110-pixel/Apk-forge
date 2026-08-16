package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ComponentType {
    HEADER,
    TEXT,
    BUTTON,
    INPUT_FIELD,
    CARD,
    IMAGE_BANNER,
    SWITCH,
    SLIDER,
    PROGRESS_BAR,
    METRIC_STAT,
    LIST_VIEW,
    ACTION_CHIP,
    BADGE,
    DIVIDER,
    COUNTER_WIDGET,
    RATING_BAR
}

enum class ComponentAction {
    NONE,
    SHOW_TOAST,
    INCREMENT_COUNTER,
    DECREMENT_COUNTER,
    RESET_COUNTER,
    TOGGLE_STATE,
    CALCULATE_SUM,
    OPEN_URL,
    SHOW_ALERT
}

@Entity(tableName = "app_projects")
data class AppProject(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val packageName: String,
    val description: String,
    val category: String = "Utility",
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val primaryColorHex: String = "#6366F1", // Indigo
    val secondaryColorHex: String = "#06B6D4", // Cyan
    val backgroundColorHex: String = "#0F172A", // Slate Dark
    val surfaceColorHex: String = "#1E293B",
    val textColorHex: String = "#F8FAFC",
    val isDarkTheme: Boolean = true,
    val iconName: String = "ic_default_app",
    val promptUsed: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val latestApkPath: String? = null,
    val latestApkSize: Long = 0L,
    val buildCount: Int = 0,
    val viewCount: Int = 142,
    val downloadCount: Int = 38,
    val starCount: Int = 19,
    val isStarred: Boolean = false
)

@Entity(tableName = "ui_components")
data class UiComponent(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val type: ComponentType,
    val title: String,
    val subtitle: String = "",
    val stateValue: String = "",
    val placeholder: String = "",
    val actionType: ComponentAction = ComponentAction.NONE,
    val actionPayload: String = "",
    val orderIndex: Int = 0,
    val colorHex: String? = null,
    val fontSizeSp: Int = 16,
    val cornerRadiusDp: Int = 12,
    val iconName: String = "",
    val isEnabled: Boolean = true,
    val customDataJson: String = "{}"
)

@Entity(tableName = "apk_builds")
data class ApkBuildRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val projectName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS", // PENDING, BUILDING, SUCCESS, FAILED
    val apkFileName: String,
    val apkFilePath: String,
    val fileSizeBytes: Long,
    val buildDurationMs: Long,
    val buildLogs: String
)

data class PromptTemplate(
    val title: String,
    val prompt: String,
    val category: String,
    val icon: String,
    val tagColor: String,
    val isBuiltinTemplate: Boolean = false,
    val templateId: String = ""
)

// In-App Asset Store & Add-Ons Marketplace Models
enum class StoreAssetCategory(val displayName: String) {
    ALL("All Assets"),
    ADD_ONS("Add-Ons & SDKs"),
    UI_COMPONENTS("UI Components"),
    VECTOR_ICONS("Vector Icons"),
    CODE_MODULES("Code Modules"),
    LAYOUT_KITS("Layout Kits")
}

data class StoreAsset(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: StoreAssetCategory,
    val isPremium: Boolean = false,
    val iconName: String = "widgets",
    val tags: List<String> = emptyList(),
    val previewSnippet: String = "",
    val componentSnippet: UiComponent? = null,
    val fullCodeModule: String = "",
    val rating: Float = 4.9f,
    val downloadCount: Int = 1240,
    // Add-On Marketplace & Purchase attributes
    val priceCredits: Int = 0, // 0 = Free / Included with Pro
    val priceUsd: String = "$0.00",
    val addOnVersion: String = "1.0.0",
    val author: String = "Google AI Studio Verified",
    val permissionsRequired: List<String> = emptyList(),
    val featuresList: List<String> = emptyList()
)

// AI Model & Generator Configuration Models
enum class AiModelOption(val displayName: String, val provider: String, val isPremium: Boolean, val description: String) {
    GEMINI_FLASH("Gemini 1.5 Flash", "Google", false, "Ultra-fast synthesis for real-time mobile app development"),
    GEMINI_PRO("Gemini 1.5 Pro", "Google", true, "Advanced reasoning with deep architecture planning"),
    CLAUDE_SONNET("Claude 3.5 Sonnet", "Anthropic", true, "Superior Compose styling & clean design aesthetics"),
    DEEPSEEK_CODER("DeepSeek Coder V2", "DeepSeek", true, "Specialized Kotlin compiler & state architecture"),
    LOCAL_NANO("On-Device Nano", "Local Edge", false, "Instant offline synthesis without network")
}

enum class DesignThemeMood(val displayName: String, val primaryHex: String, val secondaryHex: String, val bgHex: String) {
    SLEEK_MINIMAL("Sleek Minimal", "#005AC1", "#535F70", "#F3F4F9"),
    CYBERPUNK_NEON("Cyberpunk Neon", "#EC4899", "#8B5CF6", "#0E0B16"),
    GLASS_MODERN("Glassmorphism Dark", "#6366F1", "#06B6D4", "#0B132B"),
    MATERIAL_EXPRESSIVE("Material 3 Expressive", "#6750A4", "#7D5260", "#FEF7FF"),
    RETRO_ARCADE("Retro 80s Synth", "#F59E0B", "#EF4444", "#180B1E"),
    FOREST_EMERALD("Forest Emerald", "#059669", "#10B981", "#061A14")
}

enum class ArchitecturePattern(val displayName: String, val description: String) {
    MVVM_COMPOSE("MVVM + StateFlow", "Standard modern Android MVVM with reactive UI state"),
    CLEAN_ARCHITECTURE("Clean Arch + UseCases", "Domain repositories, use-cases and view models"),
    SINGLE_DECLARATIVE("Single-Screen MVI", "Declarative intent-driven single state flow")
}

enum class DatasetInputType(val displayName: String) {
    JSON("JSON Schema / Array"),
    CSV("CSV Records Table"),
    PLAIN_TEXT("Plain Text Knowledge Base")
}

data class AiConfiguration(
    val selectedModel: AiModelOption = AiModelOption.GEMINI_FLASH,
    val temperature: Float = 0.7f,
    val themeMood: DesignThemeMood = DesignThemeMood.SLEEK_MINIMAL,
    val architecture: ArchitecturePattern = ArchitecturePattern.MVVM_COMPOSE,
    val targetSdk: Int = 35,
    val systemPromptPersona: String = "Senior Android Architect & Compose Specialist",
    val customDatasetType: DatasetInputType = DatasetInputType.JSON,
    val customDatasetPayload: String = "",
    val datasetDescription: String = ""
)

// Subscription, Dev Credits & Add-Ons Purchase System
data class SubscriptionStatus(
    val isPremium: Boolean = false,
    val tierName: String = "Free Starter",
    val expiresAtTimestamp: Long = 0L,
    val redeemedCode: String? = null,
    val devCredits: Int = 1500, // Developer wallet balance to buy Add-Ons & SDKs
    val purchasedAddOnIds: Set<String> = emptySet(),
    val installedAddOnIds: Set<String> = emptySet(),
    val unlockedPerks: List<String> = listOf(
        "Standard UI Component Library",
        "Gemini 1.5 Flash Model",
        "Local APK Compilation",
        "Interactive Smartphone Simulator"
    )
)

// Diagnostics & Error Logs Models
enum class LogLevel {
    INFO,
    DEBUG,
    WARN,
    ERROR,
    SUCCESS
}

enum class LogCategory(val displayName: String) {
    ALL("All Streams"),
    COMPILER("Gradle & Compose"),
    RUNTIME("App Runtime / VM"),
    SYNTAX("Kotlin Analyzer"),
    NETWORK("AI & API Sync"),
    SECURITY("APK Signer & Permissions")
}

data class DiagnosticLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val category: LogCategory,
    val tag: String,
    val message: String,
    val stackTrace: String? = null,
    val suggestedAiFix: String? = null,
    val isResolved: Boolean = false
)

// Developer Shell Models
data class ShellCommandRecord(
    val id: String = UUID.randomUUID().toString(),
    val command: String,
    val output: String,
    val isError: Boolean = false,
    val executionTimeMs: Long = 12L,
    val timestamp: Long = System.currentTimeMillis()
)

data class ProjectAnalyticsSummary(
    val projectId: String,
    val projectName: String,
    val totalViews: Int,
    val totalDownloads: Int,
    val totalStars: Int,
    val isStarred: Boolean,
    val conversionRatePercent: Float,
    val activeUsersToday: Int,
    val crashesLast24h: Int,
    val avgSessionDurationSec: Int
)

// Firebase Cloud Sync & Cross-Device Access Models
enum class CloudSyncStatus {
    LOCAL_ONLY,
    SYNCED,
    PENDING_UPLOAD,
    SYNCING,
    CONFLICT,
    ERROR
}

data class CloudProjectRecord(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val packageName: String = "",
    val description: String = "",
    val category: String = "Utility",
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val primaryColorHex: String = "#6366F1",
    val secondaryColorHex: String = "#06B6D4",
    val backgroundColorHex: String = "#0F172A",
    val surfaceColorHex: String = "#1E293B",
    val textColorHex: String = "#F8FAFC",
    val isDarkTheme: Boolean = true,
    val iconName: String = "ic_default_app",
    val promptUsed: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val lastSyncDeviceId: String = "",
    val lastSyncDeviceName: String = "Mobile Device",
    val authorEmail: String = "developer@aistudio.com",
    val authorName: String = "Android Dev",
    val componentCount: Int = 0,
    val componentsJson: String = "[]",
    val syncStatus: CloudSyncStatus = CloudSyncStatus.SYNCED
)

data class CloudSyncState(
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
    val syncError: String? = null,
    val currentDeviceName: String = "Android Device",
    val currentDeviceId: String = "",
    val autoSyncEnabled: Boolean = true,
    val cloudProjects: List<CloudProjectRecord> = emptyList(),
    val userEmail: String? = null,
    val userDisplayName: String? = null,
    val isSignedIn: Boolean = false,
    val isFirebaseReady: Boolean = true
)

// Interactive Tutorial Mode Models
data class TutorialStep(
    val id: String,
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: String,
    val iconName: String,
    val highlights: List<String>,
    val actionLabel: String? = null,
    val actionRoute: String? = null
)

data class TutorialProgress(
    val completedStepIds: Set<String> = emptySet(),
    val currentStepIndex: Int = 0,
    val isCompleted: Boolean = false,
    val rewardClaimed: Boolean = false,
    val hasDismissedIntro: Boolean = false
)
