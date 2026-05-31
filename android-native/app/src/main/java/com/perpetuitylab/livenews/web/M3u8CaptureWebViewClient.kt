package com.perpetuitylab.livenews.web

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

@Suppress("DEPRECATION")
class M3u8CaptureWebViewClient(
  private val onCandidate: (String) -> Unit,
  private val onPageReady: (WebView) -> Unit = {},
) : WebViewClient() {
  override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
    observe(request?.url?.toString())
    return super.shouldInterceptRequest(view, request)
  }

  @Deprecated("Deprecated in Android WebViewClient")
  override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
    observe(url)
    return super.shouldInterceptRequest(view, url)
  }

  override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
    observe(request?.url?.toString())
    return false
  }

  @Deprecated("Deprecated in Android WebViewClient")
  override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
    observe(url)
    return false
  }

  override fun onLoadResource(view: WebView?, url: String?) {
    observe(url)
    super.onLoadResource(view, url)
  }

  override fun onPageCommitVisible(view: WebView?, url: String?) {
    view?.let(onPageReady)
    super.onPageCommitVisible(view, url)
  }

  override fun onPageFinished(view: WebView?, url: String?) {
    view?.let(onPageReady)
    super.onPageFinished(view, url)
  }

  private fun observe(url: String?) {
    if (url?.contains(".m3u8", ignoreCase = true) == true) {
      onCandidate(url)
    }
  }
}
