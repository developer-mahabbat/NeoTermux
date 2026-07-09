package com.neotermux.app.ui.screens.editor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class EditorState(
    val content: String = "",
    val currentFile: String? = null,
    val openFiles: List<String> = listOf("Untitled"),
    val isModified: Boolean = false,
    val cursorLine: Int = 1,
    val cursorCol: Int = 1,
    val wordWrap: Boolean = false,
    val showMinimap: Boolean = true,
    val fontSize: Int = 14
)

@HiltViewModel
class EditorViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    fun updateContent(content: String) {
        _state.value = _state.value.copy(content = content, isModified = true)
    }

    fun openFile(path: String, content: String) {
        val existingFiles = _state.value.openFiles.toMutableList()
        if (path !in existingFiles) existingFiles.add(path)
        _state.value = _state.value.copy(
            currentFile = path,
            content = content,
            openFiles = existingFiles,
            isModified = false
        )
    }

    fun switchFile(path: String) {
        _state.value = _state.value.copy(currentFile = path)
    }

    fun save() {
        _state.value = _state.value.copy(isModified = false)
    }

    fun undo() {}
    fun redo() {}
}