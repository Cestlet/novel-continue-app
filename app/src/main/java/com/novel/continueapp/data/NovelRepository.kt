package com.novel.continueapp.data

import android.content.Context
import com.novel.continueapp.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * 数据仓库：统一管理小说、识别、续写、存储。
 */
class NovelRepository(context: Context) {

    private val db = NovelDatabase.getInstance(context)
    private val bookDao = db.bookDao()
    private val settingsRepo = SettingsRepository(context)
    private val apiClient = ApiClient(com.google.gson.Gson())

    // ── 小说操作 ──

    /** 所有小说列表（按更新时间倒序） */
    val allBooks: Flow<List<Book>> = bookDao.getAllFlow()

    suspend fun getBook(id: Long): Book? = bookDao.getById(id)

    suspend fun createBook(title: String = "未命名小说"): Long {
        val book = Book(title = title)
        return bookDao.insert(book)
    }

    suspend fun updateBook(book: Book) = bookDao.update(book)

    suspend fun deleteBook(book: Book) = bookDao.delete(book)

    /**
     * 向当前小说追加捕获的原文（去重）。
     * 返回追加后的文本。
     */
    suspend fun appendCapturedText(bookId: Long, newText: String): String {
        val book = bookDao.getById(bookId) ?: return newText
        val existing = book.capturedText
        val merged = if (existing.isBlank()) newText
        else TextCleaner.clean(existing + "\n" + newText)
        val updated = book.copy(capturedText = merged, updatedAt = System.currentTimeMillis())
        bookDao.update(updated)
        return merged
    }

    /**
     * 保存续写结果到小说。
     */
    suspend fun saveContinueResult(bookId: Long, result: String) {
        val book = bookDao.getById(bookId) ?: return
        val existing = book.continueText
        val merged = if (existing.isBlank()) result
        else existing + "\n\n---\n\n" + result
        val updated = book.copy(continueText = merged, updatedAt = System.currentTimeMillis())
        bookDao.update(updated)
    }

    // ── 续写 ──

    suspend fun continueText(sourceText: String): String {
        val baseUrl = settingsRepo.baseUrl.first()
        val apiKey = settingsRepo.apiKey.first()
        val model = settingsRepo.model.first()
        val temperature = settingsRepo.temperature.first()
        val maxTokens = settingsRepo.maxTokens.first()
        val lengthHint = settingsRepo.lengthHint.first()
        val styleHint = settingsRepo.styleHint.first()

        return apiClient.continueNovel(
            sourceText = sourceText,
            baseUrl = baseUrl,
            apiKey = apiKey,
            model = model,
            temperature = temperature,
            maxTokens = maxTokens,
            lengthHint = lengthHint,
            styleHint = styleHint
        )
    }

    /**
     * 清洗单段文本。
     */
    fun cleanText(raw: String): String = TextCleaner.clean(raw)
}