package com.neotermux.app.ui.screens.git

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitScreen(
    onBack: () -> Unit = {},
    viewModel: GitViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Git", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(state.currentBranch, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = { /* pull */ }) { Icon(Icons.Default.Download, "Pull") }
                    IconButton(onClick = { /* push */ }) { Icon(Icons.Default.Upload, "Push") }
                    IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Default.Refresh, "Refresh") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val tabs = listOf("Status", "Commits", "Branches")
            var selectedTab by remember { mutableIntStateOf(0) }
            TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = index == selectedTab, onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (index == selectedTab) FontWeight.Bold else FontWeight.Normal) })
                }
            }

            when (selectedTab) {
                0 -> StatusTab(state, viewModel)
                1 -> CommitsTab(state)
                2 -> BranchesTab(state, viewModel)
            }
        }
    }
}

@Composable
private fun StatusTab(state: GitState, viewModel: GitViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Branch, "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(state.currentBranch, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("${state.stagedChanges.size} staged changes", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${state.unstagedChanges.size} unstaged changes", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${state.untrackedFiles.size} untracked files", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (state.unstagedChanges.isNotEmpty()) {
            item { Text("Unstaged Changes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 4.dp)) }
        }
        items(state.unstagedChanges) { change ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(change.first, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFFE53935))
                Spacer(Modifier.width(8.dp))
                Text(change.second, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        if (state.untrackedFiles.isNotEmpty()) {
            item { Text("Untracked Files", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
        }
        items(state.untrackedFiles) { file ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("?", fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFFFFA000))
                Spacer(Modifier.width(8.dp))
                Text(file, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                TextButton(onClick = { /* add */ }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Add", fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun CommitsTab(state: GitState) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(state.recentCommits) { commit ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(commit.hash.take(7), fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        Text(commit.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(commit.message, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(commit.author, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun BranchesTab(state: GitState, viewModel: GitViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(state.branches) { branch ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Branch,
                    "",
                    tint = if (branch == state.currentBranch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(branch, fontSize = 14.sp, fontWeight = if (branch == state.currentBranch) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                if (branch == state.currentBranch) {
                    Text("current", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}