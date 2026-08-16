package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.apk.ApkFileManager
import com.example.data.model.DiagnosticLog
import com.example.data.model.LogCategory
import com.example.data.model.LogLevel
import com.example.ui.theme.*

@Composable
fun ErrorLogsView(
    logs: List<DiagnosticLog>,
    onResolveLog: (String) -> Unit,
    onClearLogs: () -> Unit,
    onTriggerScan: () -> Unit,
    onApplyAiFix: (DiagnosticLog) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLevelFilter by remember { mutableStateOf<LogLevel?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf(LogCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedLogId by remember { mutableStateOf<String?>(null) }

    val filteredLogs = remember(logs, selectedLevelFilter, selectedCategoryFilter, searchQuery) {
        logs.filter { log ->
            val matchesLevel = selectedLevelFilter == null || log.level == selectedLevelFilter
            val matchesCategory = selectedCategoryFilter == LogCategory.ALL || log.category == selectedCategoryFilter
            val matchesQuery = searchQuery.isBlank() ||
                    log.message.contains(searchQuery, ignoreCase = true) ||
                    log.tag.contains(searchQuery, ignoreCase = true) ||
                    (log.stackTrace?.contains(searchQuery, ignoreCase = true) == true)
            matchesLevel && matchesCategory && matchesQuery
        }
    }

    val errorCount = logs.count { it.level == LogLevel.ERROR && !it.isResolved }
    val warnCount = logs.count { it.level == LogLevel.WARN && !it.isResolved }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
    ) {
        // Top Toolbar
        Surface(
            color = SleekSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (errorCount > 0) SleekError.copy(alpha = 0.15f) else SleekSuccess.copy(alpha = 0.15f)
                        ) {
                            Icon(
                                imageVector = if (errorCount > 0) Icons.Default.BugReport else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (errorCount > 0) SleekError else SleekSuccess,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Diagnostics & Error Logs",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = if (errorCount > 0) "$errorCount unresolved errors • $warnCount warnings" else "All compilation checks healthy",
                                fontSize = 11.sp,
                                color = if (errorCount > 0) SleekError else SleekSuccess
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onTriggerScan,
                            modifier = Modifier.size(32.dp).testTag("btn_trigger_scan")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Run Diagnostic Scan",
                                tint = SleekPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                val textToCopy = logs.joinToString("\n\n") {
                                    "[${it.level}] ${it.tag} (${ApkFileManager.formatDate(it.timestamp)}): ${it.message}\n${it.stackTrace ?: ""}"
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Diagnostic Logs", textToCopy))
                            },
                            modifier = Modifier.size(32.dp).testTag("btn_copy_logs")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy all logs",
                                tint = SleekTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onClearLogs,
                            modifier = Modifier.size(32.dp).testTag("btn_clear_logs")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear logs",
                                tint = SleekTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter logs, tags, stacktraces...", fontSize = 12.sp, color = SleekTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("input_filter_logs"),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = SleekTextMuted, modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = SleekTextMuted, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekCardBorder,
                        focusedContainerColor = SleekSurfaceContainer,
                        unfocusedContainerColor = SleekSurfaceContainer,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedLevelFilter == null,
                            onClick = { selectedLevelFilter = null },
                            label = { Text("All (${logs.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SleekPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = SleekSurfaceContainer,
                                labelColor = SleekTextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedLevelFilter == LogLevel.ERROR,
                            onClick = {
                                selectedLevelFilter = if (selectedLevelFilter == LogLevel.ERROR) null else LogLevel.ERROR
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(SleekError, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Errors ($errorCount)", fontSize = 11.sp)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SleekError,
                                selectedLabelColor = Color.White,
                                containerColor = SleekSurfaceContainer,
                                labelColor = SleekError
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedLevelFilter == LogLevel.WARN,
                            onClick = {
                                selectedLevelFilter = if (selectedLevelFilter == LogLevel.WARN) null else LogLevel.WARN
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(SleekWarning, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Warnings ($warnCount)", fontSize = 11.sp)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SleekWarning,
                                selectedLabelColor = Color.Black,
                                containerColor = SleekSurfaceContainer,
                                labelColor = SleekWarning
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedLevelFilter == LogLevel.SUCCESS,
                            onClick = {
                                selectedLevelFilter = if (selectedLevelFilter == LogLevel.SUCCESS) null else LogLevel.SUCCESS
                            },
                            label = { Text("Success", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SleekSuccess,
                                selectedLabelColor = Color.White,
                                containerColor = SleekSurfaceContainer,
                                labelColor = SleekSuccess
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Logs List
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = SleekSuccess,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Clean Log Output",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "No diagnostic errors match the active criteria.",
                        fontSize = 12.sp,
                        color = SleekTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onTriggerScan,
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run Diagnostic Scan", fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    DiagnosticLogItemCard(
                        log = log,
                        isExpanded = expandedLogId == log.id,
                        onToggleExpand = {
                            expandedLogId = if (expandedLogId == log.id) null else log.id
                        },
                        onResolve = { onResolveLog(log.id) },
                        onApplyAiFix = { onApplyAiFix(log) }
                    )
                }
            }
        }
    }
}

@Composable
fun DiagnosticLogItemCard(
    log: DiagnosticLog,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onResolve: () -> Unit,
    onApplyAiFix: () -> Unit
) {
    val levelColor = when (log.level) {
        LogLevel.ERROR -> SleekError
        LogLevel.WARN -> SleekWarning
        LogLevel.INFO -> SleekPrimary
        LogLevel.DEBUG -> SleekSecondary
        LogLevel.SUCCESS -> SleekSuccess
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (log.level == LogLevel.ERROR && !log.isResolved) SleekError.copy(alpha = 0.4f) else SleekCardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onToggleExpand)
            .testTag("log_item_${log.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = levelColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = log.level.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = log.tag,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = SleekTextPrimary
                )

                Spacer(modifier = Modifier.weight(1f))

                if (log.isResolved) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SleekSuccess, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Resolved", fontSize = 10.sp, color = SleekSuccess)
                    }
                } else {
                    Text(
                        text = ApkFileManager.formatDate(log.timestamp),
                        fontSize = 10.sp,
                        color = SleekTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = log.message,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = if (log.level == LogLevel.ERROR) SleekError else SleekTextPrimary,
                lineHeight = 16.sp
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    if (log.stackTrace != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("STACK TRACE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = log.stackTrace,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFF87171),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (log.suggestedAiFix != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = SleekPrimary.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Suggested AI Auto-Fix", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = log.suggestedAiFix,
                                    fontSize = 11.sp,
                                    color = SleekTextPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!log.isResolved) {
                            if (log.suggestedAiFix != null) {
                                Button(
                                    onClick = onApplyAiFix,
                                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_apply_ai_fix")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Apply AI Fix", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            OutlinedButton(
                                onClick = onResolve,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Mark Resolved", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
