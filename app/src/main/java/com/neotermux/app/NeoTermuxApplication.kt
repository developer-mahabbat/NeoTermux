package com.neotermux.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NeoTermuxApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        val channels = listOf(
            NotificationChannel(
                "terminal_sessions", "Terminal Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active terminal session notifications"
                setShowBadge(true)
            },
            NotificationChannel(
                "downloads", "Downloads",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Download progress notifications"
                setShowBadge(true)
            },
            NotificationChannel(
                "package_updates", "Package Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Package update notifications"
                setShowBadge(true)
            }
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
}
