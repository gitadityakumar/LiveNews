package com.perpetuitylab.livenews.ui.network

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.perpetuitylab.livenews.web.M3u8CaptureWebViewClient
import com.perpetuitylab.livenews.web.M3u8InjectedScript
import com.perpetuitylab.livenews.web.M3u8JavascriptBridge
import com.perpetuitylab.livenews.web.M3u8Validator
import kotlinx.coroutines.delay

private const val SCAN_TIMEOUT_MS = 25_000L

@Composable
fun NetworkInspectorScreen(
  channelId: String?,
  pageUrl: String?,
  onPlaylistCaptured: (channelId: String, url: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val currentOnPlaylistCaptured by rememberUpdatedState(onPlaylistCaptured)
  var capturedUrl by remember(channelId, pageUrl) { mutableStateOf<String?>(null) }
  var timedOut by remember(channelId, pageUrl) { mutableStateOf(false) }

  if (channelId.isNullOrBlank() || pageUrl.isNullOrBlank()) {
    MissingNetworkInspectorArgs(modifier)
    return
  }

  LaunchedEffect(channelId, pageUrl, capturedUrl) {
    if (capturedUrl == null) {
      delay(SCAN_TIMEOUT_MS)
      if (capturedUrl == null) timedOut = true
    }
  }

  Surface(modifier = modifier.fillMaxSize(), color = Color(0xFF050816)) {
    Column(modifier = Modifier.safeDrawingPadding().padding(16.dp)) {
      Text(
        text = "Play the stream to capture URL",
        color = Color.White,
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text = "Channel: $channelId",
        color = Color(0xFF9CA3AF),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 4.dp),
      )
      if (timedOut && capturedUrl == null) {
        Text(
          text = "No .m3u8 detected yet. This channel may use protected playback.",
          color = Color(0xFFF97316),
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(top = 8.dp),
        )
      }

      NetworkInspectorWebView(
        pageUrl = pageUrl,
        isScanning = capturedUrl == null && !timedOut,
        onCandidate = { rawCandidate ->
          if (capturedUrl != null) return@NetworkInspectorWebView
          val playlistUrl = M3u8Validator.normalizeValidPlaylistUrl(rawCandidate) ?: return@NetworkInspectorWebView
          capturedUrl = playlistUrl
          currentOnPlaylistCaptured(channelId, playlistUrl)
        },
        modifier = Modifier.padding(top = 12.dp).fillMaxWidth().weight(1f),
      )
    }
  }
}

@Composable
private fun MissingNetworkInspectorArgs(modifier: Modifier = Modifier) {
  Surface(modifier = modifier.fillMaxSize(), color = Color(0xFF050816)) {
    Box(modifier = Modifier.safeDrawingPadding().padding(16.dp)) {
      Text(
        text = "Missing channelId or pageUrl.",
        color = Color(0xFFF87171),
        style = MaterialTheme.typography.bodyLarge,
      )
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun NetworkInspectorWebView(
  pageUrl: String,
  isScanning: Boolean,
  onCandidate: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val mainHandler = remember { Handler(Looper.getMainLooper()) }
  val currentOnCandidate by rememberUpdatedState(onCandidate)
  val borderModifier = if (isScanning) Modifier.tracingBorder() else Modifier.border(1.dp, Color(0xFF1F2937))

  Box(modifier = modifier.then(borderModifier)) {
    AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = {
        WebView(context).apply {
          layoutParams =
            ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT,
            )
          settings.javaScriptEnabled = true
          settings.domStorageEnabled = true
          settings.mediaPlaybackRequiresUserGesture = false
          settings.loadsImagesAutomatically = true
          settings.javaScriptCanOpenWindowsAutomatically = true
          webChromeClient = WebChromeClient()

          val candidateCallback: (String) -> Unit = { rawCandidate ->
            mainHandler.post { currentOnCandidate(rawCandidate) }
          }

          addJavascriptInterface(M3u8JavascriptBridge(candidateCallback), M3u8JavascriptBridge.BRIDGE_NAME)
          webViewClient = M3u8CaptureWebViewClient(candidateCallback)
          loadUrl(pageUrl)
        }
      },
      update = { webView ->
        val script = M3u8InjectedScript.build()
        webView.evaluateJavascript(script, null)
        if (webView.url != pageUrl) {
          webView.loadUrl(pageUrl)
        }
      },
      onRelease = { webView ->
        webView.stopLoading()
        webView.removeJavascriptInterface(M3u8JavascriptBridge.BRIDGE_NAME)
        webView.destroy()
      },
    )
  }
}

@Composable
private fun Modifier.tracingBorder(): Modifier {
  val transition = rememberInfiniteTransition(label = "network-inspector-border")
  val pulse by
    transition.animateFloat(
      initialValue = 0.35f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 900, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "network-inspector-border-alpha",
    )

  return border(BorderStroke(2.dp, Color(0xFF38BDF8).copy(alpha = pulse)))
}
