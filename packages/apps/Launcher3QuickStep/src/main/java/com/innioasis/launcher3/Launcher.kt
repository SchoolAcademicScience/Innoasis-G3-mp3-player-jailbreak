package com.innioasis.launcher3

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.innioasis.launcher3.model.AppInfo
import com.innioasis.launcher3.model.AppModel
import com.innioasis.launcher3.util.LauncherLogs
import com.innioasis.launcher3.views.AppIconView
import com.innioasis.launcher3.views.DockView
import com.innioasis.launcher3.views.HotseatView
import com.innioasis.launcher3.views.PageIndicatorView
import com.innioasis.launcher3.views.WorkspaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class Launcher : AppCompatActivity(), LifecycleObserver, WorkspaceView.OnPageChangeListener {

    companion object {
        private const val TAG = "Launcher"
        private const val ANIMATION_DURATION = 200L
        private const val ALL_APPS_REQUEST_CODE = 100
    }

    private lateinit var workspaceView: WorkspaceView
    private lateinit var dockView: DockView
    private lateinit var hotseatView: HotseatView
    private lateinit var pageIndicatorView: PageIndicatorView
    private lateinit var allAppsButton: ImageView
    private lateinit var searchBar: TextView
    private lateinit var backgroundView: View

    private var appModel: AppModel? = null
    private var launcherApps: LauncherApps? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val handler = Handler(Looper.getMainLooper())
    private var gestureDetector: GestureDetector? = null
    private var isAllAppsVisible = false
    private var currentPage = 0
    private val pageChangeRunnable = Runnable { updatePageIndicator() }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Optimize window for low memory
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setContentView(R.layout.activity_launcher)

        // Initialize views
        workspaceView = findViewById(R.id.workspace)
        dockView = findViewById(R.id.dock)
        hotseatView = findViewById(R.id.hotseat)
        pageIndicatorView = findViewById(R.id.page_indicator)
        allAppsButton = findViewById(R.id.all_apps_button)
        searchBar = findViewById(R.id.search_bar)
        backgroundView = findViewById(R.id.background)

        // Setup listeners
        workspaceView.setOnPageChangeListener(this)
        setupGestureDetector()
        setupClickListeners()

        // Initialize app model
        appModel = AppModel.getInstance(this)
        launcherApps = getSystemService(LauncherApps::class.java)

        // Load apps asynchronously
        scope.launch {
            loadApps()
        }

        // Register lifecycle observer
        lifecycle.addObserver(this)

        // Register receivers
        registerReceivers()

        // Apply initial state
        applyInitialState()
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                if (isAllAppsVisible) return false
                val SWIPE_THRESHOLD = 100
                val VELOCITY_THRESHOLD = 1000
                if (e1 != null && e2 != null) {
                    val diffX = e2.x - e1.x
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            workspaceView.previousPage()
                        } else {
                            workspaceView.nextPage()
                        }
                        return true
                    }
                }
                return false
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                if (isAllAppsVisible) {
                    hideAllApps()
                    return true
                }
                return false
            }
        })

        workspaceView.setOnTouchListener { _, event ->
            gestureDetector?.onTouchEvent(event) ?: false
        }
    }

    private fun setupClickListeners() {
        allAppsButton.setOnClickListener { toggleAllApps() }
        searchBar.setOnClickListener { startSearch() }

        hotseatView.setOnItemClickListener { appInfo ->
            launchApp(appInfo)
        }

        dockView.setOnItemClickListener { appInfo ->
            launchApp(appInfo)
        }
    }

    private fun applyInitialState() {
        // Set initial page
        workspaceView.setCurrentPage(currentPage, false)
        updatePageIndicator()

        // Apply dynamic colors if available (Material You)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            applyDynamicColors()
        }
    }

    private fun applyDynamicColors() {
        // Material You dynamic colors
        val colorScheme = androidx.compose.material3.dynamicDarkColorScheme(this)
        // Apply to views
    }

    private suspend fun loadApps() = withContext(Dispatchers.IO) {
        appModel?.loadApps(this@Launcher) { apps ->
            withContext(Dispatchers.Main) {
                workspaceView.setApps(apps)
                hotseatView.setApps(apps.take(5))
                dockView.setApps(apps.take(4))
            }
        }
    }

    private fun launchApp(appInfo: AppInfo) {
        val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            if (isAllAppsVisible) hideAllApps()
        }
    }

    private fun toggleAllApps() {
        if (isAllAppsVisible) {
            hideAllApps()
        } else {
            showAllApps()
        }
    }

    private fun showAllApps() {
        isAllAppsVisible = true
        val allAppsIntent = Intent(this, AllAppsActivity::class.java)
        startActivityForResult(allAppsIntent, ALL_APPS_REQUEST_CODE)
        overridePendingTransition(R.anim.slide_up, R.anim.fade_in)
    }

    private fun hideAllApps() {
        isAllAppsVisible = false
        // AllAppsActivity handles its own exit animation
    }

    private fun startSearch() {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, "")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to Google search
            val googleIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com"))
            googleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(googleIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ALL_APPS_REQUEST_CODE) {
            isAllAppsVisible = false
        }
    }

    override fun onPageChanged(page: Int) {
        currentPage = page
        handler.removeCallbacks(pageChangeRunnable)
        handler.postDelayed(pageChangeRunnable, 50)
    }

    private fun updatePageIndicator() {
        pageIndicatorView.setCurrentPage(currentPage, workspaceView.pageCount)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        workspaceView.resume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        workspaceView.pause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        scope.cancel()
        handler.removeCallbacksAndMessages(null)
        unregisterReceivers()
        appModel?.clear()
        super.onDestroy()
    }

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE)
            addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE)
            addDataScheme("package")
        }
        registerReceiver(packageReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    private fun unregisterReceivers() {
        try {
            unregisterReceiver(packageReceiver)
        } catch (e: IllegalArgumentException) {
            // Ignore
        }
    }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            scope.launch {
                loadApps()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        workspaceView.onConfigurationChanged(newConfig)
        hotseatView.onConfigurationChanged(newConfig)
    }

    override fun onWindowInsetsChanged(rootView: View, insets: WindowInsets): WindowInsets {
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
        val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        workspaceView.setPadding(0, 0, 0, imeInsets.bottom)
        dockView.setPadding(systemInsets.left, 0, systemInsets.right, systemInsets.bottom)
        hotseatView.setPadding(systemInsets.left, 0, systemInsets.right, 0)

        return insets
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (isAllAppsVisible) {
                    hideAllApps()
                    true
                } else if (workspaceView.currentPage != 0) {
                    workspaceView.setCurrentPage(0, true)
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                }
            }
            KeyEvent.KEYCODE_APP_SWITCH -> {
                // Handle recent apps
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        workspaceView.onTrimMemory(level)
    }
}