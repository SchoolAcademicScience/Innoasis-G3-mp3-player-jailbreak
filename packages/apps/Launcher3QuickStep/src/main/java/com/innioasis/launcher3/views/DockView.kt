package com.innioasis.launcher3.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.innioasis.launcher3.model.AppInfo

class DockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var recyclerView: RecyclerView? = null
    private var adapter: DockAdapter? = null
    private var onItemClickListener: OnItemClickListener? = null
    private var apps: List<AppInfo> = emptyList()

    interface OnItemClickListener {
        fun onItemClick(appInfo: AppInfo)
    }

    init {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView = RecyclerView(context).apply {
            id = View.generateViewId()
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            itemAnimator = null

            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager.isAutoMeasureEnabled = true
            this.layoutManager = layoutManager

            adapter = DockAdapter().also { this.adapter = it }
            this.adapter = adapter

            // Add spacing between items
            addItemDecoration(DockItemDecoration(resources.getDimensionPixelSize(R.dimen.dock_item_spacing)))
        }
        addView(recyclerView!!)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        onItemClickListener = listener
    }

    fun setApps(apps: List<AppInfo>) {
        this.apps = apps
        adapter?.notifyDataSetChanged()
    }

    private inner class DockAdapter : RecyclerView.Adapter<DockViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DockViewHolder {
            val view = View.inflate(context, R.layout.dock_item, null) as ViewGroup
            return DockViewHolder(view)
        }

        override fun onBindViewHolder(holder: DockViewHolder, position: Int) {
            if (position < apps.size) {
                holder.bind(apps[position])
            }
        }

        override fun getItemCount(): Int = apps.size
    }

    private inner class DockViewHolder(view: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val iconView: AppIconView = view.findViewById(R.id.app_icon)
        private val labelView: android.widget.TextView = view.findViewById(R.id.app_label)

        fun bind(appInfo: AppInfo) {
            iconView.setAppInfo(appInfo)
            labelView.text = appInfo.label
            itemView.setOnClickListener { onItemClickListener?.onItemClick(appInfo) }
            itemView.setOnLongClickListener {
                // Show context menu
                true
            }
        }
    }

    private class DockItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            if (position > 0) {
                outRect.left = spacing
            }
            if (position == parent.adapter?.itemCount?.minus(1)) {
                outRect.right = spacing
            }
        }
    }
}