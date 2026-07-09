package com.neotermux.app.util

object Constants {
    const val APP_NAME = "NeoTermux"
    const val PACKAGE_NAME = "com.neotermux.app"
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1

    const val TERMINAL_FONT_SIZE_DEFAULT = 14f
    const val TERMINAL_FONT_SIZE_MIN = 8f
    const val TERMINAL_FONT_SIZE_MAX = 72f
    const val SCROLLBACK_LINES_DEFAULT = 10000
    const val PTY_BUFFER_SIZE = 65536

    const val SHARED_PREFS_NAME = "neotermux_prefs"
    const val PREFS_THEME_MODE = "theme_mode"
    const val PREFS_FONT_SIZE = "font_size"
    const val PREFS_FONT_FAMILY = "font_family"
    const val PREFS_DEFAULT_SHELL = "default_shell"
    const val PREFS_CURSOR_STYLE = "cursor_style"
    const val PREFS_EXTRA_KEYS = "extra_keys"

    val SHELL_DEFAULT = "/data/data/com.neotermux.app/files/usr/bin/bash"
    val SHELL_PATHS = listOf(
        "/data/data/com.neotermux.app/files/usr/bin/bash",
        "/data/data/com.neotermux.app/files/usr/bin/zsh",
        "/data/data/com.neotermux.app/files/usr/bin/fish",
        "/data/data/com.neotermux.app/files/usr/bin/sh",
        "/data/data/com.neotermux.app/files/usr/bin/dash",
        "/system/bin/sh"
    )

    val SUPPORTED_ARCHIVES = listOf("zip", "tar", "gz", "bz2", "xz", "rar", "7z")
    val SUPPORTED_LANGUAGES = listOf(
        "python", "c", "cpp", "java", "kotlin", "php", "go", "rust",
        "javascript", "typescript", "html", "css", "shell", "sql",
        "json", "yaml", "markdown"
    )
}
