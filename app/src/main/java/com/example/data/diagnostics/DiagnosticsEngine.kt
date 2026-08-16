package com.example.data.diagnostics

import com.example.data.model.DiagnosticLog
import com.example.data.model.LogCategory
import com.example.data.model.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object DiagnosticsEngine {

    private val _logs = MutableStateFlow<List<DiagnosticLog>>(createInitialLogs())
    val logs: StateFlow<List<DiagnosticLog>> = _logs.asStateFlow()

    private fun createInitialLogs(): List<DiagnosticLog> {
        val now = System.currentTimeMillis()
        return listOf(
            DiagnosticLog(
                id = UUID.randomUUID().toString(),
                timestamp = now - 180000,
                level = LogLevel.SUCCESS,
                category = LogCategory.COMPILER,
                tag = "GradleDaemon",
                message = "Gradle build daemon initialized (Kotlin 2.0.21, AGP 8.6.0)",
                isResolved = true
            ),
            DiagnosticLog(
                id = UUID.randomUUID().toString(),
                timestamp = now - 140000,
                level = LogLevel.INFO,
                category = LogCategory.RUNTIME,
                tag = "ComposeCompiler",
                message = "Smart recomposition cache active. 14 composable lambdas memoized.",
                isResolved = true
            ),
            DiagnosticLog(
                id = UUID.randomUUID().toString(),
                timestamp = now - 95000,
                level = LogLevel.WARN,
                category = LogCategory.SYNTAX,
                tag = "ComposeLint",
                message = "Missing accessibility contentDescription on custom IconButton widget",
                stackTrace = "at com.example.ui.components.CustomIconBtn(Unknown Source:24)\nat androidx.compose.ui.Modifier.semantics()",
                suggestedAiFix = "Added default contentDescription = \"Action button\" to semantics node.",
                isResolved = false
            ),
            DiagnosticLog(
                id = UUID.randomUUID().toString(),
                timestamp = now - 45000,
                level = LogLevel.ERROR,
                category = LogCategory.COMPILER,
                tag = "Aapt2Error",
                message = "Resource string 'app_version_label' contains unescaped single quote (')",
                stackTrace = "com.android.aapt2.Aapt2Exception: Resource compilation failed\n  File: res/values/strings.xml:18:14\n  Error: unescaped apostrophe in <string>",
                suggestedAiFix = "Escape the apostrophe with backslash: \\' or wrap string in double quotes.",
                isResolved = false
            ),
            DiagnosticLog(
                id = UUID.randomUUID().toString(),
                timestamp = now - 15000,
                level = LogLevel.INFO,
                category = LogCategory.NETWORK,
                tag = "GeminiClient",
                message = "Synthesizer stream completed with HTTP 200 OK (latency 312ms)",
                isResolved = true
            )
        )
    }

    fun log(level: LogLevel, category: LogCategory, tag: String, message: String, stackTrace: String? = null, suggestedAiFix: String? = null) {
        val newLog = DiagnosticLog(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            level = level,
            category = category,
            tag = tag,
            message = message,
            stackTrace = stackTrace,
            suggestedAiFix = suggestedAiFix,
            isResolved = false
        )
        _logs.value = listOf(newLog) + _logs.value
    }

    fun logError(tag: String, message: String, stackTrace: String? = null, suggestedAiFix: String? = null) {
        log(LogLevel.ERROR, LogCategory.RUNTIME, tag, message, stackTrace, suggestedAiFix)
    }

    fun logBuildSuccess(projectName: String, durationMs: Long, apkSizeBytes: Long) {
        log(
            level = LogLevel.SUCCESS,
            category = LogCategory.COMPILER,
            tag = "ApkBuilder",
            message = "Successfully assembled APK for '$projectName' (${apkSizeBytes / 1024} KB) in ${durationMs}ms"
        )
    }

    fun resolveLog(id: String) {
        _logs.value = _logs.value.map {
            if (it.id == id) it.copy(isResolved = true) else it
        }
    }

    fun clearAllLogs() {
        _logs.value = emptyList()
    }

    fun triggerSimulatedDiagnosticCheck(): DiagnosticLog {
        val sampleErrors = listOf(
            DiagnosticLog(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                level = LogLevel.WARN,
                category = LogCategory.RUNTIME,
                tag = "StateFlowWatcher",
                message = "Potential infinite recomposition loop detected in mutableStateOf binding",
                stackTrace = "androidx.compose.runtime.RecomposeScopeImpl.compose()\nat com.example.ui.screens.VisualEditorScreen(VisualEditorScreen.kt:342)",
                suggestedAiFix = "Wrap state update inside rememberUpdatedState or move side-effect to LaunchedEffect."
            ),
            DiagnosticLog(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                level = LogLevel.ERROR,
                category = LogCategory.SECURITY,
                tag = "ManifestValidator",
                message = "Missing exported attribute on Activity with intent-filter (Android 12+ requirement)",
                stackTrace = "AndroidManifest.xml:24: Error: <activity android:name=\".MainActivity\"> with <intent-filter> must explicitly declare android:exported",
                suggestedAiFix = "Set android:exported=\"true\" on the launcher ComponentActivity declaration."
            ),
            DiagnosticLog(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                level = LogLevel.ERROR,
                category = LogCategory.COMPILER,
                tag = "D8Dexer",
                message = "Duplicate class found in multidex packaging: com.example.util.MathHelper",
                stackTrace = "com.android.tools.r8.CompilationFailedException: Type com.example.util.MathHelper is defined multiple times",
                suggestedAiFix = "Apply exclude group: 'com.example.util' or consolidate package imports."
            )
        )
        val selected = sampleErrors.random()
        _logs.value = listOf(selected) + _logs.value
        return selected
    }
}
