package com.innioasis.launcher3.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PagerLayoutManager(
    context: Context,
    orientation: Int,
    reverseLayout: Boolean
) : LinearLayoutManager(context, orientation, reverseLayout) {

    constructor(context: Context) : this(context, HORIZONTAL, false)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
        super(context, attrs, defStyleAttr, defStyleRes)

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
        return lp.width == LayoutParams.MATCH_PARENT && lp.height == LayoutParams.MATCH_PARENT
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        // Ensure each page fills the screen
        val childCount = itemCount
        for (i in 0 until childCount) {
            val view = recycler.getViewForPosition(i)
            addView(view)
            measureChildWithMargins(view, 0, 0)
            val width = width - paddingLeft - paddingRight
            val height = height - paddingTop - paddingBottom
            layoutDecoratedWithMargins(view, 0, 0, width, height)
        }
    }

    override fun canScrollHorizontally(): Boolean = orientation == HORIZONTAL

    override fun canScrollVertically(): Boolean = orientation == VERTICAL

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        // Snap to page boundaries
        return super.scrollHorizontallyBy(dx, recycler, state)
    }

    override fun computeHorizontalScrollOffset(state: RecyclerView.State): Int {
        return super.computeHorizontalScrollOffset(state)
    }
}