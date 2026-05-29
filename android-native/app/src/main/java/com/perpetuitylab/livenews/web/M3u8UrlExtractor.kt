package com.perpetuitylab.livenews.web

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale

object M3u8UrlExtractor {
  private val preferredParamNames = listOf("param8", "url", "streamurl")
  private val embeddedHttpPlaylistPattern =
    Regex("""https?://[^\s'"<>]+?\.m3u8(?:\?[^\s'"<>]*)?""", RegexOption.IGNORE_CASE)

  fun extract(inputUrl: String?): String? {
    val candidate = inputUrl.cleanCandidate() ?: return null

    normalizeDirectPlaylistUrl(candidate)?.let { return it }
    extractFromQueryParams(candidate)?.let { return it }
    normalizePlaylistCandidate(candidate)?.let { return it }

    val decodedCandidate = candidate.percentDecode()
    if (decodedCandidate != candidate) {
      normalizeDirectPlaylistUrl(decodedCandidate)?.let { return it }
      extractFromQueryParams(decodedCandidate)?.let { return it }
      normalizePlaylistCandidate(decodedCandidate)?.let { return it }
    }

    return null
  }

  private fun extractFromQueryParams(input: String): String? {
    val query = input.queryPart() ?: return null
    val params = query.split('&').mapNotNull { rawPair ->
      if (rawPair.isBlank()) return@mapNotNull null
      val equalsIndex = rawPair.indexOf('=')
      val rawName = if (equalsIndex >= 0) rawPair.substring(0, equalsIndex) else rawPair
      val rawValue = if (equalsIndex >= 0) rawPair.substring(equalsIndex + 1) else ""
      QueryParam(name = rawName.percentDecode(), value = rawValue.percentDecode())
    }

    for (preferredName in preferredParamNames) {
      params
        .firstOrNull { param ->
          param.name.lowercase(Locale.US) == preferredName &&
            param.value.contains(".m3u8", ignoreCase = true)
        }
        ?.value
        ?.let { normalizePlaylistCandidate(it) }
        ?.let { return it }
    }

    return params
      .asSequence()
      .map { it.value }
      .firstNotNullOfOrNull { value ->
        if (value.contains(".m3u8", ignoreCase = true)) normalizePlaylistCandidate(value) else null
      }
  }

  private fun normalizePlaylistCandidate(value: String): String? {
    val cleaned = value.cleanCandidate() ?: return null
    if (!cleaned.contains(".m3u8", ignoreCase = true)) return null

    embeddedHttpPlaylistPattern.find(cleaned)?.let { return it.value }
    return cleaned
  }

  private fun normalizeDirectPlaylistUrl(value: String): String? {
    val cleaned = value.cleanCandidate() ?: return null
    val endpoint = cleaned.substringBefore('?').substringBefore('#')
    if (!endpoint.contains(".m3u8", ignoreCase = true)) return null
    return cleaned
  }

  private fun String.queryPart(): String? {
    val queryStart = indexOf('?')
    if (queryStart < 0 || queryStart == lastIndex) return null
    val fragmentStart = indexOf('#', startIndex = queryStart + 1)
    return if (fragmentStart >= 0) substring(queryStart + 1, fragmentStart) else substring(queryStart + 1)
  }

  private fun String?.cleanCandidate(): String? {
    val trimmed = this?.trim()?.trim('"', '\'') ?: return null
    return trimmed.takeIf { it.isNotBlank() }
  }

  private fun String.percentDecode(): String =
    try {
      URLDecoder.decode(this, StandardCharsets.UTF_8.name())
    } catch (_: IllegalArgumentException) {
      this
    }

  private data class QueryParam(val name: String, val value: String)
}
