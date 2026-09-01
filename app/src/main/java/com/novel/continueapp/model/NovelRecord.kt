package com.novel.continueapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 一次识别 + 续写的完整记录。
 */
@Entity(tableName = "novel_records")
data class NovelRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 识别到的原文内容 */
    val sourceText: String = "",
    /** LLM 续写结果 */
    val continueText: String = "",
    /** 识别模式：accessibility / ocr */
    val captureMode: String = "accessibility",
    /** 创建时间戳 */
    val createdAt: Long = System.currentTimeMillis(),
    /** 包名（来源 App） */
    val packageName: String = "",
    /** 备注/标签 */
    val note: String = ""
)