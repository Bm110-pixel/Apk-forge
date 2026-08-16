package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppProject
import com.example.data.model.ShellCommandRecord
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DeveloperShellView(
    history: List<ShellCommandRecord>,
    onExecuteCommand: (String) -> Unit,
    currentProject: AppProject? = null,
    modifier: Modifier = Modifier
) {
    var inputCommand by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(history.size - 1)
        }
    }

    val quickCommands = listOf(
        "help",
        "stats",
        "gradle build",
        "apk info",
        "logcat -e",
        "diagnose",
        "ls",
        "cat manifest",
        "env",
        "clean"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        // Shell Top Bar
        Surface(
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            shape = RoundedCornerShape(5.dp),
                            color = Color(0xFF10B981) // Green alive dot
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "apk-shell@android-v15",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFF1F5F9)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[SDK 35 • Daemon Ready]",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { onExecuteCommand("clear") },
                            modifier = Modifier.size(28.dp).testTag("btn_shell_clear")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Shell",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Commands Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(quickCommands) { cmd ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onExecuteCommand(cmd) }
                                .testTag("quick_cmd_$cmd"),
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = "$ $cmd",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Terminal Output Screen
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "AI APK Studio Mobile Interactive Shell (Android 15)\nType 'help' for manual, 'stats' for metrics, or 'gradle build' to compile.",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF64748B),
                    lineHeight = 15.sp
                )
            }

            items(history, key = { it.id }) { record ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Command Prompt line
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "apk-builder:~$ ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = record.command,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFF8FAFC)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${record.executionTimeMs}ms",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF475569)
                        )
                    }

                    if (record.output.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF020617),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Text(
                                text = record.output,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (record.isError) Color(0xFFF87171) else Color(0xFFE2E8F0),
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Command Input Bar
        Surface(
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.windowInsetsPadding(WindowInsets.ime)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ">",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF38BDF8)
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = inputCommand,
                    onValueChange = { inputCommand = it },
                    placeholder = {
                        Text(
                            text = "Enter shell command (e.g., stats, gradle build, logcat)...",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("shell_command_input"),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFF8FAFC)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputCommand.isNotBlank()) {
                                onExecuteCommand(inputCommand)
                                inputCommand = ""
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFF020617),
                        unfocusedContainerColor = Color(0xFF020617),
                        focusedTextColor = Color(0xFFF8FAFC),
                        unfocusedTextColor = Color(0xFFF8FAFC)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputCommand.isNotBlank()) {
                            onExecuteCommand(inputCommand)
                            inputCommand = ""
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF0284C7), RoundedCornerShape(8.dp))
                        .testTag("btn_shell_send")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Run command",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
