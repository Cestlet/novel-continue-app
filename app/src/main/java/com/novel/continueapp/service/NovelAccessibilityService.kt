package com.novel.continueapp.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.novel.continueapp.data.SettingsRepository
import com.novel.continueapp.data.TextCleaner
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

/**
 * 无障碍服务：监听屏幕变化，收集小说文字。
 */
class NovelAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "NovelAS"
        private const val DEBOUNCE_MS = 600L
        private const val MIN_TEXT_LEN = 8

        @Volatile var currentText: String = ""
            private set
        @Volatile var lastCaptureTime: Long = 0L
            private set
        @Volatile var isRunning: Boolean = false
            private set
        @Volatile var currentPackage: String = ""
            private set
        @Volatile var onTextChanged: ((String) -> Unit)? = null
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val lastTextHash = AtomicLong(0L)

    @Volatile private var autoCaptureEnabled = true
    @Volatile private var targetPackage = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        Log.i(TAG, "无障碍服务已连接")

        val settings = SettingsRepository(applicationContext)
        scope.launch { settings.autoCaptureEnabled.collect { autoCaptureEnabled = it } }
        scope.launch { settings.targetPackage.collect { targetPackage = it } }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        currentPackage = event.packageName?.toString() ?: ""

        // 悬浮窗显示时，捕获开关由悬浮窗的启停状态决定；否则由设置中的自动捕获开关决定
        val capturing = if (FloatWindowService.isShowing) FloatWindowService.isCapturing else autoCaptureEnabled
        if (!capturing) return

        // 仅监听指定包名（未设置则不过滤）
        if (targetPackage.isNotBlank() && currentPackage != targetPackage) return

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        scope.launch {
            delay(DEBOUNCE_MS)
            captureText()
        }
    }

    override fun onInterrupt() {
        Log.i(TAG, "无障碍服务被中断")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        scope.cancel()
        Log.i(TAG, "无障碍服务已销毁")
    }

    private fun captureText() {
        val root = rootInActiveWindow ?: return
        try {
            val texts = mutableListOf<String>()
            collectTextNodes(root, texts)
            if (texts.isEmpty()) return

            val merged = texts.joinToString("\n")
            val hash = merged.hashCode().toLong()
            if (hash == lastTextHash.get()) return

            val cleaned = TextCleaner.clean(merged)
            if (cleaned.length < MIN_TEXT_LEN) return

            lastTextHash.set(hash)
            currentText = cleaned
            lastCaptureTime = System.currentTimeMillis()
            onTextChanged?.invoke(cleaned)
            Log.d(TAG, "捕获到文本: ${cleaned.length} 字")
        } finally {
            root.recycle()
        }
    }

    private fun collectTextNodes(node: AccessibilityNodeInfo, out: MutableList<String>) {
        try {
            if (!node.isVisibleToUser) return
            val text = node.text?.toString()?.trim() ?: ""
            if (text.isNotEmpty() && text.length >= MIN_TEXT_LEN) {
                out.add(text)
            } else if (text.isNotEmpty() && text.length > 2) {
                out.add(text)
            }
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                try {
                    collectTextNodes(child, out)
                } finally {
                    child.recycle()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "遍历节点异常: ${e.message}")
        }
    }
}