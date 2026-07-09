pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "NeoTermux"

include(":app")
include(":termlib")
include(":editor")
include(":filemanager")
include(":sshclient")
include(":gitlib")
include(":terminal-emulator")

gradle.beforeSettings {
    includeBuild("build-logic")
}