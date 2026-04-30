package com.example.seteasecloudmusic.feature.player.data.parser

import com.example.seteasecloudmusic.feature.player.domain.model.LyricLine
import com.example.seteasecloudmusic.feature.player.domain.model.LyricWord

object LrcParser {

    private val TIME_PATTERN = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})]""")

    fun parse(lrcText: String): List<LyricLine> {
        val rawLines = lrcText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // 第一遍：提取所有 (timestamp, text) 对
        val timedTexts = mutableListOf<Pair<Int, String>>()

        for (line in rawLines) {
            val timeMatches = TIME_PATTERN.findAll(line).toList()
            if (timeMatches.isEmpty()) continue

            // 歌词文本 = 整行去掉所有时间标签后的剩余部分
            var text = line
            for (match in timeMatches) {
                text = text.replace(match.value, "")
            }
            text = text.trim()

            // 每个时间标签生成一个独立行（多时间轴重复行）
            for (match in timeMatches) {
                val minutes = match.groupValues[1].toInt()
                val seconds = match.groupValues[2].toInt()
                val fraction = match.groupValues[3]
                val millis = when (fraction.length) {
                    2 -> (minutes * 60 + seconds) * 1000 + fraction.toInt() * 10
                    3 -> (minutes * 60 + seconds) * 1000 + fraction.toInt()
                    else -> (minutes * 60 + seconds) * 1000
                }
                timedTexts.add(millis to text)
            }
        }

        if (timedTexts.isEmpty()) return emptyList()

        // 按时间排序
        timedTexts.sortBy { it.first }

        // 第二遍：构建 LyricLine，用下一行的开始时间作为当前行的结束时间
        val result = mutableListOf<LyricLine>()
        for (i in timedTexts.indices) {
            val (startTime, text) = timedTexts[i]
            val endTime = if (i + 1 < timedTexts.size) {
                timedTexts[i + 1].first
            } else {
                startTime + 5000  // 最后一行默认延续 5 秒
            }

            val word = LyricWord(
                word = text,
                startTime = startTime,
                endTime = endTime
            )

            result.add(
                LyricLine(
                    words = listOf(word),
                    startTime = startTime,
                    endTime = endTime
                )
            )
        }

        return result
    }
}
