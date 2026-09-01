package com.novel.continueapp.data

/**
 * 清洗小说文本：过滤空行、时间戳类噪声行、纯符号行，并按行去重（保留首次出现）。
 */
object TextCleaner {
    private val TIMESTAMP_PATTERN = Regex("""^[\s:：.\-—–|/\\]*?(?:时间|时长|进度)?[\s:：.\-—–|/\\]*?\d{1,2}[:：]\d{2}(?:[:：]\d{2})?""")
    private val SYMBOLS = setOf(' ', '#', '*', '·', '•', '-', '—', '–', '_', '=', '|', '/', '\\')

    fun clean(raw: String): String {
        if (raw.isBlank()) return ""
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
        val seen = mutableSetOf<String>()
        val builder = StringBuilder()
        for (line in lines) {
            if (TIMESTAMP_PATTERN.matches(line) || line.all { it in SYMBOLS }) continue
            val key = line.replace("\\s".toRegex(), "").take(30)
            if (key.isNotEmpty() && key !in seen) {
                seen.add(key)
                if (builder.isNotEmpty()) builder.append("\n")
                builder.append(line)
            }
        }
        return builder.toString()
    }
}
