package com.neotermux.app.ui.screens.terminal

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neotermux.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionInfo(
    val id: Int,
    val name: String,
    val isRunning: Boolean = true,
    val buffer: List<String> = emptyList()
)

@HiltViewModel
class TerminalViewModel @Inject constructor() : ViewModel() {

    private val _sessions = MutableStateFlow(listOf(SessionInfo(id = 1, name = "Session 1")))
    val sessions: StateFlow<List<SessionInfo>> = _sessions.asStateFlow()

    private val _activeSessionIndex = MutableStateFlow(0)
    val activeSessionIndex: StateFlow<Int> = _activeSessionIndex.asStateFlow()

    private val _fontSize = MutableStateFlow(Constants.TERMINAL_FONT_SIZE_DEFAULT)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _showExtraKeys = MutableStateFlow(true)
    val showExtraKeys: StateFlow<Boolean> = _showExtraKeys.asStateFlow()

    var inputText = mutableStateOf(TextFieldValue(""))
        private set

    private var sessionIdCounter = 1

    fun addSession() {
        sessionIdCounter++
        val newSession = SessionInfo(id = sessionIdCounter, name = "Session $sessionIdCounter")
        _sessions.value = _sessions.value + newSession
        _activeSessionIndex.value = _sessions.value.size - 1
    }

    fun closeSession(id: Int) {
        _sessions.value = _sessions.value.filter { it.id != id }
        if (_activeSessionIndex.value >= _sessions.value.size) {
            _activeSessionIndex.value = (_sessions.value.size - 1).coerceAtLeast(0)
        }
    }

    fun switchSession(index: Int) {
        if (index in _sessions.value.indices) {
            _activeSessionIndex.value = index
        }
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size.coerceIn(Constants.TERMINAL_FONT_SIZE_MIN, Constants.TERMINAL_FONT_SIZE_MAX)
    }

    fun toggleExtraKeys() {
        _showExtraKeys.value = !_showExtraKeys.value
    }

    fun executeCommand() {
        val cmd = inputText.value.text.trim()
        if (cmd.isBlank()) {
            inputText.value = TextFieldValue("")
            return
        }

        val currentIndex = _activeSessionIndex.value
        val currentSessions = _sessions.value.toMutableList()
        if (currentIndex in currentSessions.indices) {
            val session = currentSessions[currentIndex]
            val newBuffer = session.buffer + listOf("~ $ $cmd") + processCommand(cmd)
            currentSessions[currentIndex] = session.copy(buffer = newBuffer)
            _sessions.value = currentSessions
        }
        inputText.value = TextFieldValue("")
    }

    fun sendKey(key: String) {
        val current = inputText.value.text
        inputText.value = when (key) {
            "Esc" -> TextFieldValue(current)
            "Tab" -> TextFieldValue("$current  ")
            "Ctrl", "Alt", "Shift" -> TextFieldValue(current)
            else -> TextFieldValue(current + key + " ")
        }
    }

    private fun processCommand(cmd: String): List<String> {
        return when {
            cmd == "clear" || cmd == "cls" -> emptyList()
            cmd == "help" -> listOf(
                "NeoTermux - Available commands:",
                "  help        Show this help",
                "  clear       Clear terminal",
                "  ls          List directory",
                "  pwd         Print working directory",
                "  echo        Print text",
                "  date        Show date/time",
                "  uname       System info",
                "  whoami      Current user",
                "  neotermux   About NeoTermux"
            )
            cmd.startsWith("echo ") -> listOf(cmd.removePrefix("echo "))
            cmd == "pwd" -> listOf("/data/data/com.neotermux.app/files/home")
            cmd == "whoami" -> listOf("u0_a299")
            cmd == "date" -> listOf(java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.US).format(java.util.Date()))
            cmd == "uname -a" || cmd == "uname" -> listOf("Linux localhost 4.14.190-perf+ #1 SMP PREEMPT aarch64 Android")
            cmd == "neotermux" -> listOf("NeoTermux v${Constants.VERSION_NAME}", "Terminal Emulator for Android")
            cmd.startsWith("ls") -> listOf(".bashrc  .profile  Documents/  Downloads/  Projects/  usr/ -> ../usr")
            cmd.startsWith("cat ") -> listOf("cat: ${cmd.removePrefix("cat ")}: No such file or directory")
            cmd.startsWith("cd ") -> {
                listOf("") // cd is silent on success
            }
            else -> listOf("$cmd: command not found")
        }
    }

    fun writeToTerminal(text: String) {
        val idx = _activeSessionIndex.value
        val currentSessions = _sessions.value.toMutableList()
        if (idx in currentSessions.indices) {
            val session = currentSessions[idx]
            currentSessions[idx] = session.copy(buffer = session.buffer + listOf(text))
            _sessions.value = currentSessions
        }
    }
}
