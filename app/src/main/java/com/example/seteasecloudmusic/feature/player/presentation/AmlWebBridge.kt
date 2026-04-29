package com.example.seteasecloudmusic.feature.player.presentation

import android.webkit.JavascriptInterface
import org.json.JSONObject

class AmlWebBridge(
    private val onMessage: (type: String, payload: JSONObject?) -> Unit
) {
    @JavascriptInterface
    fun postMessage(json: String) {
        runCatching {
            val obj = JSONObject(json)
            val type = obj.optString("type")
            val payload = obj.optJSONObject("payload")
            onMessage(type, payload)
        }
    }
}