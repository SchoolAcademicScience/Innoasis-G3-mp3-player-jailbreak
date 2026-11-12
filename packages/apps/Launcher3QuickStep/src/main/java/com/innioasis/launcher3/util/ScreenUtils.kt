package com.innioasis.launcher3.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

object ScreenUtils {
    private var sDisplayMetrics: DisplayMetrics? = null
    private var sScreenWidth = 0
    private var sScreenHeight = 0
    private var sDensity = 0f
    private var sDensityDpi = 0

    fun init(context: Context) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        sDisplayMetrics = metrics
        sScreenWidth = metrics.widthPixels
        sScreenHeight = metrics.heightPixels
        sDensity = metrics.density
        sDensityDpi = metrics.densityDpi
    }

    val screenWidth: Int get() = sScreenWidth
    val screenHeight: Int get() = sScreenHeight
    val density: Float get() = sDensity
    val densityDpi: Int get() = sDensityDpi

    fun dpToPx(dp: Int): Int = (dp * density + 0.5f).toInt()
    fun pxToDp(px: Int): Int = (px / density + 0.5f).toInt()

    fun isTablet(context: Context): Boolean {
        val (w, h) = if (sScreenWidth < sScreenHeight) sScreenWidth to sScreenHeight else sScreenHeight to sScreenWidth
        val smallestWidthDp = (w / density).toInt()
        return smallestWidthDp >= 600
    }

    fun isSmallScreen(): Boolean = sScreenWidth < 720 || sScreenHeight < 720
}