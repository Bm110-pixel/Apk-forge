package com.example.data.ai

import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class GeneratedAppResult(
    val project: AppProject,
    val components: List<UiComponent>,
    val generatedSourceCode: String,
    val aiModelUsed: String
)

object AiAppGenerator {

    val PRESET_TEMPLATES = listOf(
        PromptTemplate(
            title = "Simple Calculator",
            prompt = "Build an intuitive responsive calculator with real-time math display, numeric keypad, and operations",
            category = "Utility & Tools",
            icon = "calculate",
            tagColor = "#6366F1",
            isBuiltinTemplate = true,
            templateId = "template_calculator"
        ),
        PromptTemplate(
            title = "Smart To-Do List",
            prompt = "Create a modern task checklist with task input, priority tags, completion switches, and progress gauge",
            category = "Productivity",
            icon = "check_circle",
            tagColor = "#10B981",
            isBuiltinTemplate = true,
            templateId = "template_todo"
        ),
        PromptTemplate(
            title = "Tech & Daily Blog",
            prompt = "Build a modern blog and news reader with hero banner, category filters, article cards, and interactive like counts",
            category = "Media & News",
            icon = "newspaper",
            tagColor = "#3B82F6",
            isBuiltinTemplate = true,
            templateId = "template_blog"
        ),
        PromptTemplate(
            title = "Fitness & Workout Tracker",
            prompt = "Build a fitness rep counter and rest timer app with workout stat cards and quick rep buttons",
            category = "Health & Fitness",
            icon = "fitness_center",
            tagColor = "#06B6D4",
            isBuiltinTemplate = true,
            templateId = "template_fitness"
        ),
        PromptTemplate(
            title = "Cyber Synth Soundboard",
            prompt = "Create a retro synthesizer audio pad app with pitch sliders, pad triggers, and neon dark theme",
            category = "Music & Audio",
            icon = "graphic_eq",
            tagColor = "#8B5CF6",
            isBuiltinTemplate = true,
            templateId = "template_synth"
        ),
        PromptTemplate(
            title = "Crypto & Expense Wallet",
            prompt = "Make a crypto portfolio and budget tracker app with quick expense log, balance cards, and asset chip filters",
            category = "Finance",
            icon = "account_balance_wallet",
            tagColor = "#F59E0B",
            isBuiltinTemplate = true,
            templateId = "template_crypto"
        ),
        PromptTemplate(
            title = "Retro RPG Companion",
            prompt = "Build an RPG tabletop companion with HP/Mana stat meters, D20 dice roller button, inventory list, and level up counter",
            category = "Gaming & Tools",
            icon = "sports_esports",
            tagColor = "#EC4899",
            isBuiltinTemplate = true,
            templateId = "template_rpg"
        )
    )

    suspend fun generateApp(
        userPrompt: String,
        config: AiConfiguration = AiConfiguration()
    ): GeneratedAppResult = withContext(Dispatchers.Default) {
        val cleanPrompt = userPrompt.trim()

        // Check if user requested a specific built-in template
        val lower = cleanPrompt.lowercase()
        when {
            lower.contains("calc") -> return@withContext createCalculatorTemplate(config)
            lower.contains("todo") || (lower.contains("task") && !lower.contains("workout")) -> return@withContext createTodoListTemplate(config)
            lower.contains("blog") || lower.contains("news") || lower.contains("article") -> return@withContext createBlogTemplate(config)
        }

        // Try calling Gemini if API key configured and network active
        if (config.selectedModel != AiModelOption.LOCAL_NANO) {
            val geminiPrompt = buildAiPrompt(cleanPrompt, config)
            val geminiResult = GeminiClient.generateRawText(geminiPrompt)

            if (geminiResult.isSuccess) {
                val rawText = geminiResult.getOrNull().orEmpty()
                val parsed = parseGeminiResponse(rawText, cleanPrompt, config)
                if (parsed != null) {
                    return@withContext parsed
                }
            }
        }

        // Contextual AI Synthesis with custom dataset ingestion
        synthesizeApp(cleanPrompt, config)
    }

    private fun buildAiPrompt(userPrompt: String, config: AiConfiguration): String {
        val datasetCtx = if (config.customDatasetPayload.isNotBlank()) {
            "\nIncorporate this user-provided ${config.customDatasetType.displayName} dataset:\n${config.customDatasetPayload.take(1500)}"
        } else ""

        return """
            You are an expert Android AI Architect (${config.systemPromptPersona}).
            Target Model: ${config.selectedModel.displayName}. Architecture: ${config.architecture.displayName}.
            Generate a full Jetpack Compose Android App schema for: "$userPrompt".
            $datasetCtx
            
            Return ONLY a valid JSON object matching this structure:
            {
              "name": "App Name",
              "packageName": "com.ai.app.example",
              "description": "Concise app summary",
              "category": "Utility",
              "primaryColorHex": "${config.themeMood.primaryHex}",
              "secondaryColorHex": "${config.themeMood.secondaryHex}",
              "backgroundColorHex": "${config.themeMood.bgHex}",
              "components": [
                {
                  "type": "HEADER",
                  "title": "Welcome",
                  "subtitle": "Track your progress",
                  "actionType": "NONE",
                  "actionPayload": "",
                  "stateValue": ""
                },
                {
                  "type": "METRIC_STAT",
                  "title": "Active Streak",
                  "subtitle": "Days completed",
                  "stateValue": "14",
                  "actionType": "NONE"
                },
                {
                  "type": "BUTTON",
                  "title": "Log Activity",
                  "actionType": "INCREMENT_COUNTER",
                  "actionPayload": "Streak updated!"
                }
              ]
            }
            Valid component types: HEADER, TEXT, BUTTON, INPUT_FIELD, CARD, IMAGE_BANNER, SWITCH, SLIDER, PROGRESS_BAR, METRIC_STAT, LIST_VIEW, ACTION_CHIP, BADGE, DIVIDER, COUNTER_WIDGET, RATING_BAR.
            Valid action types: NONE, SHOW_TOAST, INCREMENT_COUNTER, DECREMENT_COUNTER, RESET_COUNTER, TOGGLE_STATE, CALCULATE_SUM, OPEN_URL, SHOW_ALERT.
        """.trimIndent()
    }

    private fun parseGeminiResponse(raw: String, originalPrompt: String, config: AiConfiguration): GeneratedAppResult? {
        try {
            val jsonStart = raw.indexOf('{')
            val jsonEnd = raw.lastIndexOf('}')
            if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) return null
            val jsonStr = raw.substring(jsonStart, jsonEnd + 1)
            val json = JSONObject(jsonStr)

            val name = json.optString("name", "AI Created App")
            val sanitizedPkg = name.lowercase().replace("[^a-z0-9]".toRegex(), "")
            val packageName = json.optString("packageName", "com.aistudio.$sanitizedPkg.app")
            val description = json.optString("description", "Generated with AI APK Builder")
            val category = json.optString("category", "General")
            val primaryColor = json.optString("primaryColorHex", config.themeMood.primaryHex)
            val secondaryColor = json.optString("secondaryColorHex", config.themeMood.secondaryHex)
            val backgroundColor = json.optString("backgroundColorHex", config.themeMood.bgHex)

            val projectId = UUID.randomUUID().toString()
            val project = AppProject(
                id = projectId,
                name = name,
                packageName = packageName,
                description = description,
                category = category,
                primaryColorHex = primaryColor,
                secondaryColorHex = secondaryColor,
                backgroundColorHex = backgroundColor,
                promptUsed = originalPrompt,
                iconName = "ic_apk_package"
            )

            val components = mutableListOf<UiComponent>()
            val compsArray = json.optJSONArray("components") ?: JSONArray()
            for (i in 0 until compsArray.length()) {
                val c = compsArray.getJSONObject(i)
                val typeStr = c.optString("type", "TEXT")
                val compType = try { ComponentType.valueOf(typeStr) } catch (e: Exception) { ComponentType.TEXT }
                val actStr = c.optString("actionType", "NONE")
                val compAct = try { ComponentAction.valueOf(actStr) } catch (e: Exception) { ComponentAction.NONE }

                components.add(
                    UiComponent(
                        id = UUID.randomUUID().toString(),
                        projectId = projectId,
                        type = compType,
                        title = c.optString("title", "Component $i"),
                        subtitle = c.optString("subtitle", ""),
                        stateValue = c.optString("stateValue", ""),
                        placeholder = c.optString("placeholder", ""),
                        actionType = compAct,
                        actionPayload = c.optString("actionPayload", ""),
                        orderIndex = i,
                        colorHex = if (c.has("colorHex")) c.optString("colorHex") else null
                    )
                )
            }

            if (components.isEmpty()) {
                components.addAll(generateFallbackComponents(projectId, name, category, originalPrompt, config))
            }

            val sourceCode = generateJetpackComposeCode(project, components, config)

            return GeneratedAppResult(
                project = project,
                components = components,
                generatedSourceCode = sourceCode,
                aiModelUsed = config.selectedModel.displayName
            )
        } catch (e: Exception) {
            return null
        }
    }

    fun synthesizeApp(userPrompt: String, config: AiConfiguration = AiConfiguration()): GeneratedAppResult {
        val lower = userPrompt.lowercase()
        val projectId = UUID.randomUUID().toString()

        val (name, category, primaryColor, secondaryColor, bgColor) = when {
            lower.contains("calc") -> {
                AppMeta("QuickCalc Pro", "Tools & Math", config.themeMood.primaryHex, config.themeMood.secondaryHex, config.themeMood.bgHex)
            }
            lower.contains("todo") || lower.contains("task") -> {
                AppMeta("TaskMatrix To-Do", "Productivity", "#10B981", "#06B6D4", "#0A121A")
            }
            lower.contains("blog") || lower.contains("news") -> {
                AppMeta("TechWave Daily Blog", "News & Media", "#3B82F6", "#8B5CF6", "#0B132B")
            }
            lower.contains("fit") || lower.contains("workout") || lower.contains("gym") || lower.contains("rep") -> {
                AppMeta("Titan Fit Tracker", "Health & Fitness", "#10B981", "#06B6D4", "#091218")
            }
            lower.contains("music") || lower.contains("synth") || lower.contains("sound") || lower.contains("audio") || lower.contains("beat") -> {
                AppMeta("Neon Synth Wave", "Music & Audio", "#8B5CF6", "#EC4899", "#110E1B")
            }
            lower.contains("crypto") || lower.contains("finance") || lower.contains("money") || lower.contains("budget") || lower.contains("wallet") -> {
                AppMeta("Apex Crypto Wallet", "Finance", "#F59E0B", "#10B981", "#12141A")
            }
            lower.contains("habit") || lower.contains("streak") || lower.contains("daily") || lower.contains("routine") -> {
                AppMeta("Momentum Habits", "Productivity", "#06B6D4", "#6366F1", "#0A101D")
            }
            lower.contains("game") || lower.contains("rpg") || lower.contains("dice") || lower.contains("score") || lower.contains("arcade") -> {
                AppMeta("QuestMaster RPG Tools", "Gaming", "#EC4899", "#F59E0B", "#150B18")
            }
            lower.contains("recipe") || lower.contains("cook") || lower.contains("food") || lower.contains("kitchen") -> {
                AppMeta("ChefCraft Recipes", "Food & Drink", "#EF4444", "#F59E0B", "#1A0C0C")
            }
            lower.contains("weather") || lower.contains("forecast") || lower.contains("climate") -> {
                AppMeta("Aero Weather Live", "Weather", "#0284C7", "#38BDF8", "#081524")
            }
            else -> {
                val words = userPrompt.split(" ").filter { it.length > 2 }.take(2)
                val derivedName = if (words.isNotEmpty()) words.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } + " App" else "Smart AI Studio App"
                AppMeta(derivedName, "Utility", config.themeMood.primaryHex, config.themeMood.secondaryHex, config.themeMood.bgHex)
            }
        }

        val sanitizedPkg = name.lowercase().replace("[^a-z0-9]".toRegex(), "")
        val project = AppProject(
            id = projectId,
            name = name,
            packageName = "com.aistudio.$sanitizedPkg.app",
            description = "AI generated Android application tailored for: $userPrompt",
            category = category,
            primaryColorHex = primaryColor,
            secondaryColorHex = secondaryColor,
            backgroundColorHex = bgColor,
            surfaceColorHex = "#1E293B",
            promptUsed = userPrompt,
            iconName = "ic_apk_package"
        )

        val components = generateFallbackComponents(projectId, name, category, userPrompt, config)
        val sourceCode = generateJetpackComposeCode(project, components, config)

        return GeneratedAppResult(
            project = project,
            components = components,
            generatedSourceCode = sourceCode,
            aiModelUsed = config.selectedModel.displayName
        )
    }

    // ================= BUILTIN TEMPLATES =================

    fun createCalculatorTemplate(config: AiConfiguration = AiConfiguration()): GeneratedAppResult {
        val projectId = UUID.randomUUID().toString()
        val project = AppProject(
            id = projectId,
            name = "QuickCalc Pro",
            packageName = "com.aistudio.quickcalc.app",
            description = "Responsive scientific & arithmetic calculator with live display and memory functions",
            category = "Tools & Math",
            primaryColorHex = "#6366F1",
            secondaryColorHex = "#06B6D4",
            backgroundColorHex = "#0F172A",
            surfaceColorHex = "#1E293B",
            promptUsed = "Build a simple calculator with number pad and operations",
            iconName = "calculate"
        )

        val components = listOf(
            UiComponent(
                projectId = projectId,
                type = ComponentType.HEADER,
                title = "QuickCalc Pro",
                subtitle = "Scientific Precision Engine",
                orderIndex = 0
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.METRIC_STAT,
                title = "Calculation Result",
                subtitle = "Formula: 128 × 4.5 + 24",
                stateValue = "600",
                colorHex = "#6366F1",
                orderIndex = 1
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.INPUT_FIELD,
                title = "Active Expression",
                placeholder = "Tap buttons or type formula...",
                stateValue = "128 * 4.5 + 24",
                orderIndex = 2
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.BUTTON,
                title = "[ 7 ] [ 8 ] [ 9 ] [ ÷ ]",
                subtitle = "Upper Keypad Row",
                actionType = ComponentAction.CALCULATE_SUM,
                actionPayload = "DIVIDE",
                orderIndex = 3
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.BUTTON,
                title = "[ 4 ] [ 5 ] [ 6 ] [ × ]",
                subtitle = "Middle Keypad Row",
                actionType = ComponentAction.CALCULATE_SUM,
                actionPayload = "MULTIPLY",
                orderIndex = 4
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.BUTTON,
                title = "[ 1 ] [ 2 ] [ 3 ] [ - ]",
                subtitle = "Lower Keypad Row",
                actionType = ComponentAction.CALCULATE_SUM,
                actionPayload = "SUBTRACT",
                orderIndex = 5
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.BUTTON,
                title = "[ C Clear ] [ 0 ] [ = Calculate ] [ + ]",
                subtitle = "Execute Calculation",
                actionType = ComponentAction.CALCULATE_SUM,
                actionPayload = "EVAL",
                colorHex = "#10B981",
                orderIndex = 6
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.SWITCH,
                title = "Scientific Radians / Degree Mode",
                stateValue = "true",
                actionType = ComponentAction.TOGGLE_STATE,
                orderIndex = 7
            )
        )

        val code = generateJetpackComposeCode(project, components, config)
        return GeneratedAppResult(project, components, code, "QuickCalc Template Engine")
    }

    fun createTodoListTemplate(config: AiConfiguration = AiConfiguration()): GeneratedAppResult {
        val projectId = UUID.randomUUID().toString()
        val project = AppProject(
            id = projectId,
            name = "TaskMatrix To-Do",
            packageName = "com.aistudio.taskmatrix.app",
            description = "Clean task management app with priority filters, progress tracking, and instant checkmarks",
            category = "Productivity",
            primaryColorHex = "#10B981",
            secondaryColorHex = "#06B6D4",
            backgroundColorHex = "#0A121A",
            surfaceColorHex = "#16222F",
            promptUsed = "Build a modern to-do list app with task checkboxes and progress tracking",
            iconName = "check_circle"
        )

        val components = listOf(
            UiComponent(
                projectId = projectId,
                type = ComponentType.HEADER,
                title = "My Daily Tasks & Goals",
                subtitle = "Stay organized and track your daily momentum",
                orderIndex = 0
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.METRIC_STAT,
                title = "Tasks Completed Today",
                subtitle = "Goal: 5 Tasks • 80% Complete",
                stateValue = "4 / 5",
                colorHex = "#10B981",
                orderIndex = 1
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.PROGRESS_BAR,
                title = "Overall Progress",
                subtitle = "Almost done with today's objectives!",
                stateValue = "80",
                orderIndex = 2
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.INPUT_FIELD,
                title = "Add New Task",
                placeholder = "Type task name and press add...",
                stateValue = "",
                orderIndex = 3
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.BUTTON,
                title = "+ Add Task to Checklist",
                actionType = ComponentAction.INCREMENT_COUNTER,
                actionPayload = "New Task Added to List!",
                colorHex = "#10B981",
                orderIndex = 4
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.ACTION_CHIP,
                title = "🔥 High Priority • Work • Personal",
                stateValue = "Work",
                orderIndex = 5
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.SWITCH,
                title = "✓ Complete APK Build Architecture",
                subtitle = "Due today @ 4:00 PM • Priority High",
                stateValue = "true",
                actionType = ComponentAction.TOGGLE_STATE,
                orderIndex = 6
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.SWITCH,
                title = "✓ Review Jetpack Compose Layouts",
                subtitle = "Design system & color tokens check",
                stateValue = "true",
                actionType = ComponentAction.TOGGLE_STATE,
                orderIndex = 7
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.SWITCH,
                title = "○ Test on Android 15 Real Device",
                subtitle = "Verify edge-to-edge insets & touch targets",
                stateValue = "false",
                actionType = ComponentAction.TOGGLE_STATE,
                orderIndex = 8
            )
        )

        val code = generateJetpackComposeCode(project, components, config)
        return GeneratedAppResult(project, components, code, "TaskMatrix Template Engine")
    }

    fun createBlogTemplate(config: AiConfiguration = AiConfiguration()): GeneratedAppResult {
        val projectId = UUID.randomUUID().toString()
        val project = AppProject(
            id = projectId,
            name = "TechWave Daily Blog",
            packageName = "com.aistudio.techwave.blog",
            description = "Curated mobile tech, Kotlin and Android development articles with modern reading feed",
            category = "Media & News",
            primaryColorHex = "#3B82F6",
            secondaryColorHex = "#8B5CF6",
            backgroundColorHex = "#0B132B",
            surfaceColorHex = "#1C2541",
            promptUsed = "Build a basic blog and article reader app",
            iconName = "newspaper"
        )

        val components = listOf(
            UiComponent(
                projectId = projectId,
                type = ComponentType.HEADER,
                title = "TechWave Magazine",
                subtitle = "Insights into Android, Kotlin & Mobile AI",
                orderIndex = 0
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.ACTION_CHIP,
                title = "⚡ All Posts • Jetpack Compose • Kotlin • AI Studio",
                stateValue = "Jetpack Compose",
                orderIndex = 1
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.CARD,
                title = "Building Dynamic Android APKs with On-Device AI",
                subtitle = "Explore how modern declarative UI and generative neural models compile native APKs directly in your hand. 5 min read • by Alex Rivera",
                stateValue = "Featured",
                orderIndex = 2
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.METRIC_STAT,
                title = "Article Reader Engagement",
                subtitle = "+340 claps & comments this morning",
                stateValue = "1,420 Reads",
                colorHex = "#3B82F6",
                orderIndex = 3
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.BUTTON,
                title = "👏 Like & Bookmark Article (+1)",
                actionType = ComponentAction.INCREMENT_COUNTER,
                actionPayload = "Thanks for reading! Article saved to bookmarks.",
                colorHex = "#3B82F6",
                orderIndex = 4
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.CARD,
                title = "Material 3 Expressive & Android 15 Edge-to-Edge",
                subtitle = "A deep dive into dynamic color palettes, tactile spring animations, and immersive insets. 4 min read",
                stateValue = "Trending",
                orderIndex = 5
            ),
            UiComponent(
                projectId = projectId,
                type = ComponentType.INPUT_FIELD,
                title = "Leave a Comment / Response",
                placeholder = "Share your thoughts on this story...",
                stateValue = "",
                orderIndex = 6
            )
        )

        val code = generateJetpackComposeCode(project, components, config)
        return GeneratedAppResult(project, components, code, "TechWave Template Engine")
    }

    private data class AppMeta(
        val name: String,
        val category: String,
        val primaryColor: String,
        val secondaryColor: String,
        val bgColor: String
    )

    fun generateFallbackComponents(
        projectId: String,
        appName: String,
        category: String,
        prompt: String,
        config: AiConfiguration
    ): List<UiComponent> {
        val lower = prompt.lowercase()
        val list = mutableListOf<UiComponent>()

        // If user uploaded a custom dataset, incorporate data rows!
        if (config.customDatasetPayload.isNotBlank()) {
            list.add(UiComponent(projectId = projectId, type = ComponentType.HEADER, title = appName, subtitle = "Custom Data-Driven Application", orderIndex = 0))
            list.add(UiComponent(projectId = projectId, type = ComponentType.METRIC_STAT, title = "Dataset Ingestion Status", subtitle = "${config.customDatasetType.displayName} Active", stateValue = "Verified & Synced", orderIndex = 1))
            list.add(UiComponent(projectId = projectId, type = ComponentType.CARD, title = "Live Dataset Schema", subtitle = config.customDatasetPayload.take(120), orderIndex = 2))
            list.add(UiComponent(projectId = projectId, type = ComponentType.BUTTON, title = "Sync & Query Dataset", actionType = ComponentAction.SHOW_TOAST, actionPayload = "Dataset queried successfully!", orderIndex = 3))
            list.add(UiComponent(projectId = projectId, type = ComponentType.INPUT_FIELD, title = "Filter Records", placeholder = "Search schema records...", stateValue = "", orderIndex = 4))
            list.add(UiComponent(projectId = projectId, type = ComponentType.SWITCH, title = "Auto-Refresh Live Feed", stateValue = "true", actionType = ComponentAction.TOGGLE_STATE, orderIndex = 5))
            return list
        }

        when {
            lower.contains("fit") || lower.contains("workout") || lower.contains("rep") -> {
                list.add(UiComponent(projectId = projectId, type = ComponentType.HEADER, title = appName, subtitle = "High Intensity Training & Rep Log", orderIndex = 0))
                list.add(UiComponent(projectId = projectId, type = ComponentType.METRIC_STAT, title = "Total Reps Done", subtitle = "Today's Volume", stateValue = "48", actionType = ComponentAction.NONE, orderIndex = 1))
                list.add(UiComponent(projectId = projectId, type = ComponentType.COUNTER_WIDGET, title = "Current Set Reps", stateValue = "12", actionType = ComponentAction.INCREMENT_COUNTER, orderIndex = 2))
                list.add(UiComponent(projectId = projectId, type = ComponentType.BUTTON, title = "+ Add 5 Reps Bonus", actionType = ComponentAction.INCREMENT_COUNTER, actionPayload = "Awesome set completed!", orderIndex = 3))
                list.add(UiComponent(projectId = projectId, type = ComponentType.PROGRESS_BAR, title = "Daily Workout Goal", stateValue = "75", subtitle = "3/4 Sets Completed", orderIndex = 4))
                list.add(UiComponent(projectId = projectId, type = ComponentType.SWITCH, title = "Rest Timer Sound Alerts", stateValue = "true", actionType = ComponentAction.TOGGLE_STATE, orderIndex = 5))
                list.add(UiComponent(projectId = projectId, type = ComponentType.CARD, title = "Next Exercise: Dumbbell Bench Press", subtitle = "Target: 4 sets x 10 reps @ 24kg", orderIndex = 6))
                list.add(UiComponent(projectId = projectId, type = ComponentType.INPUT_FIELD, title = "Workout Notes", placeholder = "Felt great on final dropset...", stateValue = "", orderIndex = 7))
            }
            lower.contains("music") || lower.contains("synth") || lower.contains("audio") || lower.contains("sound") -> {
                list.add(UiComponent(projectId = projectId, type = ComponentType.HEADER, title = appName, subtitle = "8-Bit & Analog Sound Synthesizer", orderIndex = 0))
                list.add(UiComponent(projectId = projectId, type = ComponentType.ACTION_CHIP, title = "Lead Synth • 128 BPM", stateValue = "Active", orderIndex = 1))
                list.add(UiComponent(projectId = projectId, type = ComponentType.SLIDER, title = "Filter Resonance & Cutoff", stateValue = "68", subtitle = "Freq: 2.4 kHz", orderIndex = 2))
                list.add(UiComponent(projectId = projectId, type = ComponentType.SLIDER, title = "Reverb & Delay Feedback", stateValue = "42", subtitle = "Decay: 1.8s", orderIndex = 3))
                list.add(UiComponent(projectId = projectId, type = ComponentType.BUTTON, title = "▶ Play Synth Beat Loop", actionType = ComponentAction.SHOW_TOAST, actionPayload = "Synthesizer Loop Started!", orderIndex = 4))
                list.add(UiComponent(projectId = projectId, type = ComponentType.COUNTER_WIDGET, title = "Oscillator Octave", stateValue = "3", actionType = ComponentAction.INCREMENT_COUNTER, orderIndex = 5))
                list.add(UiComponent(projectId = projectId, type = ComponentType.SWITCH, title = "Analog Distortion Tube", stateValue = "true", actionType = ComponentAction.TOGGLE_STATE, orderIndex = 6))
                list.add(UiComponent(projectId = projectId, type = ComponentType.CARD, title = "Sound Engine Preset: Cyber Glitch #04", subtitle = "Polyphony: 8 voices | Low Latency Buffer", orderIndex = 7))
            }
            lower.contains("crypto") || lower.contains("finance") || lower.contains("wallet") -> {
                list.add(UiComponent(projectId = projectId, type = ComponentType.HEADER, title = appName, subtitle = "Multi-Chain Asset Vault & Balances", orderIndex = 0))
                list.add(UiComponent(projectId = projectId, type = ComponentType.METRIC_STAT, title = "Total Balance", subtitle = "+8.4% (24h)", stateValue = "$18,420.50", orderIndex = 1))
                list.add(UiComponent(projectId = projectId, type = ComponentType.ACTION_CHIP, title = "Bitcoin • $96,400 | Ethereum • $3,450", stateValue = "Bullish", orderIndex = 2))
                list.add(UiComponent(projectId = projectId, type = ComponentType.BUTTON, title = "⚡ Quick Swap / Send Tokens", actionType = ComponentAction.SHOW_TOAST, actionPayload = "Instant Transaction Initiated", orderIndex = 3))
                list.add(UiComponent(projectId = projectId, type = ComponentType.INPUT_FIELD, title = "Amount (USD)", placeholder = "Enter USD amount...", stateValue = "500", orderIndex = 4))
                list.add(UiComponent(projectId = projectId, type = ComponentType.SWITCH, title = "Price Volatility Alerts", stateValue = "true", actionType = ComponentAction.TOGGLE_STATE, orderIndex = 5))
                list.add(UiComponent(projectId = projectId, type = ComponentType.CARD, title = "Recent Tx: Received +0.15 ETH", subtitle = "From 0x71C...89A • Confirmed 2m ago", orderIndex = 6))
            }
            lower.contains("game") || lower.contains("rpg") || lower.contains("dice") -> {
                list.add(UiComponent(projectId = projectId, type = ComponentType.HEADER, title = appName, subtitle = "Tabletop RPG Companion & Sheet", orderIndex = 0))
                list.add(UiComponent(projectId = projectId, type = ComponentType.METRIC_STAT, title = "Character Level", subtitle = "Paladin of Light", stateValue = "Level 7", orderIndex = 1))
                list.add(UiComponent(projectId = projectId, type = ComponentType.COUNTER_WIDGET, title = "Hit Points (HP)", stateValue = "68", actionType = ComponentAction.INCREMENT_COUNTER, orderIndex = 2))
                list.add(UiComponent(projectId = projectId, type = ComponentType.PROGRESS_BAR, title = "Mana Spell Slots", subtitle = "4/6 Slots Remaining", stateValue = "66", orderIndex = 3))
                list.add(UiComponent(projectId = projectId, type = ComponentType.BUTTON, title = "🎲 Roll 1d20 + Modifiers", actionType = ComponentAction.SHOW_TOAST, actionPayload = "Rolled Natural 19! Critical Hit!", orderIndex = 4))
                list.add(UiComponent(projectId = projectId, type = ComponentType.CARD, title = "Equipped Weapon: Sunblade +2", subtitle = "Damage: 1d8+4 Radiant | Advantage on Fiends", orderIndex = 5))
                list.add(UiComponent(projectId = projectId, type = ComponentType.SWITCH, title = "Concentration Spell Active", stateValue = "true", actionType = ComponentAction.TOGGLE_STATE, orderIndex = 6))
            }
            else -> {
                list.add(UiComponent(projectId = projectId, type = ComponentType.HEADER, title = appName, subtitle = "AI-Crafted Application Interface", orderIndex = 0))
                list.add(UiComponent(projectId = projectId, type = ComponentType.METRIC_STAT, title = "Activity Status", subtitle = "System Ready", stateValue = "Active", orderIndex = 1))
                list.add(UiComponent(projectId = projectId, type = ComponentType.COUNTER_WIDGET, title = "Interactive Counter", stateValue = "10", actionType = ComponentAction.INCREMENT_COUNTER, orderIndex = 2))
                list.add(UiComponent(projectId = projectId, type = ComponentType.BUTTON, title = "Execute Primary Action", actionType = ComponentAction.SHOW_TOAST, actionPayload = "Action completed successfully!", orderIndex = 3))
                list.add(UiComponent(projectId = projectId, type = ComponentType.INPUT_FIELD, title = "Search or Enter Data", placeholder = "Type custom query here...", stateValue = "", orderIndex = 4))
                list.add(UiComponent(projectId = projectId, type = ComponentType.PROGRESS_BAR, title = "Operation Progress", subtitle = "Syncing local database", stateValue = "85", orderIndex = 5))
                list.add(UiComponent(projectId = projectId, type = ComponentType.SWITCH, title = "Live Cloud Synchronization", stateValue = "true", actionType = ComponentAction.TOGGLE_STATE, orderIndex = 6))
                list.add(UiComponent(projectId = projectId, type = ComponentType.CARD, title = "Summary & Insights", subtitle = "All app components rendered dynamically and ready to package into standalone APK.", orderIndex = 7))
            }
        }

        return list
    }

    fun generateJetpackComposeCode(
        project: AppProject,
        components: List<UiComponent>,
        config: AiConfiguration = AiConfiguration()
    ): String {
        val sb = StringBuilder()
        sb.append("package ${project.packageName}\n\n")
        sb.append("import android.os.Bundle\n")
        sb.append("import android.widget.Toast\n")
        sb.append("import androidx.activity.ComponentActivity\n")
        sb.append("import androidx.activity.compose.setContent\n")
        sb.append("import androidx.activity.enableEdgeToEdge\n")
        sb.append("import androidx.compose.foundation.background\n")
        sb.append("import androidx.compose.foundation.layout.*\n")
        sb.append("import androidx.compose.foundation.lazy.LazyColumn\n")
        sb.append("import androidx.compose.foundation.lazy.items\n")
        sb.append("import androidx.compose.foundation.shape.RoundedCornerShape\n")
        sb.append("import androidx.compose.material3.*\n")
        sb.append("import androidx.compose.runtime.*\n")
        sb.append("import androidx.compose.ui.Alignment\n")
        sb.append("import androidx.compose.ui.Modifier\n")
        sb.append("import androidx.compose.ui.graphics.Color\n")
        sb.append("import androidx.compose.ui.platform.LocalContext\n")
        sb.append("import androidx.compose.ui.text.font.FontWeight\n")
        sb.append("import androidx.compose.ui.unit.dp\n")
        sb.append("import androidx.compose.ui.unit.sp\n\n")

        sb.append("// Generated by AI APK Builder for Android (${config.selectedModel.displayName})\n")
        sb.append("// App: ${project.name} (${project.versionName}) | Target SDK: ${config.targetSdk}\n\n")

        sb.append("class MainActivity : ComponentActivity() {\n")
        sb.append("    override fun onCreate(savedInstanceState: Bundle?) {\n")
        sb.append("        super.onCreate(savedInstanceState)\n")
        sb.append("        enableEdgeToEdge()\n")
        sb.append("        setContent {\n")
        sb.append("            MaterialTheme(\n")
        sb.append("                colorScheme = darkColorScheme(\n")
        sb.append("                    primary = Color(android.graphics.Color.parseColor(\"${project.primaryColorHex}\")),\n")
        sb.append("                    secondary = Color(android.graphics.Color.parseColor(\"${project.secondaryColorHex}\")),\n")
        sb.append("                    background = Color(android.graphics.Color.parseColor(\"${project.backgroundColorHex}\"))\n")
        sb.append("                )\n")
        sb.append("            ) {\n")
        sb.append("                AppScreen()\n")
        sb.append("            }\n")
        sb.append("        }\n")
        sb.append("    }\n")
        sb.append("}\n\n")

        sb.append("@OptIn(ExperimentalMaterial3Api::class)\n")
        sb.append("@Composable\n")
        sb.append("fun AppScreen() {\n")
        sb.append("    val context = LocalContext.current\n")
        sb.append("    var counterState by remember { mutableIntStateOf(10) }\n")
        sb.append("    var switchState by remember { mutableStateOf(true) }\n")
        sb.append("    var sliderState by remember { mutableFloatStateOf(50f) }\n")
        sb.append("    var textInputState by remember { mutableStateOf(\"\") }\n\n")

        sb.append("    Scaffold(\n")
        sb.append("        topBar = {\n")
        sb.append("            TopAppBar(\n")
        sb.append("                title = { Text(\"${project.name}\", fontWeight = FontWeight.Bold) },\n")
        sb.append("                colors = TopAppBarDefaults.topAppBarColors(\n")
        sb.append("                    containerColor = Color(android.graphics.Color.parseColor(\"${project.backgroundColorHex}\")),\n")
        sb.append("                    titleContentColor = Color.White\n")
        sb.append("                )\n")
        sb.append("            )\n")
        sb.append("        },\n")
        sb.append("        containerColor = Color(android.graphics.Color.parseColor(\"${project.backgroundColorHex}\"))\n")
        sb.append("    ) { padding ->\n")
        sb.append("        LazyColumn(\n")
        sb.append("            modifier = Modifier\n")
        sb.append("                .fillMaxSize()\n")
        sb.append("                .padding(padding)\n")
        sb.append("                .padding(horizontal = 16.dp),\n")
        sb.append("            verticalArrangement = Arrangement.spacedBy(14.dp)\n")
        sb.append("        ) {\n")

        for (comp in components) {
            sb.append("            item {\n")
            when (comp.type) {
                ComponentType.HEADER -> {
                    sb.append("                Column(modifier = Modifier.padding(vertical = 8.dp)) {\n")
                    sb.append("                    Text(\"${comp.title}\", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)\n")
                    if (comp.subtitle.isNotBlank()) {
                        sb.append("                    Text(\"${comp.subtitle}\", fontSize = 14.sp, color = Color.Gray)\n")
                    }
                    sb.append("                }\n")
                }
                ComponentType.METRIC_STAT -> {
                    sb.append("                Card(\n")
                    sb.append("                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),\n")
                    sb.append("                    shape = RoundedCornerShape(16.dp),\n")
                    sb.append("                    modifier = Modifier.fillMaxWidth()\n")
                    sb.append("                ) {\n")
                    sb.append("                    Column(modifier = Modifier.padding(16.dp)) {\n")
                    sb.append("                        Text(\"${comp.title}\", fontSize = 13.sp, color = Color.LightGray)\n")
                    sb.append("                        Text(\"${comp.stateValue}\", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(android.graphics.Color.parseColor(\"${project.primaryColorHex}\")))\n")
                    if (comp.subtitle.isNotBlank()) {
                        sb.append("                        Text(\"${comp.subtitle}\", fontSize = 12.sp, color = Color.Gray)\n")
                    }
                    sb.append("                    }\n")
                    sb.append("                }\n")
                }
                ComponentType.COUNTER_WIDGET -> {
                    sb.append("                Card(\n")
                    sb.append("                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),\n")
                    sb.append("                    shape = RoundedCornerShape(16.dp),\n")
                    sb.append("                    modifier = Modifier.fillMaxWidth()\n")
                    sb.append("                ) {\n")
                    sb.append("                    Row(\n")
                    sb.append("                        modifier = Modifier.padding(16.dp).fillMaxWidth(),\n")
                    sb.append("                        horizontalArrangement = Arrangement.SpaceBetween,\n")
                    sb.append("                        verticalAlignment = Alignment.CenterVertically\n")
                    sb.append("                    ) {\n")
                    sb.append("                        Text(\"${comp.title}\", fontWeight = FontWeight.SemiBold, color = Color.White)\n")
                    sb.append("                        Row(verticalAlignment = Alignment.CenterVertically) {\n")
                    sb.append("                            FilledTonalButton(onClick = { if (counterState > 0) counterState-- }) { Text(\"-\") }\n")
                    sb.append("                            Text(\"\$counterState\", modifier = Modifier.padding(horizontal = 14.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)\n")
                    sb.append("                            FilledTonalButton(onClick = { counterState++ }) { Text(\"+\") }\n")
                    sb.append("                        }\n")
                    sb.append("                    }\n")
                    sb.append("                }\n")
                }
                ComponentType.BUTTON -> {
                    sb.append("                Button(\n")
                    sb.append("                    onClick = {\n")
                    sb.append("                        Toast.makeText(context, \"${if (comp.actionPayload.isNotBlank()) comp.actionPayload else comp.title}\", Toast.LENGTH_SHORT).show()\n")
                    sb.append("                    },\n")
                    sb.append("                    modifier = Modifier.fillMaxWidth().height(52.dp),\n")
                    sb.append("                    shape = RoundedCornerShape(12.dp)\n")
                    sb.append("                ) {\n")
                    sb.append("                    Text(\"${comp.title}\", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)\n")
                    sb.append("                }\n")
                }
                ComponentType.SWITCH -> {
                    sb.append("                Card(\n")
                    sb.append("                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),\n")
                    sb.append("                    shape = RoundedCornerShape(14.dp),\n")
                    sb.append("                    modifier = Modifier.fillMaxWidth()\n")
                    sb.append("                ) {\n")
                    sb.append("                    Row(\n")
                    sb.append("                        modifier = Modifier.padding(16.dp).fillMaxWidth(),\n")
                    sb.append("                        horizontalArrangement = Arrangement.SpaceBetween,\n")
                    sb.append("                        verticalAlignment = Alignment.CenterVertically\n")
                    sb.append("                    ) {\n")
                    sb.append("                        Text(\"${comp.title}\", color = Color.White)\n")
                    sb.append("                        Switch(checked = switchState, onCheckedChange = { switchState = it })\n")
                    sb.append("                    }\n")
                    sb.append("                }\n")
                }
                ComponentType.SLIDER -> {
                    sb.append("                Card(\n")
                    sb.append("                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),\n")
                    sb.append("                    shape = RoundedCornerShape(14.dp),\n")
                    sb.append("                    modifier = Modifier.fillMaxWidth()\n")
                    sb.append("                ) {\n")
                    sb.append("                    Column(modifier = Modifier.padding(16.dp)) {\n")
                    sb.append("                        Text(\"${comp.title}: \${sliderState.toInt()}\", color = Color.White)\n")
                    sb.append("                        Slider(value = sliderState, onValueChange = { sliderState = it }, valueRange = 0f..100f)\n")
                    sb.append("                    }\n")
                    sb.append("                }\n")
                }
                ComponentType.PROGRESS_BAR -> {
                    sb.append("                Card(\n")
                    sb.append("                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),\n")
                    sb.append("                    shape = RoundedCornerShape(14.dp),\n")
                    sb.append("                    modifier = Modifier.fillMaxWidth()\n")
                    sb.append("                ) {\n")
                    sb.append("                    Column(modifier = Modifier.padding(16.dp)) {\n")
                    sb.append("                        Text(\"${comp.title}\", color = Color.White, fontWeight = FontWeight.Medium)\n")
                    sb.append("                        Spacer(modifier = Modifier.height(8.dp))\n")
                    sb.append("                        LinearProgressIndicator(progress = { 0.75f }, modifier = Modifier.fillMaxWidth())\n")
                    sb.append("                        Spacer(modifier = Modifier.height(4.dp))\n")
                    sb.append("                        Text(\"${comp.subtitle}\", fontSize = 12.sp, color = Color.Gray)\n")
                    sb.append("                    }\n")
                    sb.append("                }\n")
                }
                ComponentType.INPUT_FIELD -> {
                    sb.append("                OutlinedTextField(\n")
                    sb.append("                    value = textInputState,\n")
                    sb.append("                    onValueChange = { textInputState = it },\n")
                    sb.append("                    label = { Text(\"${comp.title}\") },\n")
                    sb.append("                    placeholder = { Text(\"${comp.placeholder}\") },\n")
                    sb.append("                    modifier = Modifier.fillMaxWidth(),\n")
                    sb.append("                    shape = RoundedCornerShape(12.dp)\n")
                    sb.append("                )\n")
                }
                else -> {
                    sb.append("                Card(\n")
                    sb.append("                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),\n")
                    sb.append("                    shape = RoundedCornerShape(14.dp),\n")
                    sb.append("                    modifier = Modifier.fillMaxWidth()\n")
                    sb.append("                ) {\n")
                    sb.append("                    Column(modifier = Modifier.padding(16.dp)) {\n")
                    sb.append("                        Text(\"${comp.title}\", fontWeight = FontWeight.Bold, color = Color.White)\n")
                    if (comp.subtitle.isNotBlank()) {
                        sb.append("                        Text(\"${comp.subtitle}\", fontSize = 13.sp, color = Color.Gray)\n")
                    }
                    sb.append("                    }\n")
                    sb.append("                }\n")
                }
            }
            sb.append("            }\n")
        }

        sb.append("            item { Spacer(modifier = Modifier.height(24.dp)) }\n")
        sb.append("        }\n")
        sb.append("    }\n")
        sb.append("}\n")

        return sb.toString()
    }
}
