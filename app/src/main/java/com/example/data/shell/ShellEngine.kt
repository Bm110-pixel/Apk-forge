package com.example.data.shell

import com.example.data.diagnostics.DiagnosticsEngine
import com.example.data.model.AppProject
import com.example.data.model.LogLevel
import com.example.data.model.ShellCommandRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object ShellEngine {

    private val _history = MutableStateFlow<List<ShellCommandRecord>>(createInitialHistory())
    val history: StateFlow<List<ShellCommandRecord>> = _history.asStateFlow()

    private fun createInitialHistory(): List<ShellCommandRecord> {
        return listOf(
            ShellCommandRecord(
                command = "env",
                output = """
Android SDK: Android 15 (VanillaIceCream - API Level 35)
Kotlin Runtime: 2.0.21 | Jetpack Compose: 1.7.0 (Compose Compiler 1.5.15)
Gradle Build System: 8.6.0 (Daemon Ready)
Java Virtual Machine: OpenJDK 64-Bit Server VM (build 17.0.12+7)
Device Target: ARM64-v8a / x86_64 Multi-Arch
AI Synthesizer: Gemini 1.5 Flash (Latency ~300ms)
APK Signer: V1/V2/V3/V4 Scheme Enabled
                """.trimIndent()
            ),
            ShellCommandRecord(
                command = "stats",
                output = """
+------------------------+-------+-----------+-------+------------+
| PROJECT                | VIEWS | DOWNLOADS | STARS | CONVERSION |
+------------------------+-------+-----------+-------+------------+
| Calculator Pro         | 1,420 | 380       | ★ 86  | 26.7%      |
| Quick Task Manager     | 980   | 245       | ★ 54  | 25.0%      |
| Modern Tech Blog       | 640   | 112       | ★ 32  | 17.5%      |
| Titan Fit Tracker      | 820   | 190       | ★ 41  | 23.1%      |
+------------------------+-------+-----------+-------+------------+
Total Community Reach: 3,860 views • 927 downloads • 213 stars
                """.trimIndent()
            )
        )
    }

    suspend fun executeCommand(
        cmdLine: String,
        currentProject: AppProject?,
        allProjects: List<AppProject>,
        onStarToggle: ((String) -> Unit)? = null
    ): ShellCommandRecord {
        val trimmed = cmdLine.trim()
        val parts = trimmed.split("\\s+".toRegex())
        val command = parts.firstOrNull()?.lowercase() ?: ""
        val args = parts.drop(1)

        val startTime = System.currentTimeMillis()
        var isError = false

        val output = when (command) {
            "", " " -> ""
            "help", "?" -> """
Available Shell Commands:
  help                     Show this command manual
  stats / analytics        View real-time Views, Downloads, Stars & Conversion
  gradle build             Run virtual compilation & generate release APK
  apk info [name]          Inspect APK manifest, signing keys, and DEX size
  logcat [-e]              Stream system and app error diagnostics
  diagnose / ai-fix        Run AI code analyzer to detect & repair bugs
  ls / list                List all projects and APK artifacts
  cat manifest             Display AndroidManifest.xml for current project
  cat build.gradle.kts     Display Gradle build configuration
  star [project]           Toggle star rating on a project
  env                      Display SDK, Compiler, JVM, and Device environment
  clean                    Purge compiler cache and temporary DEX files
  clear                    Clear the terminal output screen
""".trimIndent()

            "clear", "cls" -> {
                _history.value = emptyList()
                return ShellCommandRecord(command = trimmed, output = "")
            }

            "env", "uname", "version" -> """
Android Studio Mobile Shell (Antigravity AntEngine 4.2)
OS: Android Linux Kernel 6.1.75-android15-11
Target SDK: 35 (Android 15) | Min SDK: 26 (Android 8.0)
Kotlin: 2.0.21 | Jetpack Compose BOM 2024.09.00
AGP: 8.6.0 | R8 ProGuard: Enabled (Minification: On)
Memory: 3.8 GB Heap Allocated / 6.0 GB Total
Storage: /data/user/0/com.example/files/apks (1.2 GB Available)
""".trimIndent()

            "stats", "analytics", "metrics" -> {
                val sb = StringBuilder()
                sb.append("+------------------------+-------+-----------+-------+------------+\n")
                sb.append("| PROJECT                | VIEWS | DOWNLOADS | STARS | CONVERSION |\n")
                sb.append("+------------------------+-------+-----------+-------+------------+\n")
                var totalV = 0
                var totalD = 0
                var totalS = 0
                allProjects.forEach { p ->
                    val views = if (p.viewCount > 0) p.viewCount else 120
                    val dls = if (p.downloadCount > 0) p.downloadCount else 35
                    val stars = if (p.starCount > 0) p.starCount else 8
                    val conv = String.format("%.1f%%", (dls.toFloat() / views.coerceAtLeast(1)) * 100)
                    val starSymbol = if (p.isStarred) "★" else "☆"
                    val pName = if (p.name.length > 22) p.name.take(20) + ".." else p.name.padEnd(22)
                    sb.append("| $pName | ${views.toString().padEnd(5)} | ${dls.toString().padEnd(9)} | $starSymbol ${stars.toString().padEnd(4)} | ${conv.padEnd(10)} |\n")
                    totalV += views
                    totalD += dls
                    totalS += stars
                }
                sb.append("+------------------------+-------+-----------+-------+------------+\n")
                sb.append("Total Community Reach: $totalV views • $totalD downloads • $totalS stars\n")
                sb.toString().trimEnd()
            }

            "ls", "list", "dir" -> {
                val sb = StringBuilder()
                sb.append("Projects in Workspace (${allProjects.size}):\n")
                allProjects.forEach { p ->
                    val status = if (p.latestApkPath != null) "[APK COMPILED]" else "[DRAFT]"
                    sb.append("  drwxr-xr-x  ${p.packageName.padEnd(30)} ${p.name} ($status)\n")
                }
                sb.append("\nBuild Artifacts (/vault):\n")
                allProjects.filter { it.latestApkPath != null }.forEach { p ->
                    sb.append("  -rw-r--r--  ${p.name.replace(" ", "_").lowercase()}-v${p.versionName}.apk (${p.latestApkSize / 1024} KB)\n")
                }
                sb.toString().trimEnd()
            }

            "gradle", "./gradlew" -> {
                val subTask = args.joinToString(" ").lowercase()
                delay(300)
                if (subTask.contains("build") || subTask.contains("assemble")) {
                    """
> Starting Gradle Daemon...
> Task :app:preBuild UP-TO-DATE
> Task :app:compileKotlin (Kotlin 2.0.21, Compose Compiler Plugin)
> Task :app:processDebugManifest
> Task :app:mergeDebugResources
> Task :app:dexBuilderDebug
> Task :app:mergeDexDebug (Multidex packaging: 1 DEX file, 1,420 methods)
> Task :app:packageDebug
> Task :app:zipalignDebug
> Task :app:signApkWithV4Signature

BUILD SUCCESSFUL in 842ms
27 actionable tasks: 27 executed
Artifact generated: ${currentProject?.name ?: "app"}-release-aligned.apk
""".trimIndent()
                } else if (subTask.contains("clean")) {
                    """
> Task :app:clean
> Purging /build/intermediates/
> Purging /build/outputs/apk/
BUILD SUCCESSFUL in 120ms
""".trimIndent()
                } else {
                    """
Gradle 8.6.0
Usage: gradle <task>
Tasks:
  assembleRelease   Compile and generate production release APK
  assembleDebug     Compile debug APK with live debugger
  clean             Purge all build directories and caches
  lint              Run Android lint & Compose stability inspection
""".trimIndent()
                }
            }

            "apk" -> {
                val sub = args.firstOrNull()?.lowercase() ?: "info"
                if (sub == "info" || sub == "dump") {
                    val target = currentProject ?: allProjects.firstOrNull()
                    if (target == null) {
                        isError = true
                        "Error: No active project found to inspect."
                    } else {
                        """
[APK Package Inspector]
Package Name:     ${target.packageName}
Application Name: ${target.name}
Version Name:     ${target.versionName} (Version Code: ${target.versionCode})
Target SDK:       35 (Android 15 VanillaIceCream)
Min SDK:          26 (Android 8.0 Oreo)
Architecture:     Universal (arm64-v8a, armeabi-v7a, x86_64)
Signing Scheme:   v2 + v3 (Certificate SHA-256: 8F:4A:2C:99:E1:5D:80:B3)
Permissions:      android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE
Total Views:      ${target.viewCount} • Downloads: ${target.downloadCount} • Stars: ${target.starCount}
DEX File Count:   1 (classes.dex)
APK Size:         ${if (target.latestApkSize > 0) target.latestApkSize / 1024 else 2480} KB
Status:           ${if (target.latestApkPath != null) "Signed & Ready for Install" else "Pending Compilation"}
""".trimIndent()
                    }
                } else {
                    "Usage: apk info [project-name]"
                }
            }

            "logcat", "logs" -> {
                val filterErr = args.contains("-e") || args.contains("--errors")
                val logsList = DiagnosticsEngine.logs.value
                val filtered = if (filterErr) logsList.filter { it.level == LogLevel.ERROR } else logsList
                if (filtered.isEmpty()) {
                    "No diagnostic logs recorded in this session."
                } else {
                    val sb = StringBuilder()
                    sb.append("--- BEGIN LOGCAT STREAM (${filtered.size} records) ---\n")
                    filtered.take(15).forEach { l ->
                        val lvl = when (l.level) {
                            LogLevel.ERROR -> "E/"
                            LogLevel.WARN -> "W/"
                            LogLevel.INFO -> "I/"
                            LogLevel.DEBUG -> "D/"
                            LogLevel.SUCCESS -> "V/"
                        }
                        sb.append("$lvl${l.tag.padEnd(16)}: ${l.message}\n")
                        if (l.stackTrace != null && (filterErr || l.level == LogLevel.ERROR)) {
                            sb.append("    ${l.stackTrace.lines().take(2).joinToString("\n    ")}\n")
                        }
                    }
                    sb.append("--- END LOGCAT STREAM ---")
                    sb.toString()
                }
            }

            "diagnose", "ai-fix", "lint" -> {
                delay(400)
                val simulated = DiagnosticsEngine.triggerSimulatedDiagnosticCheck()
                """
[AI Diagnostics Engine] Scanning project AST & Compose tree...
✓ Analyzed 14 Composable nodes
✓ Verified WindowInsets & Edge-to-Edge compliance
✓ Checked MaterialTheme color token contrast

Found 1 issue:
  [${simulated.level}] ${simulated.tag}: ${simulated.message}
  Suggested AI Fix: ${simulated.suggestedAiFix}

Auto-fix applied successfully! Issue resolved in AST.
""".trimIndent()
            }

            "star" -> {
                val target = currentProject ?: allProjects.firstOrNull()
                if (target != null) {
                    onStarToggle?.invoke(target.id)
                    "★ Toggled star for '${target.name}'. Current stars: ${target.starCount + if (target.isStarred) -1 else 1}"
                } else {
                    "Error: No project selected to star."
                }
            }

            "cat" -> {
                val targetFile = args.firstOrNull()?.lowercase() ?: ""
                val proj = currentProject ?: allProjects.firstOrNull()
                when {
                    targetFile.contains("manifest") -> """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${proj?.packageName ?: "com.example.app"}">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="${proj?.name ?: "AI App"}"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.MyApplication">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
""".trimIndent()
                    targetFile.contains("gradle") -> """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "${proj?.packageName ?: "com.example"}"
    compileSdk = 35

    defaultConfig {
        applicationId = "${proj?.packageName ?: "com.example.app"}"
        minSdk = 26
        targetSdk = 35
        versionCode = ${proj?.versionCode ?: 1}
        versionName = "${proj?.versionName ?: "1.0.0"}"
    }
}
""".trimIndent()
                    else -> "cat: file not found: $targetFile. Try 'cat manifest' or 'cat build.gradle.kts'"
                }
            }

            "clean" -> {
                DiagnosticsEngine.clearAllLogs()
                "Compiler and diagnostic caches cleared."
            }

            else -> {
                isError = true
                "bash: $command: command not found. Type 'help' to view available commands."
            }
        }

        val record = ShellCommandRecord(
            id = UUID.randomUUID().toString(),
            command = trimmed,
            output = output,
            isError = isError,
            executionTimeMs = System.currentTimeMillis() - startTime,
            timestamp = System.currentTimeMillis()
        )

        _history.value = _history.value + record
        return record
    }
}
