package com.neotermux.app.ui.screens.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit = {},
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.currentFile ?: "Untitled", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = { viewModel.save() }) { Icon(Icons.Default.Save, "Save") }
                    IconButton(onClick = { viewModel.undo() }) { Icon(Icons.Default.Undo, "Undo") }
                    IconButton(onClick = { viewModel.redo() }) { Icon(Icons.Default.Redo, "Redo") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = Color(0xFF1E1E1E)
    ) { padding ->
        Row(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Line numbers
            Column(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF252526))
                    .padding(end = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                val lineCount = state.content.split("\n").size
                LazyColumn {
                    items((1..lineCount.coerceAtLeast(1)).toList()) { line ->
                        Text(
                            "$line",
                            color = Color(0xFF858585),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(end = 8.dp, top = 2.dp, bottom = 2.dp)
                        )
                    }
                }
            }
            VerticalDivider(color = Color(0xFF3C3C3C))

            // Editor area
            Column(modifier = Modifier.weight(1f)) {
                // Tabs for open files
                if (state.openFiles.size > 1) {
                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF252526)).horizontalScroll(rememberScrollState())) {
                        state.openFiles.forEach { file ->
                            Tab(
                                selected = file == state.currentFile,
                                onClick = { viewModel.switchFile(file) },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(file.substringAfterLast("/"), fontSize = 11.sp, color = if (file == state.currentFile) Color.White else Color(0xFF9AA0A6), modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        }
                    }
                }

                // Editor content
                Box(modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState())) {
                    BasicTextField(
                        value = state.content,
                        onValueChange = { viewModel.updateContent(it) },
                        textStyle = TextStyle(
                            color = Color(0xFFD4D4D4),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                        ),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF8AB4F8)),
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        decorationBox = { innerTextField ->
                            Box {
                                if (state.content.isEmpty()) {
                                    Text("Start typing...", color = Color(0xFF5A5A5A), fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // Status bar
                Surface(color = Color(0xFF007ACC), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(if (state.isModified) "Modified" else "Saved", fontSize = 11.sp, color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Text("UTF-8", fontSize = 11.sp, color = Color.White)
                        Spacer(Modifier.width(16.dp))
                        Text("Ln ${state.cursorLine}, Col ${state.cursorCol}", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}