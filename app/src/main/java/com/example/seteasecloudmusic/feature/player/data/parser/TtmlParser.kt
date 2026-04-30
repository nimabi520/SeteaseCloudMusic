package com.example.seteasecloudmusic.feature.player.data.parser

import com.example.seteasecloudmusic.feature.player.domain.model.LyricLine
import com.example.seteasecloudmusic.feature.player.domain.model.LyricWord
import com.example.seteasecloudmusic.feature.player.domain.model.ParsedLyrics
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import kotlin.math.roundToInt

object TtmlParser {

    private const val NS_TT = "http://www.w3.org/ns/ttml"
    private const val NS_ITUNES = "http://music.apple.com/lyric-ttml-internal"
    private const val NS_TTM = "http://www.w3.org/ns/ttml#metadata"

    private val TIME_REGEX = Regex("""^(?:(?:(\d+):)?(\d+):)?(\d+(?:\.\d+)?)$""")

    fun parse(ttmlXml: String): ParsedLyrics {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(ttmlXml))

        val translationMap = mutableMapOf<String, String>()
        val lines = mutableListOf<LyricLine>()
        var timingMode = "Line"
        var defaultAgent: String? = null
        val agentSet = mutableSetOf<String>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                val tagName = parser.name
                val ns = parser.namespace

                when {
                    tagName == "tt" && ns == NS_TT -> {
                        val timing = parser.getAttributeValue(NS_ITUNES, "timing")
                        if (timing != null) timingMode = timing
                    }

                    tagName == "translation" -> {
                        parseTranslations(parser, translationMap)
                    }

                    tagName == "p" && (ns == NS_TT || ns.isEmpty()) -> {
                        val lineId = parser.getAttributeValue(NS_ITUNES, "key")
                        val agent = parser.getAttributeValue(NS_TTM, "agent")
                        if (agent != null) {
                            agentSet.add(agent)
                            if (defaultAgent == null) defaultAgent = agent
                        }

                        val lineResult = parseLine(parser, lineId, agent)
                        val translated = lineId?.let { translationMap[it] } ?: ""

                        if (lineResult.mainWords.isNotEmpty()) {
                            lines.add(
                                LyricLine(
                                    words = lineResult.mainWords,
                                    translatedLyric = translated,
                                    startTime = lineResult.mainStart,
                                    endTime = lineResult.mainEnd,
                                    isDuet = defaultAgent != null && agent != defaultAgent
                                )
                            )
                        }

                        if (lineResult.bgWords.isNotEmpty()) {
                            val bgTranslated = lineId?.let { translationMap["$it-bg"] } ?: ""
                            lines.add(
                                LyricLine(
                                    words = lineResult.bgWords,
                                    translatedLyric = bgTranslated,
                                    startTime = lineResult.bgStart,
                                    endTime = lineResult.bgEnd,
                                    isBG = true,
                                    isDuet = defaultAgent != null && agent != defaultAgent
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        lines.sortBy { it.startTime }

        return ParsedLyrics(
            lines = lines,
            hasWordTiming = timingMode == "Word" || lines.any { it.words.size > 1 },
            source = com.example.seteasecloudmusic.feature.player.domain.model.LyricSource.TTML_DB
        )
    }

    private fun parseTranslations(parser: XmlPullParser, map: MutableMap<String, String>) {
        var eventType = parser.eventType
        val depth = parser.depth
        var currentFor: String? = null
        val buffer = StringBuilder()

        while (!(eventType == XmlPullParser.END_TAG && parser.depth == depth)) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "text" -> {
                            currentFor = parser.getAttributeValue("http://www.w3.org/XML/1998/namespace", "for")
                                ?: parser.getAttributeValue(null, "for")
                            buffer.clear()
                        }
                        "span" -> {
                            // 收集 span 内的文本
                            val role = parser.getAttributeValue(NS_TTM, "role")
                            if (role == "x-bg" && currentFor != null) {
                                currentFor = "$currentFor-bg"
                            }
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    buffer.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "text" -> {
                            if (currentFor != null) {
                                map[currentFor] = buffer.toString().trim()
                            }
                            currentFor = null
                        }
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private data class LineParseResult(
        val mainWords: List<LyricWord>,
        val mainStart: Int,
        val mainEnd: Int,
        val bgWords: List<LyricWord>,
        val bgStart: Int,
        val bgEnd: Int
    )

    private fun parseLine(parser: XmlPullParser, lineId: String?, agent: String?): LineParseResult {
        val lineBegin = parseTime(parser.getAttributeValue(null, "begin"))
        val lineEnd = parseTime(parser.getAttributeValue(null, "end"))

        val mainWords = mutableListOf<LyricWord>()
        val bgWords = mutableListOf<LyricWord>()
        var currentIsBg = false
        var currentBuffer = StringBuilder()
        var currentStart = -1
        var currentEnd = -1
        var lastWordEnd = lineBegin

        fun flushWord(toBg: Boolean) {
            val text = currentBuffer.toString()
            if (text.isNotEmpty()) {
                val target = if (toBg) bgWords else mainWords
                val start = if (currentStart >= 0) currentStart else lastWordEnd
                val end = if (currentEnd >= 0) currentEnd else (lineEnd.takeIf { it > 0 } ?: start + 1000)
                target.add(LyricWord(word = text, startTime = start, endTime = end))
                lastWordEnd = end
            }
            currentBuffer.clear()
            currentStart = -1
            currentEnd = -1
        }

        var eventType = parser.next()
        val depth = parser.depth

        while (!(eventType == XmlPullParser.END_TAG && parser.depth == depth && parser.name == "p")) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val name = parser.name
                    val ns = parser.namespace

                    if (name == "span" && (ns == NS_TT || ns.isEmpty())) {
                        val role = parser.getAttributeValue(NS_TTM, "role")
                        if (role == "x-bg") {
                            flushWord(currentIsBg)
                            currentIsBg = true
                        } else {
                            // 普通 span：可能有 begin/end
                            val begin = parseTime(parser.getAttributeValue(null, "begin"))
                            val end = parseTime(parser.getAttributeValue(null, "end"))

                            if (begin >= 0 && end > 0) {
                                flushWord(currentIsBg)
                                currentStart = begin
                                currentEnd = end
                            }

                            // 递归或继续读取内部文本
                            eventType = parser.next()
                            var spanDepth = parser.depth
                            while (!(eventType == XmlPullParser.END_TAG && parser.depth == spanDepth && parser.name == "span")) {
                                if (eventType == XmlPullParser.TEXT) {
                                    currentBuffer.append(parser.text)
                                } else if (eventType == XmlPullParser.START_TAG && parser.name == "span") {
                                    // 嵌套 span（如 ruby）
                                    val rbBegin = parseTime(parser.getAttributeValue(null, "begin"))
                                    val rbEnd = parseTime(parser.getAttributeValue(null, "end"))
                                    eventType = parser.next()
                                    val rubyDepth = parser.depth
                                    val rubyText = StringBuilder()
                                    while (!(eventType == XmlPullParser.END_TAG && parser.depth == rubyDepth && parser.name == "span")) {
                                        if (eventType == XmlPullParser.TEXT) rubyText.append(parser.text)
                                        eventType = parser.next()
                                    }
                                    if (rubyText.isNotEmpty()) {
                                        flushWord(currentIsBg)
                                        val s = if (rbBegin >= 0) rbBegin else currentStart
                                        val e = if (rbEnd > 0) rbEnd else currentEnd
                                        val target = if (currentIsBg) bgWords else mainWords
                                        target.add(LyricWord(word = rubyText.toString(), startTime = s, endTime = e))
                                    }
                                }
                                eventType = parser.next()
                            }
                            flushWord(currentIsBg)
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    currentBuffer.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "span") {
                        val role = parser.getAttributeValue(NS_TTM, "role")
                        // 退出 bg span
                        if (parser.depth == depth + 1) {
                            // 这里需要判断是否是 bg span 结束
                            // XmlPullParser 的 END_TAG 没有属性，所以需要跟踪状态
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        flushWord(currentIsBg)

        // 如果没有逐字时间，把整个 p 当做一个词
        if (mainWords.isEmpty() && lineBegin >= 0) {
            // 尝试提取 p 内的所有文本作为一行
            // 这种情况下可能已经错过了，需要重新读取？
            // 实际上上面的逻辑应该已经处理了文本节点
        }

        val mainStart = mainWords.firstOrNull()?.startTime ?: lineBegin
        val mainEnd = mainWords.lastOrNull()?.endTime ?: lineEnd
        val bgStart = bgWords.firstOrNull()?.startTime ?: lineBegin
        val bgEnd = bgWords.lastOrNull()?.endTime ?: lineEnd

        return LineParseResult(mainWords, mainStart, mainEnd, bgWords, bgStart, bgEnd)
    }

    private fun parseTime(timeStr: String?): Int {
        if (timeStr.isNullOrBlank()) return -1
        val clean = timeStr.trim()
        if (clean.endsWith("s")) {
            return (clean.dropLast(1).toDoubleOrNull() ?: 0.0).times(1000).roundToInt()
        }
        val match = TIME_REGEX.matchEntire(clean) ?: return -1
        val hours = match.groupValues[1].toIntOrNull() ?: 0
        val minutes = match.groupValues[2].toIntOrNull() ?: 0
        val seconds = match.groupValues[3].toDouble()
        return ((hours * 3600 + minutes * 60 + seconds) * 1000).roundToInt()
    }
}
