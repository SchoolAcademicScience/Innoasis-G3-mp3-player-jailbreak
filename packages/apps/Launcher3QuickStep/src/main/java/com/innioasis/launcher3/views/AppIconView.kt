package com.innioasis.launcher3.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.innioasis.launcher3.R
import com.innioasis.launcher3.model.AppInfo

class AppIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var iconView: ImageView
    private var labelView: TextView
    private var appInfo: AppInfo? = null
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
        isDither = true
    }

    init {
        val view = View.inflate(context, R.layout.app_icon_view, this)
        iconView = view.findViewById(R.id.icon)
        labelView = view.findViewById(R.id.label)

        // Optimize for low memory
        setWillNotDraw(false)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    fun setAppInfo(appInfo: AppInfo) {
        this.appInfo = appInfo
        iconView.setImageDrawable(appInfo.icon)
        labelView.text = appInfo.label

        // Apply adaptive icon mask if available
        if (appInfo.icon is AdaptiveIconDrawable) {
            iconView.setBackgroundResource(R.drawable.adaptive_icon_background)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw selection/focus indicator if needed
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Square icon with label below
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val iconSize = width
        val labelHeight = labelView.paddingTop + labelView.paddingBottom + measureTextHeight()
        val height = iconSize + labelHeight
        setMeasuredDimension(width, height)
    }

    private fun measureTextHeight(): Int {
        val bounds = Rect()
        labelView.paint.getTextBounds(labelView.text.toString(), 0, labelView.text.length, bounds)
        return bounds.height()
    }

    fun setIconTint(color: Int) {
        iconView.setColorFilter(color)
    }

    fun clearIconTint() {
        iconView.clearColorFilter()
    }
}