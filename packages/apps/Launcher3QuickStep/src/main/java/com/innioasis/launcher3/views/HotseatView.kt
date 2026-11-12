package com.innioasis.launcher3.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.innioasis.launcher3.model.AppInfo

class HotseatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var recyclerView: RecyclerView? = null
    private var adapter: HotseatAdapter? = null
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

            adapter = HotseatAdapter().also { this.adapter = it }
            this.adapter = adapter

            addItemDecoration(HotseatItemDecoration(resources.getDimensionPixelSize(R.dimen.hotseat_item_spacing)))
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

    fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        // Recalculate layout
        adapter?.notifyDataSetChanged()
    }

    private inner class HotseatAdapter : RecyclerView.Adapter<HotseatViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotseatViewHolder {
            val view = View.inflate(context, R.layout.hotseat_item, null) as ViewGroup
            return HotseatViewHolder(view)
        }

        override fun onBindViewHolder(holder: HotseatViewHolder, position: Int) {
            if (position < apps.size) {
                holder.bind(apps[position])
            }
        }

        override fun getItemCount(): Int = apps.size
    }

    private inner class HotseatViewHolder(view: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val iconView: AppIconView = view.findViewById(R.id.app_icon)

        fun bind(appInfo: AppInfo) {
            iconView.setAppInfo(appInfo)
            itemView.setOnClickListener { onItemClickListener?.onItemClick(appInfo) }
        }
    }

    private class HotseatItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            if (position > 0) {
                outRect.left = spacing
            }
        }
    }
}