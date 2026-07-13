package com.neotermux.app.ui.screens.packagemanager

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
fun PackageManagerScreen(
    onBack: () -> Unit = {},
    viewModel: PackageManagerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Package Manager", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Default.Refresh, "Refresh") }
                    IconButton(onClick = { viewModel.updateAll() }) { Icon(Icons.Default.SystemUpdate, "Update All") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tabs
            val tabs = listOf("Installed", "Updates", "Search")
            var selectedTab by remember { mutableIntStateOf(0) }
            TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = index == selectedTab, onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (index == selectedTab) FontWeight.Bold else FontWeight.Normal) })
                }
            }

            when (selectedTab) {
                0 -> InstalledPackagesTab(state, viewModel)
                1 -> UpdatesTab(state, viewModel)
                2 -> SearchTab(state, viewModel)
            }
        }
    }
}

@Composable
private fun InstalledPackagesTab(state: PackageManagerState, viewModel: PackageManagerViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Stats
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.installedPackages.size}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Packages", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.updatableCount}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFFFA000))
                    Text("Updates", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatSize(state.totalSize), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.tertiary)
                    Text("Size", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(state.installedPackages.sortedBy { it.name }) { pkg ->
                PackageListItem(pkg, onUninstall = { viewModel.uninstallPackage(pkg.name) }, onUpdate = { viewModel.updatePackage(pkg.name) })
            }
        }
    }
}

@Composable
private fun UpdatesTab(state: PackageManagerState, viewModel: PackageManagerViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (state.updatablePackages.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, "", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("All packages are up to date", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        } else {
            items(state.updatablePackages) { pkg ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${pkg.version} -> ${pkg.latest}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = { viewModel.updatePackage(pkg.name) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Text("Update", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTab(state: PackageManagerState, viewModel: PackageManagerViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.searchPackages(it) },
            placeholder = { Text("Search packages...") },
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(12.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(state.searchResults) { pkg ->
                PackageSearchItem(pkg, onInstall = { viewModel.installPackage(pkg.name) })
            }
        }
    }
}

@Composable
private fun PackageListItem(pkg: PackageInfo, onUninstall: () -> Unit, onUpdate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Inventory2, "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(pkg.version, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (pkg.description.isNotEmpty()) {
                    Text(pkg.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (pkg.hasUpdate) {
                FilledTonalButton(onClick = onUpdate, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp), modifier = Modifier.height(28.dp)) {
                    Text("Update", fontSize = 11.sp)
                }
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onUninstall, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, "Uninstall", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PackageSearchItem(pkg: PackageInfo, onInstall: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(pkg.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        FilledTonalButton(onClick = onInstall, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
            Icon(Icons.Default.Download, "Install", modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Install", fontSize = 12.sp)
        }
    }
}

private fun formatSize(size: Long): String = when {
    size < 1024 -> "$size B"
    size < 1024 * 1024 -> "${size / 1024} KB"
    size < 1024 * 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
    else -> String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0))
}

data class PackageInfo(
    val name: String,
    val version: String,
    val latest: String = "",
    val description: String = "",
    val size: Long = 0,
    val hasUpdate: Boolean = false
)

data class PackageManagerState(
    val installedPackages: List<PackageInfo> = emptyList(),
    val installed: List<String> = emptyList(),
    val installedSizes: Map<String, Long> = emptyMap(),
    val updatablePackages: List<PackageInfo> = emptyList(),
    val searchResults: List<PackageInfo> = emptyList(),
    val searchQuery: String = ""
) {
    val installedPackagesList: List<PackageInfo> get() = installedPackages
    val installedPackagesWithUpdates: List<PackageInfo> get() = updatablePackages
    val updatableCount: Int get() = updatablePackages.size
    val totalSize: Long get() = installedSizes.values.sum()
}
