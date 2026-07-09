package com.neotermux.app.ui.screens.ssh

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SshConnection(
    val name: String,
    val host: String,
    val port: Int = 22,
    val user: String = "root",
    val authType: String = "password"
)

data class SshState(
    val connections: List<SshConnection> = listOf(
        SshConnection("My Server", "192.168.1.100", 22, "root"),
        SshConnection("Dev Box", "dev.example.com", 2222, "developer"),
        SshConnection("Raspberry Pi", "raspberry.local", 22, "pi")
    ),
    val activeConnection: SshConnection? = null,
    val knownHosts: List<String> = emptyList(),
    val sshKeys: List<String> = listOf("id_rsa", "id_ed25519")
)

@HiltViewModel
class SshViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(SshState())
    val state: StateFlow<SshState> = _state.asStateFlow()

    fun connect(connection: SshConnection) {}
    fun disconnect() {}
    fun addConnection(connection: SshConnection) {}
    fun removeConnection(name: String) {}
    fun generateKey(type: String) {}
}