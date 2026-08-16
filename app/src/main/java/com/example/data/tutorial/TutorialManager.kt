package com.example.data.tutorial

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppProject
import com.example.data.model.ComponentAction
import com.example.data.model.ComponentType
import com.example.data.model.TutorialProgress
import com.example.data.model.TutorialStep
import com.example.data.model.UiComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class TutorialManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("apk_builder_tutorial_prefs", Context.MODE_PRIVATE)

    val tutorialSteps: List<TutorialStep> = listOf(
        TutorialStep(
            id = "step_welcome",
            stepNumber = 1,
            title = "Welcome to AI APK Studio",
            subtitle = "Build production Android apps without writing boilerplate",
            description = "AI APK Studio gives you an end-to-end mobile development environment right on your device. From visual design to real standalone APK compilation, you have complete creative power.",
            category = "Overview",
            iconName = "rocket_launch",
            highlights = listOf(
                "Visual Drag & Drop: Build reactive layouts with live component previews",
                "On-Device Bytecode Compiler: Synthesizes installable .apk packages",
                "Firebase Cloud Sync: Access your projects across multiple phones & tablets"
            ),
            actionLabel = "Next: AI Creation"
        ),
        TutorialStep(
            id = "step_ai_generator",
            stepNumber = 2,
            title = "AI Generative Engine",
            subtitle = "Transform prompts into working Jetpack Compose code",
            description = "Use Google Gemini models to synthesize complete UI workflows from simple natural language descriptions. Choose from 6 curated design moods and custom dataset schemas.",
            category = "AI Synthesis",
            iconName = "auto_awesome",
            highlights = listOf(
                "Gemini 1.5 Flash & Pro: Lightning fast layout and logic generation",
                "Theme Presets: Sleek Minimal, Cyberpunk Neon, Glassmorphism, Material 3",
                "Dataset Ingestion: Pass your own JSON/CSV tables to auto-populate UI"
            ),
            actionLabel = "Next: Visual Canvas"
        ),
        TutorialStep(
            id = "step_visual_canvas",
            stepNumber = 3,
            title = "Visual Canvas & Inspector",
            subtitle = "Customize components, layout order, and action behaviors",
            description = "Select any component to open the property inspector. Adjust titles, fonts, corner radius, action payloads (e.g. Toast alerts, Counters, URL openers), and reorder elements dynamically.",
            category = "Editor",
            iconName = "dashboard_customize",
            highlights = listOf(
                "16+ UI Component types: Buttons, Sliders, Metric Cards, Switch toggles",
                "Interactive Inspector: Real-time property and color tuning",
                "Live Interactive Preview: Test buttons and state changes on canvas"
            ),
            actionLabel = "Next: Pro Shortcuts"
        ),
        TutorialStep(
            id = "step_keyboard_shortcuts",
            stepNumber = 4,
            title = "Keyboard Shortcuts & Hotkeys",
            subtitle = "Master fast desktop & tablet canvas productivity",
            description = "Work at the speed of thought using built-in keyboard shortcuts. Undo mistakes, copy-paste elements, reorder canvas items, and compile APKs with quick hotkeys.",
            category = "Productivity",
            iconName = "keyboard",
            highlights = listOf(
                "Ctrl + Z / Ctrl + Y: Full snapshot undo and redo history",
                "Ctrl + C / Ctrl + V / Ctrl + D: Copy, paste, and duplicate components",
                "Alt + ↑ / ↓: Rapidly reorder canvas layout elements",
                "Ctrl + K: Open Component Palette | Ctrl + B: Fast Build APK"
            ),
            actionLabel = "Next: Cloud Sync"
        ),
        TutorialStep(
            id = "step_cloud_sync",
            stepNumber = 5,
            title = "Firebase Cloud Sync & Multi-Device",
            subtitle = "Access & edit your APK projects from any device",
            description = "Sync your projects to Firebase Firestore in real-time. Start designing on your phone, continue on your tablet, or edit on an emulator with instant cloud backup.",
            category = "Cloud Continuity",
            iconName = "cloud_sync",
            highlights = listOf(
                "Cross-Device Cloud Vault: Device origin tagging (e.g. Pixel 8, Galaxy Tab)",
                "1-Tap Restore & Edit: Pull cloud projects into local database seamlessly",
                "Auto-Sync on Save: Always keeps your mobile workspace updated"
            ),
            actionLabel = "Next: Asset Store"
        ),
        TutorialStep(
            id = "step_store_addons",
            stepNumber = 6,
            title = "Asset Store & Dev Credits",
            subtitle = "Unlock SDKs, UI Kits, and Monetization Modules",
            description = "Browse the in-app Add-Ons marketplace to install AdMob monetization, Google Maps, ARCore, and Firebase Auth modules. Use promo code 'dev15' for 3 Months Free Pro + 2,500 Credits!",
            category = "Marketplace",
            iconName = "storefront",
            highlights = listOf(
                "SDK Add-Ons: AdMob Ads, Firebase Auth, Google Maps, ARCore",
                "Daily Dev Credits: Free credit grants to purchase modules",
                "Claim Promo Code 'dev15': Unlocks all Pro features and perks"
            ),
            actionLabel = "Complete Tutorial & Claim Reward"
        )
    )

    private val _tutorialProgress = MutableStateFlow(loadProgress())
    val tutorialProgress: StateFlow<TutorialProgress> = _tutorialProgress.asStateFlow()

    private fun loadProgress(): TutorialProgress {
        val completedSet = prefs.getStringSet(KEY_COMPLETED_STEPS, emptySet()) ?: emptySet()
        val isDone = prefs.getBoolean(KEY_IS_COMPLETED, false)
        val rewardClaimed = prefs.getBoolean(KEY_REWARD_CLAIMED, false)
        val hasDismissed = prefs.getBoolean(KEY_DISMISSED_INTRO, false)
        val currentIdx = prefs.getInt(KEY_CURRENT_STEP_IDX, 0)

        return TutorialProgress(
            completedStepIds = completedSet,
            currentStepIndex = currentIdx,
            isCompleted = isDone || completedSet.size >= tutorialSteps.size,
            rewardClaimed = rewardClaimed,
            hasDismissedIntro = hasDismissed
        )
    }

    fun markStepCompleted(stepId: String) {
        val currentSet = _tutorialProgress.value.completedStepIds.toMutableSet().apply { add(stepId) }
        val isAllCompleted = currentSet.size >= tutorialSteps.size

        prefs.edit()
            .putStringSet(KEY_COMPLETED_STEPS, currentSet)
            .putBoolean(KEY_IS_COMPLETED, isAllCompleted)
            .apply()

        _tutorialProgress.value = _tutorialProgress.value.copy(
            completedStepIds = currentSet,
            isCompleted = isAllCompleted
        )
    }

    fun setStepIndex(index: Int) {
        val safeIndex = index.coerceIn(0, tutorialSteps.size - 1)
        prefs.edit().putInt(KEY_CURRENT_STEP_IDX, safeIndex).apply()
        _tutorialProgress.value = _tutorialProgress.value.copy(currentStepIndex = safeIndex)
    }

    fun setDismissedIntro(dismissed: Boolean) {
        prefs.edit().putBoolean(KEY_DISMISSED_INTRO, dismissed).apply()
        _tutorialProgress.value = _tutorialProgress.value.copy(hasDismissedIntro = dismissed)
    }

    fun claimReward(): Boolean {
        if (_tutorialProgress.value.rewardClaimed) return false
        prefs.edit().putBoolean(KEY_REWARD_CLAIMED, true).apply()
        _tutorialProgress.value = _tutorialProgress.value.copy(rewardClaimed = true)
        return true
    }

    fun resetTutorial() {
        prefs.edit()
            .remove(KEY_COMPLETED_STEPS)
            .remove(KEY_IS_COMPLETED)
            .remove(KEY_CURRENT_STEP_IDX)
            .remove(KEY_DISMISSED_INTRO)
            .apply()
        _tutorialProgress.value = loadProgress()
    }

    fun createTutorialSandboxProject(): Pair<AppProject, List<UiComponent>> {
        val project = AppProject(
            id = "tutorial_sandbox_project",
            name = "🚀 Space Explorer: Tutorial Project",
            packageName = "com.tutorial.space.explorer",
            description = "Interactive guided project for mastering the visual canvas, hotkeys, and cloud sync",
            category = "Education",
            versionName = "1.0.0",
            primaryColorHex = "#6366F1",
            secondaryColorHex = "#06B6D4",
            backgroundColorHex = "#0B132B",
            surfaceColorHex = "#1C2541",
            textColorHex = "#F8FAFC",
            isDarkTheme = true,
            iconName = "rocket_launch",
            promptUsed = "Tutorial Sandbox Template",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val components = listOf(
            UiComponent(
                id = "tut_c1",
                projectId = project.id,
                type = ComponentType.HEADER,
                title = "🚀 Mission Control: Mars Orbit",
                subtitle = "Welcome to your first interactive project! Tap components below to customize.",
                orderIndex = 0,
                colorHex = "#6366F1",
                fontSizeSp = 20
            ),
            UiComponent(
                id = "tut_c2",
                projectId = project.id,
                type = ComponentType.METRIC_STAT,
                title = "Telemetry Signal",
                subtitle = "Active Antenna Array",
                stateValue = "99.8% Uplink",
                orderIndex = 1,
                colorHex = "#10B981"
            ),
            UiComponent(
                id = "tut_c3",
                projectId = project.id,
                type = ComponentType.COUNTER_WIDGET,
                title = "Oxygen Resupply Cycles",
                subtitle = "Tap + / - to test live state on canvas",
                stateValue = "4",
                actionType = ComponentAction.INCREMENT_COUNTER,
                orderIndex = 2,
                colorHex = "#06B6D4"
            ),
            UiComponent(
                id = "tut_c4",
                projectId = project.id,
                type = ComponentType.SWITCH,
                title = "Sub-Orbital Shielding Grid",
                subtitle = "Interactive canvas toggle switch",
                stateValue = "true",
                actionType = ComponentAction.TOGGLE_STATE,
                orderIndex = 3,
                colorHex = "#8B5CF6"
            ),
            UiComponent(
                id = "tut_c5",
                projectId = project.id,
                type = ComponentType.BUTTON,
                title = "⚡ Fire Thrusters (Test Toast Action)",
                subtitle = "Demonstrates click event listener",
                actionType = ComponentAction.SHOW_TOAST,
                actionPayload = "🔥 Plasma thrusters engaged at 100% thrust!",
                orderIndex = 4,
                colorHex = "#EC4899"
            )
        )

        return Pair(project, components)
    }

    companion object {
        private const val KEY_COMPLETED_STEPS = "key_completed_steps"
        private const val KEY_IS_COMPLETED = "key_is_completed"
        private const val KEY_REWARD_CLAIMED = "key_reward_claimed"
        private const val KEY_DISMISSED_INTRO = "key_dismissed_intro"
        private const val KEY_CURRENT_STEP_IDX = "key_current_step_idx"

        @Volatile
        private var instance: TutorialManager? = null

        fun getInstance(context: Context): TutorialManager {
            return instance ?: synchronized(this) {
                instance ?: TutorialManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
