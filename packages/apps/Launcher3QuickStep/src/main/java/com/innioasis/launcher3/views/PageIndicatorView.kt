package com.innioasis.launcher3.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.innioasis.launcher3.R

class PageIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.page_indicator_active)
    }

    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.page_indicator_inactive)
    }

    private var currentPage = 0
    private var totalPages = 1
    private val indicatorRadius = resources.getDimension(R.dimen.page_indicator_radius)
    private val indicatorSpacing = resources.getDimension(R.dimen.page_indicator_spacing)

    fun setCurrentPage(current: Int, total: Int) {
        currentPage = current.coerceIn(0, total - 1)
        totalPages = maxOf(1, total)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = (totalPages * (indicatorRadius * 2 + indicatorSpacing) - indicatorSpacing).toInt()
        val height = (indicatorRadius * 2).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height / 2f
        val startX = (width - (totalPages * (indicatorRadius * 2 + indicatorSpacing) - indicatorSpacing)) / 2f

        for (i in 0 until totalPages) {
            val x = startX + i * (indicatorRadius * 2 + indicatorSpacing) + indicatorRadius
            if (i == currentPage) {
                canvas.drawCircle(x, centerY, indicatorRadius, activePaint)
            } else {
                canvas.drawCircle(x, centerY, indicatorRadius * 0.6f, inactivePaint)
            }
        }
    }
}