package com.innioasis.launcher3.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.innioasis.launcher3.model.AppInfo
import com.innioasis.launcher3.util.ScreenUtils
import kotlinx.android.synthetic.main.workspace_item.view.*

class WorkspaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var recyclerView: RecyclerView? = null
    private var adapter: WorkspaceAdapter? = null
    private var snapHelper: SnapHelper? = null
    private var gestureDetector: GestureDetector? = null
    private var onPageChangeListener: OnPageChangeListener? = null
    private var apps: List<AppInfo> = emptyList()
    private var currentPage = 0
    private val pageSize = 20 // 4x5 grid
    private var pageCount = 0

    interface OnPageChangeListener {
        fun onPageChanged(page: Int)
    }

    init {
        setupRecyclerView()
        setupGestureDetector()
    }

    private fun setupRecyclerView() {
        recyclerView = RecyclerView(context).apply {
            id = View.generateViewId()
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            itemAnimator = null // Disable animations for performance

            val layoutManager = PagerLayoutManager(context, PagerLayoutManager.HORIZONTAL, false)
            layoutManager.isAutoMeasureEnabled = true
            layoutManager.orientation = PagerLayoutManager.HORIZONTAL
            this.layoutManager = layoutManager

            adapter = WorkspaceAdapter().also { this.adapter = it }
            this.adapter = adapter

            snapHelper = PagerSnapHelper().also { this.snapHelper = it }
            snapHelper?.attachToRecyclerView(this)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val page = computeCurrentPage()
                        if (page != currentPage) {
                            currentPage = page
                            onPageChangeListener?.onPageChanged(page)
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    // Update page indicator during scroll
                }
            })
        }
        addView(recyclerView!!)
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                val SWIPE_THRESHOLD = 50
                val VELOCITY_THRESHOLD = 500
                if (e1 != null && e2 != null) {
                    val diffX = e2.x - e1.x
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            previousPage()
                        } else {
                            nextPage()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    fun setOnPageChangeListener(listener: OnPageChangeListener?) {
        onPageChangeListener = listener
    }

    fun setApps(apps: List<AppInfo>) {
        this.apps = apps
        pageCount = maxOf(1, (apps.size + pageSize - 1) / pageSize)
        adapter?.notifyDataSetChanged()
        recyclerView?.post {
            setCurrentPage(0, false)
        }
    }

    fun setCurrentPage(page: Int, animate: Boolean) {
        val targetPage = page.coerceIn(0, pageCount - 1)
        currentPage = targetPage
        val position = targetPage * 1 // One item per page in PagerLayoutManager
        if (animate) {
            recyclerView?.smoothScrollToPosition(position)
        } else {
            recyclerView?.scrollToPosition(position)
        }
        onPageChangeListener?.onPageChanged(targetPage)
    }

    fun nextPage() {
        if (currentPage < pageCount - 1) {
            setCurrentPage(currentPage + 1, true)
        }
    }

    fun previousPage() {
        if (currentPage > 0) {
            setCurrentPage(currentPage - 1, true)
        }
    }

    val currentPage: Int get() = currentPage

    val pageCount: Int get() = pageCount

    fun resume() {
        // Resume any paused animations
    }

    fun pause() {
        // Pause animations
    }

    fun onTrimMemory(level: Int) {
        // Clear image caches
        adapter?.clearImageCache()
    }

    fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        // Recalculate layout
        adapter?.notifyDataSetChanged()
    }

    private fun computeCurrentPage(): Int {
        val layoutManager = recyclerView?.layoutManager as? PagerLayoutManager
        return layoutManager?.findFirstVisibleItemPosition()?.coerceIn(0, pageCount - 1) ?: 0
    }

    private inner class WorkspaceAdapter : RecyclerView.Adapter<WorkspaceViewHolder>() {

        private val imageCache = mutableMapOf<String, android.graphics.drawable.Drawable>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkspaceViewHolder {
            val view = View.inflate(context, R.layout.workspace_page, null) as ViewGroup
            return WorkspaceViewHolder(view)
        }

        override fun onBindViewHolder(holder: WorkspaceViewHolder, position: Int) {
            val startIndex = position * pageSize
            val endIndex = min(startIndex + pageSize, apps.size)
            val pageApps = apps.subList(startIndex, endIndex)
            holder.bind(pageApps)
        }

        override fun getItemCount(): Int = pageCount

        fun clearImageCache() {
            imageCache.clear()
        }
    }

    private inner class WorkspaceViewHolder(view: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val gridView: androidx.gridlayout.widget.GridLayout = view.findViewById(R.id.app_grid)

        fun bind(pageApps: List<AppInfo>) {
            gridView.removeAllViews()
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setGravity(android.view.Gravity.CENTER)
            }

            for (app in pageApps) {
                val iconView = AppIconView(context).apply {
                    setAppInfo(app)
                    setOnClickListener { onAppClick(app) }
                    setOnLongClickListener { onAppLongClick(app) }
                    layoutParams = params
                }
                gridView.addView(iconView)
            }

            // Fill empty slots
            val emptyCount = pageSize - pageApps.size
            for (i in 0 until emptyCount) {
                val emptyView = View(context).apply {
                    layoutParams = params
                }
                gridView.addView(emptyView)
            }
        }

        private fun onAppClick(app: AppInfo) {
            // Handled by parent
        }

        private fun onAppLongClick(app: AppInfo): Boolean {
            // Show app info / uninstall options
            true
        }
    }
}