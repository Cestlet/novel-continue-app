package com.novel.continueapp.data

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 用 ML Kit 离线中文 OCR 识别截图中的文字（无障碍服务兜底方案）。
 */
class OcrEngine {

    private val recognizer by lazy {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    /**
     * 识别 Bitmap 中的文字，返回按行/块排列的文本。
     */
    suspend fun recognize(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = suspendCancellableCoroutine<com.google.mlkit.vision.text.Text> { cont ->
            recognizer.process(image)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
        result.text
    }

    /**
     * 释放资源。
     */
    fun close() {
        recognizer.close()
    }
}