package com.novel.continueapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    // ── LLM 配置 ──
    val baseUrl: Flow<String> = context.dataStore.data.map { it[KEY_BASE_URL] ?: DEFAULT_BASE_URL }
    val apiKey: Flow<String> = context.dataStore.data.map { it[KEY_API_KEY] ?: "" }
    val model: Flow<String> = context.dataStore.data.map { it[KEY_MODEL] ?: DEFAULT_MODEL }
    val temperature: Flow<Float> = context.dataStore.data.map { it[KEY_TEMPERATURE] ?: 1.0f }
    val styleHint: Flow<String> = context.dataStore.data.map { it[KEY_STYLE_HINT] ?: "" }

    // ── 功能开关 ──
    /** 是否启用截图 OCR 兜底（默认开启） */
    val ocrFallbackEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_OCR_FALLBACK] ?: true }
    /** 是否自动从无障碍获取文本 */
    val autoCaptureEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_CAPTURE] ?: true }

    // ── 过滤 ──
    /** 仅监听指定包名（空 = 监听所有 App） */
    val targetPackage: Flow<String> = context.dataStore.data.map { it[KEY_TARGET_PACKAGE] ?: "" }

    // ── 版本 ──
    val lastCaptureText: Flow<String> = context.dataStore.data.map { it[KEY_LAST_CAPTURE] ?: "" }

    suspend fun saveBaseUrl(value: String) { context.dataStore.edit { it[KEY_BASE_URL] = value } }
    suspend fun saveApiKey(value: String) { context.dataStore.edit { it[KEY_API_KEY] = value } }
    suspend fun saveModel(value: String) { context.dataStore.edit { it[KEY_MODEL] = value } }
    suspend fun saveTemperature(value: Float) { context.dataStore.edit { it[KEY_TEMPERATURE] = value } }
    suspend fun saveStyleHint(value: String) { context.dataStore.edit { it[KEY_STYLE_HINT] = value } }
    suspend fun saveOcrFallback(enabled: Boolean) { context.dataStore.edit { it[KEY_OCR_FALLBACK] = enabled } }
    suspend fun saveAutoCapture(enabled: Boolean) { context.dataStore.edit { it[KEY_AUTO_CAPTURE] = enabled } }
    suspend fun saveTargetPackage(value: String) { context.dataStore.edit { it[KEY_TARGET_PACKAGE] = value } }
    suspend fun saveLastCaptureText(value: String) { context.dataStore.edit { it[KEY_LAST_CAPTURE] = value } }

    companion object {
        private val KEY_BASE_URL = stringPreferencesKey("base_url")
        private val KEY_API_KEY = stringPreferencesKey("api_key")
        private val KEY_MODEL = stringPreferencesKey("model")
        private val KEY_TEMPERATURE = floatPreferencesKey("temperature")
        private val KEY_STYLE_HINT = stringPreferencesKey("style_hint")
        private val KEY_OCR_FALLBACK = booleanPreferencesKey("ocr_fallback")
        private val KEY_AUTO_CAPTURE = booleanPreferencesKey("auto_capture")
        private val KEY_TARGET_PACKAGE = stringPreferencesKey("target_package")
        private val KEY_LAST_CAPTURE = stringPreferencesKey("last_capture_text")

        const val DEFAULT_BASE_URL = "https://api.deepseek.com"
        const val DEFAULT_MODEL = "deepseek-chat"
    }
}