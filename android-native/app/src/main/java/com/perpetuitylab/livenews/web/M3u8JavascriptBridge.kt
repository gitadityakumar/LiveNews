package com.perpetuitylab.livenews.web

import android.webkit.JavascriptInterface
import org.json.JSONObject

class M3u8JavascriptBridge(private val onCandidate: (String) -> Unit) {
  @JavascriptInterface
  fun postMessage(message: String?) {
    val candidate = parseCandidate(message) ?: return
    onCandidate(candidate)
  }

  private fun parseCandidate(message: String?): String? {
    val payload = message?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return try {
      val json = JSONObject(payload)
      if (json.optString("type") != M3u8InjectedScript.MESSAGE_TYPE) return null
      json.optString("url").takeIf { it.contains(".m3u8", ignoreCase = true) }
    } catch (_: Exception) {
      payload.takeIf { it.contains(".m3u8", ignoreCase = true) }
    }
  }

  companion object {
    const val BRIDGE_NAME = "LiveNewsM3u8Bridge"
  }
}
