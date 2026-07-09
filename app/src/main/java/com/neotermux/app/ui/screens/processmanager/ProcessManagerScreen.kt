package com.neotermux.app.ui.screens.processmanager

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessManagerScreen(
    onBack: () -> Unit = {},
    viewModel: ProcessManagerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Process Manager", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = { IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Default.Refresh, "Refresh") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // System stats
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatItem("CPU", "${state.cpuUsage}%", Color(0xFF1A73E8))
                        Spacer(Modifier.weight(1f))
                        StatItem("RAM", state.memoryUsed, Color(0xFF34A853))
                        Spacer(Modifier.weight(1f))
                        StatItem("Processes", "${state.processCount}", Color(0xFFFBBC04))
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { state.cpuUsage / 100f },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { state.memoryPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = Color(0xFF34A853),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }

            // Process list header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)).padding(8.dp),
            ) {
                Text("PID", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(48.dp))
                Text("Process", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("CPU", fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.width(48.dp))
                Text("RAM", fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.width(64.dp))
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.processes) { proc ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${proc.pid}", fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.width(48.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(proc.name, fontSize = 13.sp, maxLines = 1)
                            Text(proc.user, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${proc.cpuPercent}%", fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.width(48.dp))
                        Text(proc.memoryFormatted, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.width(64.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

data class ProcessInfo(
    val pid: Int,
    val name: String,
    val user: String = "root",
    val cpuPercent: Float = 0f,
    val memory: Long = 0
) {
    val memoryFormatted: String get() = when {
        memory < 1024 -> "${memory}K"
        memory < 1024 * 1024 -> "${memory / 1024}M"
        else -> String.format("%.1fG", memory / (1024.0 * 1024.0))
    }
}

data class ProcessManagerState(
    val processes: List<ProcessInfo> = (1..10).map {
        ProcessInfo(
            pid = 1000 + it,
            name = listOf("init", "sh", "bash", "sshd", "httpd", "mysqld", "python3", "node", "java", "vim").randomValue,
            cpuPercent = (Math.random() * 30).toFloat(),
            memory = (Math.random() * 500 * 1024).toLong()
        )
    },
    val cpuUsage: Float = (20 + Math.random() * 60).toFloat(),
    val memoryUsed: String = String.format("%.1f GB", 1.5 + Math.random() * 2),
    val memoryPercent: Float = (30 + Math.random() * 40).toFloat(),
    val processCount: Int = 128 + (Math.random() * 50).toInt()
)

private val <T> List<T>.randomValue: T get() = this[randomIndex()]
private fun randomIndex(): Int = (Math.random() * 10).toInt()

@HiltViewModel
class ProcessManagerViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(ProcessManagerState())
    val state: StateFlow<ProcessManagerState> = _state.asStateFlow()

    fun killProcess(pid: Int) {}
    fun refresh() {}
}