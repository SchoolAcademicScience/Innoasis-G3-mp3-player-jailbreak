package com.innioasis.launcher3.model

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.innioasis.launcher3.util.LauncherLogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class AppModel private constructor(context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val launcherApps: LauncherApps = context.getSystemService(LauncherApps::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var appsCache: List<AppInfo> = emptyList()
    private var loadCallback: ((List<AppInfo>) -> Unit)? = null

    companion object {
        @Volatile private var INSTANCE: AppModel? = null

        fun getInstance(context: Context): AppModel = INSTANCE ?: synchronized(this) {
            INSTANCE ?: AppModel(context.applicationContext).also { INSTANCE = it }
        }
    }

    fun loadApps(context: Context, callback: (List<AppInfo>) -> Unit) {
        loadCallback = callback
        // Load on background thread
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val apps = loadAppsInternal(context)
            mainHandler.post {
                appsCache = apps
                callback(apps)
                loadCallback = null
            }
        }
    }

    private fun loadAppsInternal(context: Context): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()
        val mainIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        // Use LauncherApps for better performance on Android 10+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val launcherActivityInfos = launcherApps.getActivityList(null, android.os.Process.myUserHandle())
            for (info in launcherActivityInfos) {
                try {
                    val appInfo = createAppInfo(info, context)
                    if (appInfo != null) apps.add(appInfo)
                } catch (e: Exception) {
                    LauncherLogs.e("AppModel", "Error loading app ${info.packageName}", e)
                }
            }
        } else {
            // Fallback for older Android versions
            val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
            for (resolveInfo in resolveInfos) {
                try {
                    val appInfo = createAppInfo(resolveInfo, context)
                    if (appInfo != null) apps.add(appInfo)
                } catch (e: Exception) {
                    LauncherLogs.e("AppModel", "Error loading app ${resolveInfo.activityInfo.packageName}", e)
                }
            }
        }

        // Sort: user apps first, then system apps, alphabetically
        return apps.sortedWith(compareBy(
            { it.isSystemApp }, // system apps last
            { it.label.lowercase() } // alphabetical
        ))
    }

    private fun createAppInfo(launcherActivityInfo: LauncherActivityInfo, context: Context): AppInfo? {
        val packageName = launcherActivityInfo.packageName
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        val label = packageManager.getApplicationLabel(appInfo).toString()
        val icon = launcherActivityInfo.getBadgedIcon(0)
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isDisabled = packageManager.getApplicationEnabledSetting(packageName) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED

        return AppInfo(
            packageName = packageName,
            label = label,
            icon = icon,
            activityInfo = launcherActivityInfo.activityInfo,
            isSystemApp = isSystem,
            isDisabled = isDisabled
        )
    }

    private fun createAppInfo(resolveInfo: ResolveInfo, context: Context): AppInfo? {
        val activityInfo = resolveInfo.activityInfo
        val packageName = activityInfo.packageName
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        val label = packageManager.getApplicationLabel(appInfo).toString()
        val icon = packageManager.getApplicationIcon(appInfo)
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isDisabled = packageManager.getApplicationEnabledSetting(packageName) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED

        return AppInfo(
            packageName = packageName,
            label = label,
            icon = icon,
            activityInfo = activityInfo,
            isSystemApp = isSystem,
            isDisabled = isDisabled
        )
    }

    fun getCachedApps(): List<AppInfo> = appsCache

    fun clear() {
        appsCache = emptyList()
        loadCallback = null
    }
}