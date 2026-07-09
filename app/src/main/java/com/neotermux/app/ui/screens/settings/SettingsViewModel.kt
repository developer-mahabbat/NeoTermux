package com.neotermux.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsState(
    val isDarkMode: Boolean = false,
    val useDynamicColors: Boolean = true,
    val fontSize: Float = 14f,
    val fontFamily: String = "JetBrains Mono",
    val defaultShell: String = "/data/data/com.neotermux.app/files/usr/bin/bash",
    val scrollbackLines: Int = 10000,
    val showExtraKeys: Boolean = true,
    val keyVibration: Boolean = true,
    val keySound: Boolean = false,
    val appLock: Boolean = false,
    val biometricLock: Boolean = false,
    val keepScreenOn: Boolean = false,
    val showHiddenFiles: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _settings = MutableStateFlow(SettingsState())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    fun toggleDarkMode() { _settings.value = _settings.value.copy(isDarkMode = !_settings.value.isDarkMode, useDynamicColors = false) }
    fun toggleDynamicColors() { _settings.value = _settings.value.copy(useDynamicColors = !_settings.value.useDynamicColors) }
    fun setFontSize(size: Float) { _settings.value = _settings.value.copy(fontSize = size.coerceIn(8f, 72f)) }
    fun setScrollbackLines(lines: Int) { _settings.value = _settings.value.copy(scrollbackLines = lines.coerceIn(1000, 50000)) }
    fun toggleExtraKeys() { _settings.value = _settings.value.copy(showExtraKeys = !_settings.value.showExtraKeys) }
    fun toggleKeyVibration() { _settings.value = _settings.value.copy(keyVibration = !_settings.value.keyVibration) }
    fun toggleKeySound() { _settings.value = _settings.value.copy(keySound = !_settings.value.keySound) }
    fun toggleAppLock() { _settings.value = _settings.value.copy(appLock = !_settings.value.appLock) }
    fun toggleBiometricLock() { _settings.value = _settings.value.copy(biometricLock = !_settings.value.biometricLock) }
    fun resetSettings() { _settings.value = SettingsState() }
}
