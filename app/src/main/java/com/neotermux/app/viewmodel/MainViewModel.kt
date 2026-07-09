package com.neotermux.app.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neotermux.app.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _themeMode = MutableStateFlow(ThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    val terminalSessions = mutableStateOf<List<TerminalSessionInfo>>(emptyList())
    val activeSessionIndex = mutableStateOf(0)

    fun toggleTheme() {
        _themeMode.value = _themeMode.value.copy(
            isDark = !_themeMode.value.isDark,
            isDynamic = false
        )
    }

    fun setThemeMode(isDark: Boolean, isDynamic: Boolean = false) {
        _themeMode.value = ThemeMode(isDark = isDark, isDynamic = isDynamic)
    }

    fun addSession() {
        val current = terminalSessions.value.toMutableList()
        val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
        current.add(TerminalSessionInfo(id = newId, name = "Session $newId"))
        terminalSessions.value = current
        activeSessionIndex.value = current.size - 1
    }

    fun closeSession(id: Int) {
        val current = terminalSessions.value.toMutableList()
        current.removeAll { it.id == id }
        terminalSessions.value = current
        if (activeSessionIndex.value >= current.size) {
            activeSessionIndex.value = (current.size - 1).coerceAtLeast(0)
        }
    }

    fun switchSession(index: Int) {
        if (index in terminalSessions.value.indices) {
            activeSessionIndex.value = index
        }
    }

    data class TerminalSessionInfo(
        val id: Int,
        val name: String,
        val isRunning: Boolean = true
    )
}
