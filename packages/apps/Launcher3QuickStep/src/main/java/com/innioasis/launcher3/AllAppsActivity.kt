package com.innioasis.launcher3

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.innioasis.launcher3.model.AppInfo
import com.innioasis.launcher3.model.AppModel
import com.innioasis.launcher3.util.LauncherLogs
import com.innioasis.launcher3.views.AppIconView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllAppsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AllAppsActivity"
        private const val ANIMATION_DURATION = 250L
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var closeButton: ImageView
    private lateinit var emptyView: View
    private var adapter: AllAppsAdapter? = null
    private var allApps: List<AppInfo> = emptyList()
    private var filteredApps: List<AppInfo> = emptyList()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var isClosing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_apps)

        recyclerView = findViewById(R.id.apps_recycler)
        searchEditText = findViewById(R.id.search_edit_text)
        closeButton = findViewById(R.id.close_button)
        emptyView = findViewById(R.id.empty_view)

        setupRecyclerView()
        setupSearch()
        setupCloseButton()
        loadApps()

        // Enter animation
        animateEnter()
    }

    private fun setupRecyclerView() {
        val layoutManager = GridLayoutManager(this, 4).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = 1
            }
        }
        recyclerView.apply {
            this.layoutManager = layoutManager
            setHasFixedSize(true)
            itemAnimator = null
            adapter = AllAppsAdapter().also { this.adapter = it }
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        searchEditText.requestFocus()
        showKeyboard()
    }

    private fun setupCloseButton() {
        closeButton.setOnClickListener { closeActivity() }
    }

    private fun loadApps() {
        scope.launch {
            val apps = withContext(Dispatchers.IO) {
                AppModel.getInstance(this@AllAppsActivity).getCachedApps()
            }
            allApps = apps
            filteredApps = apps
            runOnUiThread { adapter?.notifyDataSetChanged() }
        }
    }

    private fun filterApps(query: String) {
        if (query.isBlank()) {
            filteredApps = allApps
        } else {
            val lowerQuery = query.lowercase()
            filteredApps = allApps.filter { it.label.lowercase().contains(lowerQuery) }
        }
        adapter?.notifyDataSetChanged()
        emptyView.visibility = if (filteredApps.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun animateEnter() {
        val container = findViewById<ViewGroup>(R.id.container)
        container.alpha = 0f
        container.translationY = resources.getDimension(R.dimen.all_apps_enter_translation).toFloat()

        container.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(ANIMATION_DURATION)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun closeActivity() {
        if (isClosing) return
        isClosing = true

        val container = findViewById<ViewGroup>(R.id.container)
        container.animate()
            .alpha(0f)
            .translationY(resources.getDimension(R.dimen.all_apps_exit_translation).toFloat())
            .setDuration(ANIMATION_DURATION)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    finish()
                    overridePendingTransition(0, 0)
                }
            })
            .start()
    }

    private fun showKeyboard() {
        searchEditText.postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(searchEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    override fun onBackPressed() {
        closeActivity()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            closeActivity()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private inner class AllAppsAdapter : RecyclerView.Adapter<AllAppsViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllAppsViewHolder {
            val view = View.inflate(parent.context, R.layout.all_apps_item, null) as ViewGroup
            return AllAppsViewHolder(view)
        }

        override fun onBindViewHolder(holder: AllAppsViewHolder, position: Int) {
            if (position < filteredApps.size) {
                holder.bind(filteredApps[position])
            }
        }

        override fun getItemCount(): Int = filteredApps.size
    }

    private inner class AllAppsViewHolder(view: ViewGroup) : RecyclerView.ViewHolder(view) {
        private val iconView: AppIconView = view.findViewById(R.id.app_icon)

        fun bind(appInfo: AppInfo) {
            iconView.setAppInfo(appInfo)
            itemView.setOnClickListener {
                launchApp(appInfo)
            }
        }

        private fun launchApp(appInfo: AppInfo) {
            val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                startActivity(intent)
                closeActivity()
            }
        }
    }
}