@file:Suppress("OVERRIDE_DEPRECATION")

package com.perpetuitylab.livenews.playback

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData

data class LiveStreamPlayerState(
  val isLoading: Boolean = true,
  val isPlaying: Boolean = false,
  val isMuted: Boolean = false,
  val isLive: Boolean = true,
  val isAtLiveEdge: Boolean = true,
  val errorMessage: String? = null,
)

@Stable
class LiveStreamPlayerController
internal constructor(
  val player: ExoPlayer,
  private val mediaSourceFactory: HlsMediaSource.Factory,
) {
  var state by mutableStateOf(LiveStreamPlayerState())
    private set

  private var currentStreamUrl: String? = null
  private var userPaused = false
  private var shouldResumeAfterHostPause = false
  private var renderedFirstFrame = false
  private var lastPlaybackState = Player.STATE_IDLE
  private var lastLiveOffsetLogElapsedMs = 0L
  private var lastLiveOffsetBucketMs = Long.MIN_VALUE

  private val analyticsListener =
    object : AnalyticsListener {
      override fun onLoadStarted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
      ) {
        Log.d(TAG, "load start type=${mediaLoadData.dataType} uri=${loadEventInfo.uri}")
      }

      @Suppress("DEPRECATION")
      override fun onRenderedFirstFrame(
        eventTime: AnalyticsListener.EventTime,
        output: Any,
        renderTimeMs: Long,
      ) {
        renderedFirstFrame = true
        Log.d(TAG, "rendered first frame renderTimeMs=$renderTimeMs liveOffsetMs=${player.currentLiveOffset}")
      }
    }

  private val listener =
    object : Player.Listener {
      override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING && renderedFirstFrame) {
          Log.d(TAG, "rebuffering liveOffsetMs=${player.currentLiveOffset} url=$currentStreamUrl")
        } else if (lastPlaybackState == Player.STATE_BUFFERING && playbackState == Player.STATE_READY) {
          Log.d(TAG, "buffering ended liveOffsetMs=${player.currentLiveOffset}")
        }
        lastPlaybackState = playbackState
        updateState()
      }

      override fun onIsPlayingChanged(isPlaying: Boolean) {
        updateState()
      }

      override fun onPlayerError(error: PlaybackException) {
        state = state.copy(isLoading = false, errorMessage = error.message)
      }

      override fun onEvents(player: Player, events: Player.Events) {
        updateState()
      }
    }

  init {
    player.addListener(listener)
    player.addAnalyticsListener(analyticsListener)
  }

  @OptIn(UnstableApi::class)
  fun load(streamUrl: String, autoplay: Boolean = true) {
    if (currentStreamUrl == streamUrl) {
      if (autoplay && !userPaused) player.play()
      return
    }

    currentStreamUrl = streamUrl
    userPaused = !autoplay
    renderedFirstFrame = false
    lastPlaybackState = Player.STATE_IDLE
    state = state.copy(isLoading = true, errorMessage = null)
    Log.d(TAG, "load url=$streamUrl autoplay=$autoplay")

    val mediaItem =
      MediaItem.Builder()
        .setUri(Uri.parse(streamUrl))
        .setMimeType(MimeTypes.APPLICATION_M3U8)
        .setLiveConfiguration(
          MediaItem.LiveConfiguration.Builder()
            .setTargetOffsetMs(LIVE_TARGET_OFFSET_MS)
            .setMinOffsetMs(LIVE_MIN_OFFSET_MS)
            .setMaxOffsetMs(LIVE_MAX_OFFSET_MS)
            .setMinPlaybackSpeed(0.97f)
            .setMaxPlaybackSpeed(1.03f)
            .build(),
        )
        .build()

    player.setMediaSource(mediaSourceFactory.createMediaSource(mediaItem))
    player.prepare()
    player.playWhenReady = autoplay
    if (autoplay) player.play()
    updateState()
  }

  fun play() {
    userPaused = false
    player.play()
    updateState()
  }

  fun pause() {
    userPaused = true
    player.pause()
    updateState()
  }

  fun togglePlayPause() {
    if (player.isPlaying) pause() else play()
  }

  fun setMuted(muted: Boolean) {
    player.volume = if (muted) 0f else 1f
    updateState()
  }

  fun toggleMuted() {
    setMuted(player.volume > 0f)
  }

  fun jumpToLive() {
    if (player.isCurrentMediaItemLive) {
      player.seekToDefaultPosition()
    } else if (player.duration != C.TIME_UNSET) {
      player.seekTo(player.duration)
    }
    play()
  }

  fun onHostPause() {
    shouldResumeAfterHostPause = player.isPlaying && !userPaused
    player.pause()
    updateState()
  }

  fun onHostResume() {
    if (shouldResumeAfterHostPause && !userPaused) {
      player.play()
    }
    shouldResumeAfterHostPause = false
    updateState()
  }

  fun release() {
    player.removeListener(listener)
    player.removeAnalyticsListener(analyticsListener)
    player.release()
  }

  private fun updateState() {
    val liveOffset = player.currentLiveOffset
    logLiveOffset(liveOffset)
    val isLive = player.isCurrentMediaItemLive || liveOffset != C.TIME_UNSET
    val isAtLiveEdge =
      when {
        !isLive -> true
        liveOffset != C.TIME_UNSET -> liveOffset <= LIVE_EDGE_TOLERANCE_MS
        player.duration != C.TIME_UNSET -> player.currentPosition >= player.duration - LIVE_EDGE_TOLERANCE_MS
        else -> true
      }

    state =
      state.copy(
        isLoading = player.playbackState == Player.STATE_BUFFERING || player.playbackState == Player.STATE_IDLE,
        isPlaying = player.isPlaying,
        isMuted = player.volume == 0f,
        isLive = isLive,
        isAtLiveEdge = isAtLiveEdge,
        errorMessage = null,
      )
  }

  private fun logLiveOffset(liveOffset: Long) {
    if (liveOffset == C.TIME_UNSET) return

    val now = SystemClock.elapsedRealtime()
    val bucketMs = liveOffset / LIVE_OFFSET_LOG_BUCKET_MS * LIVE_OFFSET_LOG_BUCKET_MS
    if (now - lastLiveOffsetLogElapsedMs >= LIVE_OFFSET_LOG_INTERVAL_MS || bucketMs != lastLiveOffsetBucketMs) {
      Log.d(TAG, "live offset ${liveOffset}ms state=${player.playbackState} playing=${player.isPlaying}")
      lastLiveOffsetLogElapsedMs = now
      lastLiveOffsetBucketMs = bucketMs
    }
  }

  private companion object {
    const val TAG = "LiveStreamPlayer"
    const val LIVE_EDGE_TOLERANCE_MS = 5_000L
    const val LIVE_MIN_OFFSET_MS = 8_000L
    const val LIVE_TARGET_OFFSET_MS = 10_000L
    const val LIVE_MAX_OFFSET_MS = 12_000L
    const val LIVE_OFFSET_LOG_INTERVAL_MS = 10_000L
    const val LIVE_OFFSET_LOG_BUCKET_MS = 2_000L
  }
}

@OptIn(UnstableApi::class)
@Composable
fun rememberLiveStreamPlayerController(
  streamUrl: String,
  autoplay: Boolean = true,
  muted: Boolean = false,
): LiveStreamPlayerController {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val controller =
    remember {
      val player =
        ExoPlayer.Builder(context)
          .setLoadControl(buildLoadControl())
          .build()
          .apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = autoplay
          }

      LiveStreamPlayerController(
        player = player,
        mediaSourceFactory = HlsMediaSource.Factory(buildHttpDataSourceFactory(context)),
      )
    }

  DisposableEffect(controller, streamUrl, autoplay) {
    controller.load(streamUrl = streamUrl, autoplay = autoplay)
    onDispose {}
  }

  DisposableEffect(controller, muted) {
    controller.setMuted(muted)
    onDispose {}
  }

  DisposableEffect(lifecycleOwner, controller) {
    val observer =
      LifecycleEventObserver { _, event ->
        when (event) {
          Lifecycle.Event.ON_PAUSE -> controller.onHostPause()
          Lifecycle.Event.ON_RESUME -> controller.onHostResume()
          else -> Unit
        }
      }

    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      controller.release()
    }
  }

  return controller
}

private fun buildHttpDataSourceFactory(context: Context): DefaultHttpDataSource.Factory =
  DefaultHttpDataSource.Factory()
    .setUserAgent("LiveNewsAndroid/1.0 (${context.packageName}; Android) Media3-HLS")
    .setConnectTimeoutMs(10_000)
    .setReadTimeoutMs(15_000)
    .setAllowCrossProtocolRedirects(true)

private fun buildLoadControl(): DefaultLoadControl =
  DefaultLoadControl.Builder()
    .setBufferDurationsMs(
      15_000,
      50_000,
      2_500,
      5_000,
    )
    .setPrioritizeTimeOverSizeThresholds(true)
    .build()
