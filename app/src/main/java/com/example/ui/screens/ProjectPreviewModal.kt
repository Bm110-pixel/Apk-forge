package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppProject
import com.example.data.model.ComponentAction
import com.example.data.model.ComponentType
import com.example.data.model.UiComponent
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectPreviewModal(
    project: AppProject,
    loadComponents: suspend (String) -> List<UiComponent>,
    onDismiss: () -> Unit,
    onOpenFullEditor: () -> Unit
) {
    val context = LocalContext.current
    var components by remember { mutableStateOf<List<UiComponent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var counterVal by remember { mutableIntStateOf(10) }
    var switchVal by remember { mutableStateOf(true) }
    var sliderVal by remember { mutableFloatStateOf(65f) }

    LaunchedEffect(project.id) {
        isLoading = true
        components = loadComponents(project.id)
        isLoading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF090D16),
        scrimColor = Color.Black.copy(alpha = 0.75f),
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = SleekPrimary.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Live App Preview: ${project.name}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Interactive Simulator • Test UI components live",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_preview_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SleekTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Smartphone Frame Container
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.95f)
                    .border(2.dp, SleekCardBorder, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                color = try {
                    Color(android.graphics.Color.parseColor(project.backgroundColorHex))
                } catch (e: Exception) {
                    Color(0xFF0F172A)
                }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Notch / Status Bar
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

                    // App TopBar
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
                        IconButton(
                            onClick = { Toast.makeText(context, "App Menu Options", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                        }
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = SleekPrimary)
                        }
                    } else if (components.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Widgets, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No UI components found in this project.", fontSize = 13.sp, color = Color.LightGray)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp)
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
                                                    color = try {
                                                        Color(android.graphics.Color.parseColor(project.primaryColorHex))
                                                    } catch (e: Exception) {
                                                        SleekPrimary
                                                    }
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
                                                containerColor = try {
                                                    Color(android.graphics.Color.parseColor(project.primaryColorHex))
                                                } catch (e: Exception) {
                                                    SleekPrimary
                                                }
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
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(comp.title, color = Color.White, fontSize = 14.sp)
                                                    Text("${sliderVal.toInt()}%", color = Color.LightGray, fontSize = 13.sp)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Slider(
                                                    value = sliderVal,
                                                    onValueChange = { sliderVal = it },
                                                    valueRange = 0f..100f
                                                )
                                            }
                                        }
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

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Text("Close Preview", color = SleekTextPrimary)
                }

                Button(
                    onClick = {
                        onDismiss()
                        onOpenFullEditor()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_editor_from_preview_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open in Editor", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
