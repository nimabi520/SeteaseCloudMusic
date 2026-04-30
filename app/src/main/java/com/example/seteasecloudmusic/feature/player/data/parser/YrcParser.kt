package com.example.seteasecloudmusic.feature.player.data.parser

import com.example.seteasecloudmusic.feature.player.domain.model.LyricLine
import com.example.seteasecloudmusic.feature.player.domain.model.LyricWord

object YrcParser {

    private val LINE_PATTERN = Regex("""\[(\d+),(\d+)]""")
    private val WORD_PATTERN = Regex("""\((\d+),(\d+),0\)""")
    private val BEGIN_PAREN = Regex("""^[（(]""")
    private val END_PAREN = Regex("""[）)]$""")

    fun parse(yrcText: String): List<LyricLine> {
        val lines = yrcText
            .split("\r?\n".toRegex())
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return lines.mapNotNull { lineStr ->
            val lineMatch = LINE_PATTERN.find(lineStr) ?: return@mapNotNull null
            val lineStart = lineMatch.groupValues[1].toInt()
            val lineDuration = lineMatch.groupValues[2].toInt()

            var content = lineStr.substring(lineMatch.range.last + 1).trimStart()
            if (content.isEmpty()) return@mapNotNull null

            val words = mutableListOf<LyricWord>()
            var lastStart = -1
            var lastEnd = -1

            while (content.isNotEmpty()) {
                val wordMatch = WORD_PATTERN.find(content) ?: break
                val prefixText = content.substring(0, wordMatch.range.first)
                val wordStart = wordMatch.groupValues[1].toInt()
                val wordDur = wordMatch.groupValues[2].toInt()
                val wordEnd = wordStart + wordDur

                // prefixText 是上一轮时间标签对应的词文本，此时才完整
                if (prefixText.isNotEmpty() && lastStart != -1) {
                    words.add(
                        LyricWord(
                            word = prefixText,
                            startTime = lastStart,
                            endTime = lastEnd
                        )
                    )
                }

                lastStart = wordStart
                lastEnd = wordEnd
                content = content.substring(wordMatch.range.last + 1)
            }

            // 最后一个词：剩余字符串对应最后记录的时间
            if (lastStart != -1 && content.isNotEmpty()) {
                words.add(
                    LyricWord(
                        word = content,
                        startTime = lastStart,
                        endTime = lastEnd
                    )
                )
            }

            if (words.isEmpty()) return@mapNotNull null

            val isBG = checkIsBG(words)
            if (isBG) trimBGParentheses(words)

            LyricLine(
                startTime = lineStart,
                endTime = lineStart + lineDuration,
                words = words.toList(),
                isBG = isBG
            )
        }
    }

    private fun checkIsBG(words: List<LyricWord>): Boolean {
        if (words.isEmpty()) return false
        val first = words.first().word
        val last = words.last().word
        return BEGIN_PAREN.containsMatchIn(first) && END_PAREN.containsMatchIn(last)
    }

    private fun trimBGParentheses(words: MutableList<LyricWord>) {
        if (words.isEmpty()) return
        val first = words.first()
        val last = words.last()
        words[0] = first.copy(word = first.word.replaceFirst(BEGIN_PAREN, ""))
        words[words.lastIndex] = last.copy(word = last.word.replace(END_PAREN, ""))
    }
}
