package com.neotermux.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.neotermux.app.ui.screens.editor.EditorScreen
import com.neotermux.app.ui.screens.filemanager.FileManagerScreen
import com.neotermux.app.ui.screens.git.GitScreen
import com.neotermux.app.ui.screens.packagemanager.PackageManagerScreen
import com.neotermux.app.ui.screens.processmanager.ProcessManagerScreen
import com.neotermux.app.ui.screens.settings.SettingsScreen
import com.neotermux.app.ui.screens.ssh.SshScreen
import com.neotermux.app.ui.screens.terminal.TerminalScreen
import com.neotermux.app.ui.theme.NeoTermuxTheme
import com.neotermux.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            Log.e("NeoTermux", "EdgeToEdge init failed", e)
        }
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            NeoTermuxTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "terminal") {
                        composable("terminal") {
                            TerminalScreen(
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToFileManager = { navController.navigate("filemanager") },
                                onNavigateToPackageManager = { navController.navigate("packagemanager") },
                                onNavigateToProcessManager = { navController.navigate("processmanager") },
                                onNavigateToGit = { navController.navigate("git") },
                                onNavigateToSsh = { navController.navigate("ssh") },
                                onNavigateToEditor = { navController.navigate("editor") }
                            )
                        }
                        composable("settings") { SettingsScreen(onBack = { navController.popBackStack() }) }
                        composable("filemanager") { FileManagerScreen(onBack = { navController.popBackStack() }) }
                        composable("packagemanager") { PackageManagerScreen(onBack = { navController.popBackStack() }) }
                        composable("processmanager") { ProcessManagerScreen(onBack = { navController.popBackStack() }) }
                        composable("git") { GitScreen(onBack = { navController.popBackStack() }) }
                        composable("ssh") { SshScreen(onBack = { navController.popBackStack() }) }
                        composable("editor") { EditorScreen(onBack = { navController.popBackStack() }) }
                    }
                }
            }
        }
    }
}
