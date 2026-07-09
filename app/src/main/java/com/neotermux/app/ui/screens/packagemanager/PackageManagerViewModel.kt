package com.neotermux.app.ui.screens.packagemanager

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PackageManagerViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(PackageManagerState())
    val state: StateFlow<PackageManagerState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(
            installedPackages = listOf(
                PackageInfo("bash", "5.2.26", "5.2.26", "GNU Bourne-Again SHell", hasUpdate = false),
                PackageInfo("coreutils", "9.5", "9.6", "GNU core utilities", hasUpdate = true),
                PackageInfo("curl", "8.11.0", "8.11.0", "URL transfer tool", hasUpdate = false),
                PackageInfo("git", "2.47.0", "2.47.1", "Fast version control system", hasUpdate = true),
                PackageInfo("openssh", "9.8p1", "9.8p1", "OpenSSH client and server", hasUpdate = false),
                PackageInfo("python", "3.13.0", "3.13.0", "Python programming language", hasUpdate = false),
                PackageInfo("vim", "9.1.0800", "9.1.0800", "Vi IMproved text editor", hasUpdate = false),
                PackageInfo("zsh", "5.9", "5.9", "Z shell", hasUpdate = false)
            ),
            installed = listOf("bash", "coreutils", "curl", "git", "openssh", "python", "vim", "zsh"),
            installedSizes = mapOf("bash" to 1024L * 1024, "coreutils" to 5L * 1024 * 1024),
            updatablePackages = listOf(
                PackageInfo("coreutils", "9.5", "9.6", "GNU core utilities", hasUpdate = true),
                PackageInfo("git", "2.47.0", "2.47.1", "Fast version control", hasUpdate = true)
            )
        )
    }

    fun installPackage(name: String) {}
    fun uninstallPackage(name: String) {}
    fun updatePackage(name: String) {}
    fun updateAll() {}
    fun searchPackages(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _state.value = _state.value.copy(searchResults = emptyList())
            return
        }
        _state.value = _state.value.copy(
            searchResults = listOf(
                PackageInfo("nodejs", "22.11.0", "22.11.0", "JavaScript runtime", false),
                PackageInfo("rust", "1.82.0", "1.82.0", "Rust programming language", false),
                PackageInfo("go", "1.23.3", "1.23.3", "Go programming language", false)
            ).filter { it.name.contains(query, ignoreCase = true) }
        )
    }
}