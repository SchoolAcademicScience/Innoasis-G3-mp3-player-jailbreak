package com.innioasis.launcher3.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.innioasis.launcher3.model.AppModel
import com.innioasis.launcher3.util.LauncherLogs

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        LauncherLogs.d("BootReceiver", "Received $action")

        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            // Pre-load app model on boot
            AppModel.getInstance(context).loadApps(context) { }
        }
    }
}