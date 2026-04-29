package com.example.seteasecloudmusic.feature.player.presentation

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.seteasecloudmusic.core.player.MusicPlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPlayerScreen(
    musicPlayerController: MusicPlayerController,
    ttmlProvider: (suspend (songId: String) -> String?)? = null
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Web 是否已 ready，避免页面没加载完就注入失败
    val bridgeState = remember { object { var webReady: Boolean = false } }

    fun dispatchToWeb(type: String, payload: JSONObject) {
        val webView = webViewRef ?: return
        val message = JSONObject().apply {
            put("type", type)
            put("payload", payload)
        }
        val safeJson = message.toString().replace("'", "\\'")
        val js = "window.dispatchEvent(new CustomEvent('scm-native-message',{detail: JSON.parse('$safeJson')}));"
        webView.post {
            webView.evaluateJavascript(js, null)
        }
    }

    fun dispatchPlaybackNow() {
        val st = musicPlayerController.playbackState.value
        dispatchToWeb(
            "SET_PLAYBACK",
            JSONObject().apply {
                put("currentTimeMs", st.currentPositionMs)
                put("durationMs", st.durationMs)
                put("playing", st.status.name == "PLAYING")
            }
        )
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView.setWebContentsDebuggingEnabled(true)
            WebView(context).apply {
            webViewRef = this
            setBackgroundColor(Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            // 👇👇👇 加上这两行！强制 WebView 读取前端的 Viewport 视口配置
            settings.useWideViewPort = false
            settings.loadWithOverviewMode = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()

            addJavascriptInterface(
                AmlWebBridge { type, payload ->
                    when (type) {
                        "TOGGLE_PLAY" -> {
                            val state = musicPlayerController.playbackState.value.status.name
                            if (state == "PLAYING") musicPlayerController.pause() else musicPlayerController.resume()
                        }
                        "NEXT_TRACK" -> musicPlayerController.playNext()
                        "PREV_TRACK" -> musicPlayerController.playPrevious()
                        "SEEK_TO" -> {
                            val timeMs = payload?.optLong("timeMs")?.toInt() ?: 0
                            musicPlayerController.seekTo(timeMs)
                        }
                        "WEB_READY" -> {
                            bridgeState.webReady = true
                            // ready 后先把当前播放态推过去
                            dispatchPlaybackNow()

                            // 再推当前曲目信息（有则推）
                            val track = musicPlayerController.playbackState.value.currentTrack
                            if (track != null) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    val lrcOrTtml = ttmlProvider?.invoke(track.id.toString()) ?: ""
                                    dispatchToWeb(
                                        "SET_TRACK",
                                        JSONObject().apply {
                                            put("id", track.id.toString())
                                            put("title", track.title)
                                            put("artist", track.artists.joinToString(" / ") { it.name })
                                            put("coverUrl", track.coverUrl ?: "")
                                            // 你当前前端是 parseLrc，所以字段名先用 lrc
                                            put("lrc", lrcOrTtml)
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                "SCMBridge"
            )

            loadUrl("http://192.168.1.184:5173")
        }
        }
    )

    DisposableEffect(Unit) {
        val scope = CoroutineScope(Dispatchers.Main)
        var trackJob: Job? = null

        // 持续推播放状态（只在 webReady 后推）
        val playbackJob = scope.launch {
            musicPlayerController.playbackState.collectLatest { st ->
                if (!bridgeState.webReady) return@collectLatest
                dispatchToWeb(
                    "SET_PLAYBACK",
                    JSONObject().apply {
                        put("currentTimeMs", st.currentPositionMs)
                        put("durationMs", st.durationMs)
                        put("playing", st.status.name == "PLAYING")
                    }
                )
            }
        }

        // 监听 currentTrack 变化，变化时推 SET_TRACK
        trackJob = scope.launch {
            var lastTrackId: String? = null
            musicPlayerController.playbackState.collectLatest { st ->
                if (!bridgeState.webReady) return@collectLatest
                val track = st.currentTrack ?: return@collectLatest
                val trackId = track.id.toString()
                if (trackId == lastTrackId) return@collectLatest
                lastTrackId = trackId

                val lrcOrTtml = ttmlProvider?.invoke(track.id.toString()) ?: ""
                dispatchToWeb(
                    "SET_TRACK",
                    JSONObject().apply {
                        put("id", trackId)
                        put("title", track.title)
                        put("artist", track.artists.joinToString(" / ") { it.name })
                        put("coverUrl", track.coverUrl ?: "")
                        put("lrc", lrcOrTtml)
                    }
                )
            }
        }

        onDispose {
            playbackJob.cancel()
            trackJob?.cancel()
            webViewRef?.removeJavascriptInterface("SCMBridge")
            webViewRef?.destroy()
        }
    }
}