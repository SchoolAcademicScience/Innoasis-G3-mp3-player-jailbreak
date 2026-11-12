package com.innioasis.launcher3.util

import android.content.Context
import android.util.Log

object LauncherLogs {
    private var sDebugEnabled = false
    private var sContext: Context? = null

    fun init(context: Context) {
        sContext = context.applicationContext
        sDebugEnabled = context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    fun d(tag: String, msg: String) {
        if (sDebugEnabled) Log.d(tag, msg)
    }

    fun i(tag: String, msg: String) {
        if (sDebugEnabled) Log.i(tag, msg)
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        Log.e(tag, msg, throwable)
    }

    fun v(tag: String, msg: String) {
        if (sDebugEnabled) Log.v(tag, msg)
    }

    fun wtf(tag: String, msg: String, throwable: Throwable? = null) {
        Log.wtf(tag, msg, throwable)
    }
}