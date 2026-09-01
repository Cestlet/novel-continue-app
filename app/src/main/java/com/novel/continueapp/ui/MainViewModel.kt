package com.novel.continueapp.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novel.continueapp.NovelApp
import com.novel.continueapp.data.OcrEngine
import com.novel.continueapp.model.Book
import com.novel.continueapp.service.FloatWindowService
import com.novel.continueapp.service.NovelAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NovelApp
    private val repo = app.repository
    private val settings = app.settingsRepository

    // ── 小说管理 ──

    /** 所有小说列表 */
    var bookList: List<Book> by mutableStateOf(emptyList())
        private set

    /** 当前选中的小说 */
    var currentBook: Book? by mutableStateOf(null)
        private set

    /** 当前小说是否已加载 */
    val hasCurrentBook: Boolean get() = currentBook != null

    /** 当前小说名 */
    var currentBookTitle: String by mutableStateOf("")
        internal set

    // ── 捕获与续写状态 ──

    /** 当前显示/编辑的原文 */
    var editableSource: String by mutableStateOf("")
        private set

    /** 续写结果 */
    var continueResult: String by mutableStateOf("")
        private set

    /** 续写中 */
    var isContinuing: Boolean by mutableStateOf(false)
        private set

    /** 错误信息 */
    var errorMessage: String by mutableStateOf("")
        internal set

    /** 无障碍服务是否运行 */
    var isServiceRunning: Boolean by mutableStateOf(NovelAccessibilityService.isRunning)
        private set

    /** 当前包名 */
    var currentPackage: String by mutableStateOf(NovelAccessibilityService.currentPackage)
        private set

    /** 最近捕获时间 */
    var lastCaptureTime: Long by mutableStateOf(NovelAccessibilityService.lastCaptureTime)
        private set

    /** 悬浮窗状态 */
    var isFloatWindowShowing: Boolean by mutableStateOf(false)
        private set
    var isFloatCapturing: Boolean by mutableStateOf(false)
        private set

    // ── 设置值 ──
    var baseUrl: String by mutableStateOf("")
    var apiKey: String by mutableStateOf("")
    var model: String by mutableStateOf("")
    var temperature: Float by mutableStateOf(1.0f)
    var styleHint: String by mutableStateOf("")
    var autoCapture: Boolean by mutableStateOf(true)
    var ocrFallback: Boolean by mutableStateOf(true)
    var targetPackage: String by mutableStateOf("")

    private var capturePollJob: Job? = null
    private var ocrEngine: OcrEngine? = null

    init {
        loadSettings()
        pollServiceState()
        loadBooks()
        // 监听服务文本变化
        NovelAccessibilityService.onTextChanged = { text ->
            onCapturedText(text)
        }
    }

    // ── 小说管理 ──

    /** 加载小说列表 */
    fun loadBooks() {
        viewModelScope.launch {
            repo.allBooks.collect { books ->
                bookList = books
                // 如果当前没有选中的小说，自动选择第一个
                if (currentBook == null && books.isNotEmpty()) {
                    selectBook(books.first())
                }
            }
        }
    }

    /** 选中一本小说 */
    fun selectBook(book: Book) {
        currentBook = book
        currentBookTitle = book.title
        editableSource = book.capturedText
        continueResult = book.continueText
    }

    /** 新建一本小说 */
    fun createBook(title: String = "未命名小说") {
        viewModelScope.launch {
            val id = repo.createBook(title)
            val book = repo.getBook(id)
            if (book != null) {
                selectBook(book)
                errorMessage = "已创建小说「${book.title}」"
            }
        }
    }

    /** 更新当前小说名 */
    fun updateBookTitle(newTitle: String) {
        currentBook?.let { book ->
            if (newTitle.isNotBlank()) {
                currentBookTitle = newTitle
                viewModelScope.launch {
                    repo.updateBook(book.copy(title = newTitle))
                }
            }
        }
    }

    /** 删除一本小说 */
    fun deleteBook(book: Book) {
        viewModelScope.launch {
            repo.deleteBook(book)
            if (currentBook?.id == book.id) {
                currentBook = null
                editableSource = ""
                continueResult = ""
            }
        }
    }

    // ── 捕获文本处理 ──

    /**
     * 当捕获到新文本时调用。
     * 如果有当前小说，自动追加到小说中。
     */
    private fun onCapturedText(text: String) {
        val book = currentBook ?: return
        captureText = text
        // 追加到当前小说
        viewModelScope.launch {
            val merged = repo.appendCapturedText(book.id, text)
            editableSource = merged
            // 更新本地 book 对象
            currentBook = repo.getBook(book.id)
        }
    }

    /** 当前捕获的原文（临时） */
    var captureText: String by mutableStateOf("")
        private set

    /** 手动更新编辑区文本 */
    fun updateSourceText(text: String) {
        editableSource = text
    }

    // ── 设置 ──

    private fun loadSettings() {
        viewModelScope.launch {
            baseUrl = settings.baseUrl.first()
            apiKey = settings.apiKey.first()
            model = settings.model.first()
            temperature = settings.temperature.first()
            styleHint = settings.styleHint.first()
            autoCapture = settings.autoCaptureEnabled.first()
            ocrFallback = settings.ocrFallbackEnabled.first()
            targetPackage = settings.targetPackage.first()
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            settings.saveBaseUrl(baseUrl)
            settings.saveApiKey(apiKey)
            settings.saveModel(model)
            settings.saveTemperature(temperature)
            settings.saveStyleHint(styleHint)
            settings.saveAutoCapture(autoCapture)
            settings.saveOcrFallback(ocrFallback)
            settings.saveTargetPackage(targetPackage)
        }
    }

    // ── 服务状态轮询 ──

    private fun pollServiceState() {
        capturePollJob = viewModelScope.launch {
            while (true) {
                isServiceRunning = NovelAccessibilityService.isRunning
                currentPackage = NovelAccessibilityService.currentPackage
                isFloatWindowShowing = FloatWindowService.isShowing
                isFloatCapturing = FloatWindowService.isCapturing
                delay(1000)
            }
        }
    }

    // ── 打开无障碍设置 ──

    fun openAccessibilitySettings() {
        val context = getApplication<Application>()
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // ── 悬浮窗控制 ──

    fun toggleFloatWindow(context: Context) {
        if (isFloatWindowShowing) {
            context.stopService(Intent(context, FloatWindowService::class.java))
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!android.provider.Settings.canDrawOverlays(context)) {
                    try {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        errorMessage = "请在设置中手动开启悬浮窗权限"
                    }
                    return
                }
            }
            context.startService(Intent(context, FloatWindowService::class.java))
        }
    }

    // ── 续写 ──

    fun doContinue() {
        if (currentBook == null) {
            errorMessage = "请先新建一本小说"
            return
        }
        val source = editableSource.trim()
        if (source.isBlank()) {
            errorMessage = "请先识别原文或粘贴小说内容"
            return
        }
        if (apiKey.isBlank()) {
            errorMessage = "请先在设置中填写 API Key"
            return
        }
        isContinuing = true
        errorMessage = ""
        continueResult = ""
        viewModelScope.launch {
            try {
                val result = repo.continueText(source)
                continueResult = result
                // 保存到当前小说
                currentBook?.let { book ->
                    repo.saveContinueResult(book.id, result)
                    // 刷新当前小说
                    currentBook = repo.getBook(book.id)
                }
                errorMessage = "✅ 续写完成，已保存到「${currentBook?.title}」"
            } catch (e: Exception) {
                errorMessage = "续写失败: ${e.message}"
            } finally {
                isContinuing = false
            }
        }
    }

    // ── OCR 截图 ──

    fun doOcrOnBitmap(bitmap: Bitmap) {
        if (!ocrFallback) {
            errorMessage = "截图 OCR 兜底已关闭，请先在设置中开启"
            return
        }
        viewModelScope.launch {
            try {
                if (ocrEngine == null) ocrEngine = OcrEngine()
                val text = withContext(Dispatchers.IO) {
                    ocrEngine!!.recognize(bitmap)
                }
                if (text.isNotBlank()) {
                    val cleaned = repo.cleanText(text)
                    onCapturedText(cleaned)
                    errorMessage = "OCR 识别成功: ${cleaned.length} 字"
                } else {
                    errorMessage = "OCR 未识别到文字"
                }
            } catch (e: Exception) {
                errorMessage = "OCR 失败: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        capturePollJob?.cancel()
        ocrEngine?.close()
        NovelAccessibilityService.onTextChanged = null
    }
}