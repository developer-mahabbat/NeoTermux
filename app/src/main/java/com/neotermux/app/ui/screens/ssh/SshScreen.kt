package com.neotermux.app.ui.screens.ssh

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neotermux.app.ui.screens.ssh.SshViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SshScreen(
    onBack: () -> Unit = {},
    viewModel: SshViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SSH Connections", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Add, "New Connection") }
                    IconButton(onClick = { }) { Icon(Icons.Default.Key, "Keys") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Quick Connect", fontWeight = FontWeight.Bold)
                                Text("Connect to any SSH server", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = "user@hostname:22", onValueChange = {}, label = { Text("ssh [user@]host[:port]") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Connect") }
                    }
                }
            }

            item {
                Text("Saved Connections", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
            }

            items(state.connections) { conn ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Computer, "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(conn.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${conn.user}@${conn.host}:${conn.port}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FilledTonalButton(onClick = {}, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) { Text("Connect", fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}

