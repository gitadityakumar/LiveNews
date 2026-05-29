package com.perpetuitylab.livenews.web

import java.util.Locale

object M3u8Validator {
  fun normalizeValidPlaylistUrl(inputUrl: String?): String? {
    val extracted = M3u8UrlExtractor.extract(inputUrl)?.trim() ?: return null
    val lower = extracted.lowercase(Locale.US)
    if (!lower.startsWith("http://") && !lower.startsWith("https://")) return null
    if (lower.contains("youboranqs")) return null

    val endpoint = extracted.substringBefore('?').substringBefore('#')
    if (!endpoint.contains(".m3u8", ignoreCase = true)) return null

    return extracted
  }

  fun isValidPlaylistUrl(inputUrl: String?): Boolean = normalizeValidPlaylistUrl(inputUrl) != null
}
