package com.novel.continueapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 一本小说：管理的单位，包含识别到的原文和续写内容。
 */
@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 书名（用户自定义） */
    var title: String = "未命名小说",
    /** 累计捕获的原文（追加） */
    var capturedText: String = "",
    /** 续写结果 */
    var continueText: String = "",
    /** 创建时间戳 */
    val createdAt: Long = System.currentTimeMillis(),
    /** 更新时间戳 */
    var updatedAt: Long = System.currentTimeMillis(),
    /** 备注 */
    var note: String = ""
) {
    /** 原文总字数 */
    val sourceLength: Int get() = capturedText.length
    /** 续写总字数 */
    val continueLength: Int get() = continueText.length
}