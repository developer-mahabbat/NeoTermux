package com.neotermux.app.ui.screens.filemanager

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

data class FileManagerState(
    val currentPath: String = "/data/data/com.neotermux.app/files/home",
    val files: List<FileItem> = emptyList(),
    val filteredFiles: List<FileItem> = emptyList(),
    val currentSegments: List<String> = emptyList(),
    val searchQuery: String = "",
    val isGridView: Boolean = false,
    val showHidden: Boolean = false
)

@HiltViewModel
class FileManagerViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(FileManagerState())
    val state: StateFlow<FileManagerState> = _state.asStateFlow()

    init { navigateTo(_state.value.currentPath) }

    fun navigateTo(path: String) {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return
        val files = dir.listFiles()?.map { file ->
            FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isFile) file.length() else 0L,
                lastModified = file.lastModified(),
                permissions = if (file.canRead()) "r" else "-" + if (file.canWrite()) "w" else "-" + if (file.canExecute()) "x" else "-",
                isHidden = file.name.startsWith(".")
            )
        }?.sortedWith(compareByDescending<FileItem> { it.isDirectory }.thenBy { it.name.lowercase() }) ?: emptyList()
        val segments = dir.absolutePath.split("/").filter { it.isNotEmpty() }.let { parts ->
            listOf("") + parts
        }
        _state.value = _state.value.copy(
            currentPath = dir.absolutePath,
            files = files,
            filteredFiles = if (_state.value.searchQuery.isEmpty()) files else files.filter { it.name.contains(_state.value.searchQuery, ignoreCase = true) },
            currentSegments = segments
        )
    }

    fun openFile(file: FileItem) {
        if (file.isDirectory) navigateTo(file.path)
    }

    fun navigateUp() {
        val parent = File(_state.value.currentPath).parent
        if (parent != null) navigateTo(parent)
    }

    fun navigateToSegment(index: Int) {
        val parts = _state.value.currentPath.split("/").filter { it.isNotEmpty() }
        if (index == 0) {
            navigateTo("/")
        } else if (index < parts.size) {
            navigateTo("/" + parts.take(index).joinToString("/"))
        }
    }

    fun toggleViewMode() { _state.value = _state.value.copy(isGridView = !_state.value.isGridView) }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredFiles = if (query.isEmpty()) _state.value.files
                else _state.value.files.filter { it.name.contains(query, ignoreCase = true) }
        )
    }

    fun toggleShowHidden() { _state.value = _state.value.copy(showHidden = !_state.value.showHidden) }

    fun refresh() { navigateTo(_state.value.currentPath) }
}
