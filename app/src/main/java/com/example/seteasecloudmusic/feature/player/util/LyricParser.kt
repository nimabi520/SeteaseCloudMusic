package com.example.seteasecloudmusic.feature.player.util

import com.example.seteasecloudmusic.feature.player.data.LyricData
import com.example.seteasecloudmusic.feature.player.data.YrcMetadata
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

data class ParsedLyricLine(
    val startTime: Long,     // Line start time in ms
    val duration: Long,      // Line duration in ms
    val words: List<ParsedLyricWord>
)

data class ParsedLyricWord(
    val startTime: Long,     // Word start time in ms
    val duration: Long,      // Word duration in ms
    val word: String         // Word text
)

data class ParsedYrcResult(
    val metadata: List<YrcMetadata>,
    val lines: List<ParsedLyricLine>
)

object LyricParser {
    // Regex for basic LRC format, e.g. [00:16.21]text
    private val lrcRegex = Regex("\\[(\\d{2,}):(\\d{2})(?:\\.(\\d{1,3}))?\\](.*)")

    // Regex for YRC format word-by-word, e.g. [16210,3460](16210,670,0)还(16880,410,0)没...
    private val yrcLineRegex = Regex("\\[(\\d+),(\\d+)\\](.*)")
    private val yrcWordRegex = Regex("\\((\\d+),(\\d+),\\d+\\)([^(]*)")

    fun parseYrc(yrcData: LyricData?): ParsedYrcResult {
        if (yrcData == null || yrcData.lyric.isBlank()) {
            return ParsedYrcResult(emptyList(), emptyList())
        }

        val lines = yrcData.lyric.split("\n")
        val parsedLines = mutableListOf<ParsedLyricLine>()
        val metadataList = mutableListOf<YrcMetadata>()
        val gson = Gson()

        for (line in lines) {
            val text = line.trim()
            if (text.isEmpty()) continue

            // Try to parse as JSON metadata if it starts with {
            if (text.startsWith("{") && text.endsWith("}")) {
                try {
                    val metadata = gson.fromJson(text, YrcMetadata::class.java)
                    if (metadata != null) {
                        metadataList.add(metadata)
                        continue
                    }
                } catch (e: JsonSyntaxException) {
                    // Ignore, maybe not a metadata json line
                }
            }

            val lineMatch = yrcLineRegex.find(text)
            if (lineMatch != null) {
                val lineStartTime = lineMatch.groupValues[1].toLong()
                val lineDuration = lineMatch.groupValues[2].toLong()
                val wordsContent = lineMatch.groupValues[3]

                val words = mutableListOf<ParsedLyricWord>()
                val wordMatches = yrcWordRegex.findAll(wordsContent)
                for (wordMatch in wordMatches) {
                    val wordStartTime = wordMatch.groupValues[1].toLong()
                    val wordDuration = wordMatch.groupValues[2].toLong()
                    val wordText = wordMatch.groupValues[3]
                    words.add(ParsedLyricWord(wordStartTime, wordDuration, wordText))
                }

                parsedLines.add(ParsedLyricLine(lineStartTime, lineDuration, words))
            }
        }
        return ParsedYrcResult(metadataList, parsedLines)
    }
}
