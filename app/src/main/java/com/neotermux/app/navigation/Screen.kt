package com.neotermux.app.navigation

sealed class Screen(val route: String) {
    data object Terminal : Screen("terminal")
    data object Settings : Screen("settings")
    data object FileManager : Screen("filemanager")
    data object PackageManager : Screen("packagemanager")
    data object ProcessManager : Screen("processmanager")
    data object Git : Screen("git")
    data object Ssh : Screen("ssh")
    data object Editor : Screen("editor")
}
