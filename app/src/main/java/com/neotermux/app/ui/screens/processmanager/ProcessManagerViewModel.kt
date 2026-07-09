package com.neotermux.app.ui.screens.processmanager

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProcessManagerViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(ProcessManagerState())
    val state: StateFlow<ProcessManagerState> = _state.asStateFlow()

    fun killProcess(pid: Int) {}
    fun refresh() {}
}