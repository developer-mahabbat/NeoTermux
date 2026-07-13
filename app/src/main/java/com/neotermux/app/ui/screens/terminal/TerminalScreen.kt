package com.neotermux.app.ui.screens.terminal

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neotermux.app.util.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TerminalScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFileManager: () -> Unit = {},
    onNavigateToPackageManager: () -> Unit = {},
    onNavigateToProcessManager: () -> Unit = {},
    onNavigateToGit: () -> Unit = {},
    onNavigateToSsh: () -> Unit = {},
    onNavigateToEditor: () -> Unit = {},
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val activeIndex by viewModel.activeSessionIndex.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val isDrawer = remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        if (isLandscape) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = Color(0xFF252526),
                header = {
                    FloatingActionButton(
                        onClick = { viewModel.addSession() },
                        modifier = Modifier.padding(8.dp).size(40.dp),
                        containerColor = Color(0xFF1A73E8),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Session")
                    }
                }
            ) {
                sessions.forEachIndexed { index, session ->
                    NavigationRailItem(
                        selected = index == activeIndex,
                        onClick = { viewModel.switchSession(index) },
                        icon = {
                            Icon(
                                if (session.isRunning) Icons.Default.Terminal else Icons.Default.Cancel,
                                contentDescription = session.name
                            )
                        },
                        label = {
                            Text(
                                session.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF8AB4F8),
                            selectedTextColor = Color(0xFF8AB4F8),
                            indicatorColor = Color(0xFF37373D),
                            unselectedIconColor = Color(0xFF9AA0A6),
                            unselectedTextColor = Color(0xFF9AA0A6)
                        )
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF252526))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLandscape) {
                    IconButton(onClick = { isDrawer.value = true }) {
                        Icon(Icons.Default.Menu, "Menu", tint = Color(0xFF9AA0A6))
                    }
                }
                sessions.forEachIndexed { index, session ->
                    if (index >= activeIndex - 2 && index <= activeIndex + 2) {
                        Tab(
                            selected = index == activeIndex,
                            onClick = { viewModel.switchSession(index) },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    session.name,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    color = if (index == activeIndex) Color(0xFF8AB4F8) else Color(0xFF9AA0A6)
                                )
                                if (sessions.size > 1) {
                                    IconButton(
                                        onClick = { viewModel.closeSession(session.id) },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(Icons.Default.Close, "Close", tint = Color(0xFF9AA0A6), modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.addSession() }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, "New Tab", tint = Color(0xFF9AA0A6), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onNavigateToSettings, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Settings, "Settings", tint = Color(0xFF9AA0A6), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { viewModel.toggleExtraKeys() }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Keyboard, "Keyboard", tint = Color(0xFF9AA0A6), modifier = Modifier.size(18.dp))
                }
            }

            // Terminal output area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .pointerInput(Unit) {
                        detectTapGestures { /* focus terminal */ }
                    }
            ) {
                val terminalLines = remember { mutableStateOf(listOf("~ $ ")) }
                val listState = rememberLazyListState()
                val lines = terminalLines.value

                LaunchedEffect(lines.size) {
                    if (lines.isNotEmpty()) {
                        listState.animateScrollToItem(lines.size - 1)
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(lines) { _, line ->
                        Text(
                            text = line,
                            color = Color(0xFFD4D4D4),
                            fontSize = fontSize.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Scrollbar indicator
                if (sessions.getOrNull(activeIndex)?.isRunning == true) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2DC937))
                    )
                }
            }

            // Input bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF252526),
                tonalElevation = 2.dp
            ) {
                Column {
                    // Extra keys row (togglable)
                    val showExtraKeys by viewModel.showExtraKeys.collectAsState()
                    AnimatedVisibility(visible = showExtraKeys) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(4.dp).horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Esc", "Tab", "Ctrl", "Alt", "Shift", "|", "&", ";", "#", "~", "$", "/").forEach { key ->
                                FilledTonalButton(
                                    onClick = { viewModel.sendKey(key) },
                                    modifier = Modifier.height(36.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF37373D),
                                        contentColor = Color(0xFFD4D4D4)
                                    )
                                ) {
                                    Text(key, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Command input
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$ ", color = Color(0xFF4EC9B0), fontSize = (fontSize).sp)
                        TextField(
                            value = viewModel.inputText.value,
                            onValueChange = { viewModel.inputText.value = it },
                            textStyle = TextStyle(
                                color = Color(0xFFD4D4D4),
                                fontSize = fontSize.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("Type a command...", color = Color(0xFF5A5A5A), fontSize = fontSize.sp, fontFamily = FontFamily.Monospace) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFF8AB4F8)
                            )
                        )
                        IconButton(onClick = { viewModel.executeCommand() }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Send, "Execute", tint = Color(0xFF4EC9B0), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { viewModel.addSession() }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.AddCircleOutline, "New", tint = Color(0xFF9AA0A6), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Drawer for drawer-based navigation
        if (isDrawer.value && !isLandscape) {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color(0xFF252526)
            ) {
                NavigationDrawerItems(onNavigateTo = { route ->
                    isDrawer.value = false
                    when (route) {
                        "filemanager" -> onNavigateToFileManager()
                        "packagemanager" -> onNavigateToPackageManager()
                        "processmanager" -> onNavigateToProcessManager()
                        "git" -> onNavigateToGit()
                        "ssh" -> onNavigateToSsh()
                        "editor" -> onNavigateToEditor()
                        "settings" -> onNavigateToSettings()
                    }
                }, onDismiss = { isDrawer.value = false })
            }
        }
    }
}

@Composable
fun NavigationDrawerItems(onNavigateTo: (String) -> Unit, onDismiss: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "NeoTermux",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF8AB4F8),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        HorizontalDivider(color = Color(0xFF37373D))
        Spacer(Modifier.height(8.dp))
        DrawerItem(Icons.Default.Terminal, "Terminal", "terminal") { onNavigateTo("terminal"); onDismiss() }
        DrawerItem(Icons.Default.FolderOpen, "File Manager", "filemanager") { onNavigateTo("filemanager") }
        DrawerItem(Icons.Default.Inventory2, "Packages", "packagemanager") { onNavigateTo("packagemanager") }
        DrawerItem(Icons.Default.Memory, "Processes", "processmanager") { onNavigateTo("processmanager") }
        DrawerItem(Icons.Default.Code, "Git", "git") { onNavigateTo("git") }
        DrawerItem(Icons.Default.Lock, "SSH", "ssh") { onNavigateTo("ssh") }
        DrawerItem(Icons.Default.Edit, "Editor", "editor") { onNavigateTo("editor") }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFF37373D))
        Spacer(Modifier.height(8.dp))
        DrawerItem(Icons.Default.Settings, "Settings", "settings") { onNavigateTo("settings") }
    }
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, tag: String, onClick: () -> Unit) {
    NavigationDrawerItem(
        icon = { Icon(icon, label, tint = Color(0xFF9AA0A6)) },
        label = { Text(label, color = Color(0xFFD4D4D4), fontSize = 14.sp) },
        selected = false,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedTextColor = Color(0xFFD4D4D4)
        ),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}