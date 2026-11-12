package com.innioasis.launcher3

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import androidx.lifecycle.ProcessLifecycleOwner
import com.innioasis.launcher3.util.LauncherLogs

class LauncherApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Disable StrictMode for performance
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build())
        } else {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .permitAll()
                .build())
        }

        // Initialize ProcessLifecycleOwner early
        ProcessLifecycleOwner.init(this)

        // Pre-load classes for faster startup
        LauncherLogs.init(this)

        // Set default app locale
        val config = resources.configuration
        config.setLocales(android.os.LocaleList.getDefault())
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Aggressive memory trimming for 128MB target
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // UI hidden, clear image caches
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                )
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
            ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // System running low on memory
                System.gc()
                System.runFinalization()
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
        System.runFinalization()
    }
}