package com.neotermux.app.ui.screens.git

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class GitCommit(
    val hash: String,
    val message: String,
    val author: String,
    val date: String
)

data class GitState(
    val currentBranch: String = "main",
    val branches: List<String> = listOf("main", "develop", "feature/termux"),
    val stagedChanges: List<Pair<String, String>> = emptyList(),
    val unstagedChanges: List<Pair<String, String>> = listOf(
        Pair("M", "README.md"),
        Pair("D", "old_file.txt")
    ),
    val untrackedFiles: List<String> = listOf("new_feature.py", "notes.md"),
    val recentCommits: List<GitCommit> = listOf(
        GitCommit("a1b2c3d4", "feat: initial terminal emulator implementation", "Developer", "2 hours ago"),
        GitCommit("e5f6g7h8", "fix: correct PTY input handling", "Developer", "5 hours ago"),
        GitCommit("i9j0k1l2", "docs: update README with build instructions", "Developer", "1 day ago"),
        GitCommit("m3n4o5p6", "refactor: clean up native code", "Developer", "2 days ago")
    )
)

@HiltViewModel
class GitViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(GitState())
    val state: StateFlow<GitState> = _state.asStateFlow()

    fun refresh() {}
    fun switchBranch(branch: String) { _state.value = _state.value.copy(currentBranch = branch) }
    fun commit(message: String) {}
    fun push() {}
    fun pull() {}
    fun fetch() {}
    fun stage(file: String) {}
    fun unstage(file: String) {}
}