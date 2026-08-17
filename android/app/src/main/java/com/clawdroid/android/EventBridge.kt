package com.clawdroid.android

import android.webkit.WebView
import com.google.gson.Gson

class EventBridge(private val webView: WebView) {
    private val gson = Gson()

    fun emit(type: String, data: Any?) {
        val json = gson.toJson(data ?: emptyMap<String, Any>())
        val script = "window.__oc&&window.__oc.emit('$type',$json)"
        webView.post { webView.evaluateJavascript(script, null) }
    }
}
