package com.novel.continueapp.data

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * 调用 OpenAI 兼容接口（如 DeepSeek）续写小说。
 */
class ApiClient(private val gson: Gson) {

    // 长输出需要更长的读取超时
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(600, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * 续写小说（不限制字数与 token，按需输出全部内容）。
     * @param sourceText 原文
     * @param baseUrl 接口地址（如 https://api.deepseek.com）
     * @param apiKey API Key
     * @param model 模型名
     * @param temperature 温度
     * @param styleHint 风格要求
     * @return 续写文本
     */
    suspend fun continueNovel(
        sourceText: String,
        baseUrl: String,
        apiKey: String,
        model: String,
        temperature: Float,
        styleHint: String
    ): String = withContext(Dispatchers.IO) {
        val endpoint = buildEndpoint(baseUrl)
        val stylePart = if (styleHint.isNotBlank()) "。续写风格要求：$styleHint" else "。"
        val userPrompt = buildString {
            append("请阅读下面这段小说的正文，然后自然地继续往下写$stylePart\n")
            append("要求：承接情节与人设，语言风格与原文一致，不要重复原文句子；")
            append("不要因为任何字数或长度限制而提前中断，写到情节自然的段落为止，完整输出所有内容。\n\n")
            append("———— 原文开始 ————\n")
            append(sourceText)
            append("\n———— 原文结束 ————\n\n")
            append("请直接输出续写内容：")
        }

        // 不设置 max_tokens，让模型按需输出全部内容
        val bodyMap = mapOf(
            "model" to model,
            "messages" to listOf(
                mapOf("role" to "system", "content" to SYSTEM_PROMPT),
                mapOf("role" to "user", "content" to userPrompt)
            ),
            "temperature" to temperature,
            "stream" to false
        )
        val jsonBody = gson.toJson(bodyMap).toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(jsonBody)
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw RuntimeException("API 返回空响应")
        if (!response.isSuccessful) {
            throw RuntimeException("API 返回 ${response.code}: ${body.take(500)}")
        }

        try {
            val result = gson.fromJson(body, Map::class.java)
            val choices = result["choices"] as? List<*> ?: throw RuntimeException("响应缺少 choices")
            val first = choices.firstOrNull() as? Map<*, *> ?: throw RuntimeException("choices 为空")
            val message = first["message"] as? Map<*, *> ?: throw RuntimeException("缺少 message")
            (message["content"] as? String)?.trim() ?: throw RuntimeException("content 为空")
        } catch (e: Exception) {
            throw RuntimeException("解析响应失败: ${e.message} | body: ${body.take(300)}")
        }
    }

    private fun buildEndpoint(baseUrl: String): String {
        val base = baseUrl.trimEnd('/')
        return if (base.endsWith("/v1")) "$base/chat/completions"
        else "$base/v1/chat/completions"
    }

    companion object {
        private const val SYSTEM_PROMPT = "你是一位资深的小说续写助手。你会收到一段小说的正文，请基于原文的人物设定、文风、叙事节奏与当前情节，自然地续写下去，不要总结原文，不要跳出故事评论，直接继续叙事。续写内容要连贯、有画面感、符合中文网文/小说的表达习惯。"
    }
}