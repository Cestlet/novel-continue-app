package com.novel.continueapp.service

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView

/**
 * 悬浮窗服务：显示一个小的控制按钮，点击切换识别开关。
 */
class FloatWindowService : Service() {

    companion object {
        private const val TAG = "FloatWindow"

        @Volatile var isCapturing: Boolean = false
            private set
        @Volatile var isShowing: Boolean = false
            private set

        var onToggleChanged: ((Boolean) -> Unit)? = null

        fun toggleCapture() {
            isCapturing = !isCapturing
            onToggleChanged?.invoke(isCapturing)
        }

        fun setCaptureState(enabled: Boolean) {
            isCapturing = enabled
            onToggleChanged?.invoke(enabled)
        }
    }

    private lateinit var windowManager: WindowManager
    private var floatView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    // 拖动相关
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (floatView == null) {
            createFloatView()
        }
        isShowing = true
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatView()
        isShowing = false
        isCapturing = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createFloatView() {
        val density = resources.displayMetrics.density
        val size = (56 * density).toInt()
        val radius = (28 * density).toInt()

        // 用 FrameLayout + TextView 构建悬浮窗
        val container = FrameLayout(this)
        container.layoutParams = FrameLayout.LayoutParams(size, size)

        val statusText = TextView(this).apply {
            text = "⏸"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        container.addView(statusText, FrameLayout.LayoutParams(size, size))

        // 样式：圆角
        val background = android.graphics.drawable.GradientDrawable()
        background.setShape(android.graphics.drawable.GradientDrawable.OVAL)
        background.setColor(0xAA757575.toInt())
        container.background = background

        // 点击切换
        container.setOnClickListener {
            FloatWindowService.toggleCapture()
            updateStyle(container, statusText)
        }

        // 拖动
        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    layoutParams?.let {
                        initialX = it.x
                        initialY = it.y
                    }
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams?.let { lp ->
                        lp.x = initialX + (event.rawX - initialTouchX).toInt()
                        lp.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(container, lp)
                    }
                    true
                }
                else -> false
            }
        }

        updateStyle(container, statusText)

        val lp = WindowManager.LayoutParams(
            size,
            size,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.START or Gravity.TOP
            x = (20 * density).toInt()
            y = (200 * density).toInt()
        }

        layoutParams = lp
        floatView = container
        windowManager.addView(container, lp)
    }

    private fun removeFloatView() {
        floatView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: Exception) { }
            floatView = null
        }
    }

    private fun updateStyle(container: View, statusText: TextView) {
        val bg = container.background as? android.graphics.drawable.GradientDrawable ?: return
        if (isCapturing) {
            bg.setColor(0xCC4CAF50.toInt()) // 绿色-识别中
            statusText.text = "▶"
        } else {
            bg.setColor(0xAA757575.toInt()) // 灰色-暂停
            statusText.text = "⏸"
        }
    }

    fun updateState() {
        floatView?.let { view ->
            val container = view as? FrameLayout ?: return
            val statusText = container.getChildAt(0) as? TextView ?: return
            updateStyle(container, statusText)
        }
    }
}