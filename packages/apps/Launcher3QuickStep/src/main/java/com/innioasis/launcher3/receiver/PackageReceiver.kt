package com.innioasis.launcher3.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.innioasis.launcher3.model.AppModel
import com.innioasis.launcher3.util.LauncherLogs

class PackageReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val packageName = intent.data?.schemeSpecificPart

        LauncherLogs.d("PackageReceiver", "Received $action for $packageName")

        when (action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_CHANGED,
            Intent.ACTION_PACKAGE_REPLACED,
            Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE,
            Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE -> {
                // Reload apps asynchronously
                AppModel.getInstance(context).loadApps(context) { }
            }
        }
    }
}