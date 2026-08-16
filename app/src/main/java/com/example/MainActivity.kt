package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AiCreateAppDialog
import com.example.ui.screens.ApkVaultScreen
import com.example.ui.screens.AssetStoreScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioHomeScreen
import com.example.ui.screens.VisualEditorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EditorViewModel
import com.example.ui.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val studioViewModel: StudioViewModel = viewModel()
                val editorViewModel: EditorViewModel = viewModel()

                var showAiDialog by remember { mutableStateOf(false) }

                if (showAiDialog) {
                    AiCreateAppDialog(
                        viewModel = studioViewModel,
                        onDismiss = {
                            showAiDialog = false
                            studioViewModel.resetGenerationState()
                        },
                        onAppCreated = { createdProject ->
                            showAiDialog = false
                            studioViewModel.resetGenerationState()
                            navController.navigate("visual_editor/${createdProject.id}")
                        }
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = "studio_home",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("studio_home") {
                        StudioHomeScreen(
                            viewModel = studioViewModel,
                            onOpenEditor = { projectId ->
                                navController.navigate("visual_editor/$projectId")
                            },
                            onOpenAiCreator = {
                                showAiDialog = true
                            },
                            onOpenVault = {
                                navController.navigate("apk_vault")
                            },
                            onOpenStore = {
                                navController.navigate("asset_store")
                            },
                            onOpenSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    composable(
                        route = "visual_editor/{projectId}",
                        arguments = listOf(navArgument("projectId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                        VisualEditorScreen(
                            projectId = projectId,
                            viewModel = editorViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onOpenVault = { navController.navigate("apk_vault") }
                        )
                    }

                    composable("apk_vault") {
                        ApkVaultScreen(
                            viewModel = studioViewModel
                        )
                    }

                    composable("asset_store") {
                        AssetStoreScreen(
                            viewModel = studioViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            viewModel = studioViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToStore = { navController.navigate("asset_store") }
                        )
                    }
                }
            }
        }
    }
}
